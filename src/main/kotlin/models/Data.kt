package com.rusine.models

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val token: String,
    val url: String,
    val password: String
)
