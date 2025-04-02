package com.rusine.models

import dev.arbjerg.lavalink.protocol.v4.Track

data class TrackWrapper(
    val regUserMention: String,
    val track: Track
)