package com.pokeskies.actionitems.item.actions

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer
import kotlin.random.Random

abstract class Action(
    val type: ActionType,
    val requirements: RequirementOptions? = RequirementOptions()
) {
    abstract fun executeAction(player: ServerPlayer)

    override fun toString(): String {
        return "Action(type=$type, requirements=$requirements)"
    }
}
