package com.rusine

import com.rusine.models.TrackWrapper
import dev.schlaubi.lavakord.audio.player.Player

private val playlistMap = mutableMapOf<Player, MutableList<TrackWrapper>>()
private val currentPlayingTrackMap = mutableMapOf<Player, TrackWrapper>()

val Player.playlist: List<TrackWrapper>
    get() = playlistMap[this] ?: mutableListOf<TrackWrapper>().also { playlistMap[this] = it }

val Player.currentPlayingTrack: TrackWrapper?
    get() = currentPlayingTrackMap[this]

val Player.isPlaying: Boolean
    get() = currentPlayingTrackMap[this] != null

fun Player.register(trackWrapper: TrackWrapper) {
    playlistMap[this]?.add(trackWrapper) ?: mutableListOf(trackWrapper).also { playlistMap[this] = it }
}

fun Player.hasNext(): Boolean {
    val playlist = playlistMap[this] ?: return false
    return playlist.isNotEmpty()
}

suspend fun Player.playNext(): TrackWrapper? {
    // 플레이 리스트가 없으면 함수 종료
    val playlist = playlistMap[this] ?: return null

    // 다음 곡을 재생하기 위해 플레이리스트에서 꺼냄
    val trackWrapper = playlist.removeFirstOrNull() ?: return null

    currentPlayingTrackMap[this] = trackWrapper
    playTrack(trackWrapper.track)

    return trackWrapper
}

fun Player.deleteTrack(index: Int): TrackWrapper? {
    val playlist = playlistMap[this] ?: return null

    if (playlist.lastIndex < index) {
        return null
    }

    return playlist.removeAt(index)
}

fun Player.dropPlaylist() {
    playlistMap.remove(this)
}

fun Player.clear() {
    playlistMap.remove(this)
    currentPlayingTrackMap.remove(this)
}