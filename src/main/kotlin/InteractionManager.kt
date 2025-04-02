package com.rusine

import com.rusine.command.BasicCommand
import com.rusine.command.Command
import com.rusine.command.MusicCommand
import com.rusine.command.MusicCommandManager
import dev.kord.common.entity.DiscordApplicationCommand
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on

class InteractionManager(
    private val kord: Kord,
    private val musicCommandManager: MusicCommandManager
) {
    private val commandList = Command.makeCommandList()

    suspend fun start() {
        val registeredCommandList = kord.rest.interaction.getGlobalApplicationCommands(kord.selfId)

        val commandListToCreate = commandList.filter { command ->
            registeredCommandList.find { registered -> command.key == registered.name } == null
        }

        val commandListToDelete = registeredCommandList.filter { registered ->
            commandList.find { command -> command.key == registered.name } == null
        }

        commandListToCreate.forEach { command -> command.register() }
        commandListToDelete.forEach { registered -> registered.delete() }

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            val command = commandList.find { it.key == interaction.invokedCommandName }

            when (command) {
                is BasicCommand -> interaction.responseBasicCommand(command)
                is MusicCommand -> interaction.responseMusicCommand(command)
                else -> interaction.responseUnknownCommand()
            }
        }
    }

    private suspend fun Command.register() {
        kord.createGlobalChatInputCommand(
            name = key,
            description = description,
            builder = builder
        )
    }

    private suspend fun DiscordApplicationCommand.delete() {

        kord.rest.interaction.deleteGlobalApplicationCommand(kord.selfId, id)
    }

    private suspend fun GuildChatInputCommandInteraction.responseUnknownCommand() {
        respondEphemeral { content = "제가 모르는 명령어에요." }
    }

    private suspend fun GuildChatInputCommandInteraction.responseBasicCommand(basicCommand: BasicCommand) {
        when (basicCommand) {
            BasicCommand.PING -> respondEphemeral { content = "퐁!" }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.responseMusicCommand(musicCommand: MusicCommand) {
        musicCommandManager.responseCommand(musicCommand, this)
    }
}
