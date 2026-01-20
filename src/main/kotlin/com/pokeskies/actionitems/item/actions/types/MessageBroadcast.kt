package com.pokeskies.actionitems.item.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.placeholders.PlaceholderManager
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.TextUtils
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    requirements: RequirementOptions? = RequirementOptions(),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("message",  alternate = ["messages"])
    private val message: List<String> = emptyList()
) : Action(ActionType.BROADCAST, requirements) {
    override fun executeAction(player: ServerPlayer) {
        val parsedMessages = message.map { PlaceholderManager.parse(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")

        for (line in parsedMessages) {
            ActionItems.INSTANCE.adventure.all().sendMessage(TextUtils.toNative(line))
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(requirements=$requirements, message=$message)"
    }
}
