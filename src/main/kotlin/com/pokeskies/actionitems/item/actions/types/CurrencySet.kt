package com.pokeskies.actionitems.item.actions.types

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.economy.EconomyType
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CurrencySet(
    requirements: RequirementOptions? = RequirementOptions(),
    private val currency: String = "",
    private val amount: Double = 0.0,
    private val economy: EconomyType? = null
) : Action(ActionType.CURRENCY_SET, requirements) {
    override fun executeAction(player: ServerPlayer) {
        val service = ActionItems.INSTANCE.getEconomyServiceOrDefault(economy)
        if (service == null) {
            Utils.printError("[ACTION - CURRENCY_SET] No Economy Service could be found from '$economy'! Valid services are: ${ActionItems.INSTANCE.getLoadedEconomyServices().keys}")
            return
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")

        service.set(player, amount, currency)
    }

    override fun toString(): String {
        return "CurrencySet(requirements=$requirements, currency=$currency, amount=$amount)"
    }
}
