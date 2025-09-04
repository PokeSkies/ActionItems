package com.pokeskies.actionitems.item.requirements

import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.item.actions.Action
import net.minecraft.server.level.ServerPlayer

class RequirementOptions(
    val requirements: List<Requirement> = listOf(),
    @SerializedName("deny_actions")
    val denyActions: List<Action> = listOf(),
    @SerializedName("success_actions")
    val successActions: List<Action> = listOf(),
    @SerializedName("minimum_requirements")
    val minimumRequirements: Int? = null,
    @SerializedName("stop_at_success")
    val stopAtSuccess: Boolean = false
) {
    fun checkRequirements(player: ServerPlayer): Boolean {
        var successes = 0
        for (requirement in requirements) {
            if (requirement.checkRequirements(player)) {
                successes++
                if (minimumRequirements != null && stopAtSuccess && successes >= minimumRequirements) {
                    return true
                }
            }
        }
        return if (minimumRequirements == null) successes == requirements.size else successes >= minimumRequirements
    }

    fun executeDenyActions(player: ServerPlayer) {
        for (action in denyActions) {
            action.executeAction(player)
        }
    }

    fun executeSuccessActions(player: ServerPlayer) {
        for (action in successActions) {
            action.executeAction(player)
        }
    }

    override fun toString(): String {
        return "RequirementOptions(requirements=$requirements, denyActions=$denyActions, successActions=$successActions)"
    }
}
