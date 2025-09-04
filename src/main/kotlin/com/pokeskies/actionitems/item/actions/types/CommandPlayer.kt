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

class CommandPlayer(
    type: ActionType = ActionType.COMMAND_PLAYER,
    requirements: RequirementOptions? = RequirementOptions(),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("commands",  alternate = ["command"])
    private val commands: List<String> = emptyList(),
    @SerializedName("permission_level")
    private val permissionLevel: Int? = null
) : Action(type, requirements) {
    override fun executeAction(player: ServerPlayer) {
        val parsedCommands = commands.map { ActionItems.INSTANCE.parsePlaceholders(player, it) }

        var source = player.createCommandSourceStack()
        if (permissionLevel != null) {
            source = source.withPermission(permissionLevel)
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            ActionItems.INSTANCE.server.commands.performPrefixedCommand(
                source,
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandPlayer(requirements=$requirements, commands=$commands)"
    }
}
