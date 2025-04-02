package com.rusine.command

import com.rusine.*
import com.rusine.models.TrackWrapper
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.*
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.audio.on
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem

class MusicCommandManager(private val link: LavaKord) {
    private val linkMap = mutableMapOf<Snowflake, Link>()

    suspend fun responseCommand(command: MusicCommand, interaction: GuildChatInputCommandInteraction) {
        when (command) {
            MusicCommand.LIST -> interaction.processListCommand()
            MusicCommand.PLAY -> interaction.processPlayCommand()
            MusicCommand.REMOVE -> interaction.processRemoveCommand()
            MusicCommand.SKIP -> interaction.processSkipCommand()
            MusicCommand.DROP -> interaction.processDropCommand()
        }
    }

    private suspend fun GuildChatInputCommandInteraction.processListCommand() {
        val link = getLink(guild, this.channel)

        val currentPlayingTrack = link.player.currentPlayingTrack

        if (currentPlayingTrack == null) {
            respondEphemeral { embed { description = "재생중인 음원이 없어요." } }
            return
        }

        val playlist = link.player.playlist

        respondEphemeral {
            embed {
                author {
                    this.name = "▶ [재생목록]"
                }

                field(
                    name = "재생중",
                    value = { "[${currentPlayingTrack.track.info.title}](${currentPlayingTrack.track.info.uri}) - ${currentPlayingTrack.regUserMention}\n" }
                )

                thumbnail { this.url = "${currentPlayingTrack.track.info.artworkUrl}" }

                if (playlist.isEmpty()) {
                    return@embed
                }

                field { } // for space between current track and playlist

                val playlistString = buildString {
                    playlist.forEachIndexed { index, trackWrapper ->
                        appendLine("`#${index + 1}` [${trackWrapper.track.info.title}](${trackWrapper.track.info.uri}) - ${trackWrapper.regUserMention}\n")
                    }
                }

                field(
                    name = "대기열",
                    value = { playlistString }
                )
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.processPlayCommand() {
        val link = getLink(guild, channel)

        val query = command.strings["query"] ?: run {
            respondEphemeral { embed { description = "주소 또는 검색어가 없어요." } }
            return
        }

        val search = if (query.startsWith("http")) {
            query
        } else {
            "ytsearch:$query"
        }

        val track = when (val item = link.loadItem(search)) {
            is LoadResult.TrackLoaded -> item.data
            is LoadResult.PlaylistLoaded -> item.data.tracks.first()
            is LoadResult.SearchResult -> item.data.tracks.first()
            is LoadResult.NoMatches -> {
                respondEphemeral { embed { description = "음원을 찾을 수 없어요" } }
                return
            }
            is LoadResult.LoadFailed -> {
                respondEphemeral { embed { description = "음원을 불러올 수 없어요." } }
                return
            }
        }

        val trackWrapper = TrackWrapper(
            regUserMention = getMember().mention,
            track = track
        )

        link.player.register(trackWrapper)

        if (link.state != Link.State.CONNECTED) {
            val voiceState = getMember().getVoiceStateOrNull()

            if (voiceState?.channelId == null) {
                respondEphemeral { embed { description = "먼저 보이스 채널에 접속하세요." } }
                return
            }

            link.connectAudio(voiceState.channelId!!.value)
        }

        if (!link.player.isPlaying) {
            link.player.playNext()
        }

        respondPublic {
            embed {
                author {
                    this.name = "음원을 추가했어요."
                }

                description =
                    "[${trackWrapper.track.info.title}](${trackWrapper.track.info.uri}) - ${trackWrapper.regUserMention}"
                thumbnail { url = "${trackWrapper.track.info.artworkUrl}" }
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.processRemoveCommand() {
        val link = getLink(guild, channel)

        if (!link.player.hasNext()) {
            respondEphemeral { embed { description = "음원 대기열이 없어요." } }
            return
        }

        val index = command.integers["index"]?.minus(1)?.toInt() ?: run {
            respondEphemeral { embed { description = "삭제할 음원의 대기열 순번을 입력해주세요." } }
            return
        }

        val removedTrack = link.player.deleteTrack(index) ?: run {
            respondEphemeral { embed { description = "음원을 찾지 못했어요." } }
            return
        }

        respondPublic {
            embed {
                author { name = "음원을 지웠어요." }

                description = "[${removedTrack.track.info.title}](${removedTrack.track.info.uri}) - ${removedTrack.regUserMention}"
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.processSkipCommand() {
        val link = getLink(guild, channel)

        if (!link.player.isPlaying) {
            respondEphemeral { embed { description = "재생중인 음원이 없어요." } }
            return
        }

        link.player.stopTrack()

        respondPublic { embed { description = "재생중인 음원을 스킵했어요." } }
    }

    private suspend fun GuildChatInputCommandInteraction.processDropCommand() {
        val link = getLink(guild, channel)
        link.player.dropPlaylist()

        respondPublic { embed { description = "재생 목록의 음원을 모두 지웠어요." } }
    }

    private fun getLink(guild: GuildBehavior, channel: MessageChannelBehavior): Link {
        return linkMap[guild.id] ?: guild.getLink(link).apply {
            player.on<TrackStartEvent> {
                val currentPlayingTrack = player.currentPlayingTrack ?: return@on

                channel.createMessage {
                    embed {
                        author { name = "다음 음원을 재생할게요." }

                        description =
                            "[${currentPlayingTrack.track.info.title}](${currentPlayingTrack.track.info.uri}) - ${currentPlayingTrack.regUserMention}"

                        thumbnail { url = "${currentPlayingTrack.track.info.artworkUrl}" }
                    }
                }
            }

            player.on<TrackEndEvent> {
                if (player.hasNext()) {
                    player.playNext()
                    return@on
                }

                channel.createMessage { embed { description = "음원 재생이 끝났어요." } }
                player.clear()
                disconnectAudio()
            }

            linkMap[guild.id] = this
        }
    }
}