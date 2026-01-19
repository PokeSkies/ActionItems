package com.pokeskies.actionitems.item.actions.types

import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolve
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MolangAction(
    requirements: RequirementOptions? = RequirementOptions(),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val script: List<String> = listOf()
) : Action(ActionType.MOLANG, requirements) {
    override fun executeAction(player: ServerPlayer) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")
        ActionItems.INSTANCE.molangRuntime.resolve(
            script.asExpressionLike(),
            mapOf("player" to player.asMoLangValue())
        )
    }

    override fun toString(): String {
        return "MolangAction(requirements=$requirements, script=$script)"
    }
}
