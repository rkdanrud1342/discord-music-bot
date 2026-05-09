package com.rusine

import com.rusine.command.MusicCommandManager
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.NON_PRIVILEGED
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.kord.lavakord


suspend fun main() {
    val token = System.getenv("DISCORD_TOKEN") ?: run {
        println("NO DISCORD_TOKEN Provided")
        return
    }

    val url = System.getenv("LAVA_LINK_URL") ?: run {
        println("NO LAVA_LINK_URL Provided")
        return
    }
    val pw = System.getenv("LAVA_LINK_PW") ?: run {
        println("NO LAVA_LINK_PW Provided")
        return
    }

    val kord = Kord(token)

    val musicCommandManager = MusicCommandManager(
        kord.lavakord().apply {
            addNode(serverUri = url, password = pw)
        }
    )

    val interactionManager = InteractionManager(kord, musicCommandManager)

    interactionManager.start()

    kord.login{
        @OptIn(PrivilegedIntent::class)
        intents = Intents.NON_PRIVILEGED + Intent.MessageContent
    }
}
