package com.pokeskies.actionitems.item.requirements

import com.pokeskies.actionitems.item.requirements.types.external.MolangRequirement
import com.pokeskies.actionitems.item.requirements.types.internal.*
import net.fabricmc.loader.api.FabricLoader

enum class RequirementType(
    val identifier: String,
    val clazz: Class<*>,
    val requiredMods: List<String> = emptyList(),
    val aliases: List<String> = emptyList()
) {
    // Internal
    PERMISSION("permission", PermissionRequirement::class.java),
    ITEM("item", ItemRequirement::class.java),
    CURRENCY("currency", CurrencyRequirement::class.java),
    DIMENSION("dimension", DimensionRequirement::class.java),
    PLACEHOLDER("placeholder", PlaceholderRequirement::class.java),
    XP("xp", XPRequirement::class.java),
    ADVANCEMENT("advancement", AdvancementRequirement::class.java),

    // Extensions
    MOLANG("molang", MolangRequirement::class.java, requiredMods = listOf("cobblemon"));

    fun isIdentifier(name: String): Boolean {
        if (name.equals(identifier, true)) return true
        if (aliases.any { name.equals(it, true) }) return true
        return false
    }

    fun areRequiredModsInstalled(): Boolean {
        if (requiredMods.isEmpty()) return true
        for (mod in requiredMods) {
            if (!FabricLoader.getInstance().isModLoaded(mod)) {
                return false
            }
        }
        return true
    }

    companion object {
        fun valueOfAnyCase(name: String): RequirementType? {
            for (type in entries) {
                if (type.isIdentifier(name)) return type
            }
            return null
        }
    }
}
