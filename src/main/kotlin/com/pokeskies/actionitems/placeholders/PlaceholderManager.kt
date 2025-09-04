package com.pokeskies.actionitems.placeholders

import com.pokeskies.actionitems.placeholders.services.DefaultPlaceholderService
import com.pokeskies.actionitems.placeholders.services.ImpactorPlaceholderService
import com.pokeskies.actionitems.placeholders.services.MiniPlaceholdersService
import com.pokeskies.actionitems.placeholders.services.PlaceholderAPIService
import net.minecraft.server.level.ServerPlayer

class PlaceholderManager {
    private val services: MutableList<IPlaceholderService> = mutableListOf()

    fun registerServices() {
        services.clear()
        services.add(DefaultPlaceholderService())
        for (service in PlaceholderMod.entries) {
            if (service.isModPresent()) {
                services.add(getServiceForType(service))
            }
        }
    }

    fun parse(player: ServerPlayer, text: String): String {
        var returnValue = text
        for (service in services) {
            returnValue = service.parsePlaceholders(player, returnValue)
        }
        return returnValue
    }

    private fun getServiceForType(placeholderMod: PlaceholderMod): IPlaceholderService {
        return when (placeholderMod) {
            PlaceholderMod.IMPACTOR -> ImpactorPlaceholderService()
            PlaceholderMod.PLACEHOLDERAPI -> PlaceholderAPIService()
            PlaceholderMod.MINIPLACEHOLDERS -> MiniPlaceholdersService()
        }
    }
}
