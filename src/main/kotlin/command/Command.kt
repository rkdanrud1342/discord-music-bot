package com.rusine.command

import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string

sealed class Command(
    val key : String,
    val description : String,
    val builder : ChatInputCreateBuilder.() -> Unit = {}
) {
    companion object {
        fun makeCommandList() : List<Command> = listOf(
            BasicCommand.PING,

            MusicCommand.LIST,
            MusicCommand.PLAY,
            MusicCommand.SKIP,
            MusicCommand.REMOVE,
            MusicCommand.DROP
        )
    }
}

sealed class BasicCommand(
    key : String,
    description : String
) : Command(key, description) {
    data object PING : BasicCommand("핑", "퐁해줘요.")
}

sealed class MusicCommand(
    key : String,
    description : String,
    builder: ChatInputCreateBuilder.() -> Unit = {}
) : Command(key, description, builder) {
    data object PLAY : MusicCommand(
        key = "틀어",
        description = "음원을 재생해요. 다른 음원이 재생중이라면 재생목록에 추가해요.",
        builder = {
            this.string(
                name = "query",
                description = "Youtube 영상 주소 또는 검색어를 입력해요.",
            )
        }
    )

    data object SKIP : MusicCommand(
        key = "스킵",
        description = "재생중인 음원을 건너뛰어요."
    )

    data object LIST : MusicCommand(
        key = "재생목록",
        description = "재생목록을 표시해요."
    )

    data object REMOVE : MusicCommand(
        key = "지워",
        description = "재생목록에서 음원을 삭제해요.",
        builder = {
            this.integer(
                name = "index",
                description = "지울 음원의 대기열 순번을 입력해요."
            )
        }
    )

    data object DROP : MusicCommand(
        key = "다지워",
        description = "재생목록에서 모든 음원을 삭제해요."
    )
}