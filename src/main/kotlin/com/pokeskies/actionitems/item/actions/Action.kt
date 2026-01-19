package com.pokeskies.actionitems.item.actions

import com.pokeskies.actionitems.item.requirements.RequirementOptions
import net.minecraft.server.level.ServerPlayer

abstract class Action(
    val type: ActionType,
    val requirements: RequirementOptions? = RequirementOptions()
) {
    abstract fun executeAction(player: ServerPlayer)

    override fun toString(): String {
        return "Action(type=$type, requirements=$requirements)"
    }
}
