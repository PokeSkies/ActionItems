package com.pokeskies.actionitems.item.actions.types

import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class GiveXP(
    requirements: RequirementOptions? = RequirementOptions(),
    private val amount: Int = 0,
    private val level: Boolean = false
) : Action(ActionType.GIVE_XP, requirements) {
    override fun executeAction(player: ServerPlayer) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")
        if (level) {
            player.giveExperienceLevels(amount)
        } else {
            player.giveExperiencePoints(amount)
        }
    }

    override fun toString(): String {
        return "GiveXP(requirements=$requirements, amount=$amount, level=$level)"
    }
}
