package com.pokeskies.actionitems.item.requirements.types.internal

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.economy.EconomyType
import com.pokeskies.actionitems.item.requirements.ComparisonType
import com.pokeskies.actionitems.item.requirements.Requirement
import com.pokeskies.actionitems.item.requirements.RequirementType
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CurrencyRequirement(
    type: RequirementType = RequirementType.CURRENCY,
    comparison: ComparisonType = ComparisonType.GREATER_THAN_OR_EQUALS,
    private val currency: String = "",
    private val amount: Double = 0.0,
    private val economy: EconomyType? = null
) : Requirement(type, comparison) {
    override fun checkRequirements(player: ServerPlayer): Boolean {
        if (!checkComparison()) return false

        val service = ActionItems.INSTANCE.getEconomyServiceOrDefault(economy)
        if (service == null) {
            Utils.printError("[REQUIREMENT - ${type?.name}] No Economy Service could be found from '$economy'! Valid services are: ${ActionItems.INSTANCE.getLoadedEconomyServices().keys}")
            return false
        }

        val balance = service.balance(player, currency)

        Utils.printDebug("[REQUIREMENT - ${type?.name}] Player(${player.gameProfile.name}), Player Balance($balance): $this")

        return when (comparison) {
            ComparisonType.EQUALS -> balance == amount
            ComparisonType.NOT_EQUALS -> balance != amount
            ComparisonType.GREATER_THAN -> balance > amount
            ComparisonType.LESS_THAN -> balance < amount
            ComparisonType.GREATER_THAN_OR_EQUALS -> balance >= amount
            ComparisonType.LESS_THAN_OR_EQUALS -> balance <= amount
        }
    }

    override fun allowedComparisons(): List<ComparisonType> {
        return ComparisonType.entries
    }

    override fun toString(): String {
        return "CurrencyRequirement(comparison=$comparison, currency='$currency', amount=$amount, economy=$economy)"
    }
}
