package com.pokeskies.actionitems.item.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandConsole(
    type: ActionType = ActionType.COMMAND_CONSOLE,
    requirements: RequirementOptions? = RequirementOptions(),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("commands",  alternate = ["command"])
    private val commands: List<String> = emptyList()
) : Action(type, requirements) {
    override fun executeAction(player: ServerPlayer) {
        val parsedCommands = commands.map { ActionItems.INSTANCE.parsePlaceholders(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            ActionItems.INSTANCE.server.commands.performPrefixedCommand(
                ActionItems.INSTANCE.server.createCommandSourceStack(),
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandConsole(requirements=$requirements, commands=$commands)"
    }
}
