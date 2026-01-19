package com.pokeskies.actionitems.item.actions.types

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.economy.EconomyType
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CurrencyWithdraw(
    requirements: RequirementOptions? = RequirementOptions(),
    private val currency: String = "",
    private val amount: Double = 0.0,
    private val economy: EconomyType? = null
) : Action(ActionType.CURRENCY_WITHDRAW, requirements) {
    override fun executeAction(player: ServerPlayer) {
        val service = ActionItems.INSTANCE.getEconomyServiceOrDefault(economy)
        if (service == null) {
            Utils.printError("[ACTION - CURRENCY_WITHDRAW] No Economy Service could be found from '$economy'! Valid services are: ${ActionItems.INSTANCE.getLoadedEconomyServices().keys}")
            return
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")

        service.withdraw(player, amount, currency)
    }

    override fun toString(): String {
        return "CurrencyWithdraw(requirements=$requirements, currency=$currency, amount=$amount)"
    }
}
