package com.rusine

import com.rusine.command.MusicCommandManager
import com.rusine.models.Data
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.NON_PRIVILEGED
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.kord.lavakord
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.File


suspend fun main() {
    val serializer = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {}
    }

    val file = File("./app_info.txt")
    val data = serializer.decodeFromString<Data>(file.readText())

    val kord = Kord(data.token)

    val musicCommandManager = MusicCommandManager(
        kord.lavakord().apply {
            addNode(serverUri = data.url, password = data.password)
        }
    )

    val interactionManager = InteractionManager(kord, musicCommandManager)

    interactionManager.start()

    kord.login{
        @OptIn(PrivilegedIntent::class)
        intents = Intents.NON_PRIVILEGED + Intent.MessageContent
    }
}
