package com.pokeskies.actionitems.item.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.economy.EconomyType
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.TextUtils
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    type: ActionType = ActionType.BROADCAST,
    requirements: RequirementOptions? = RequirementOptions(),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("message",  alternate = ["messages"])
    private val message: List<String> = emptyList()
) : Action(type, requirements) {
    override fun executeAction(player: ServerPlayer) {
        if (ActionItems.INSTANCE.adventure == null) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.gameProfile.name}: Adventure is null")
            return
        }

        val parsedMessages = message.map { ActionItems.INSTANCE.parsePlaceholders(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")

        for (line in parsedMessages) {
            ActionItems.INSTANCE.adventure!!.all().sendMessage(TextUtils.toNative(line))
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(requirements=$requirements, message=$message)"
    }
}
