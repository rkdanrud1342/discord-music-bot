package com.rusine

import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction

suspend fun GuildChatInputCommandInteraction.getMember(): Member = user.asMember(guildId)