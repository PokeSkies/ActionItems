package com.pokeskies.actionitems.item.actions

import com.pokeskies.actionitems.item.actions.types.*
import net.fabricmc.loader.api.FabricLoader

enum class ActionType(
    val identifier: String,
    val clazz: Class<*>,
    val requiredMods: List<String> = emptyList(),
    val aliases: List<String> = emptyList()
) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java),
    MESSAGE("message", MessagePlayer::class.java),
    BROADCAST("broadcast", MessageBroadcast::class.java),
    PLAY_SOUND("play_sound", PlaySound::class.java, aliases = listOf("playsound")),
    GIVE_XP("give_xp", GiveXP::class.java),
    CURRENCY_DEPOSIT("currency_deposit", CurrencyDeposit::class.java),
    CURRENCY_WITHDRAW("currency_withdraw", CurrencyWithdraw::class.java),
    CURRENCY_SET("currency_set", CurrencySet::class.java),
    GIVE_ITEM("give_item", GiveItem::class.java),
    TAKE_ITEM("take_item", TakeItem::class.java),

    // External
    MOLANG("molang", MolangAction::class.java, requiredMods = listOf("cobblemon"));

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
        fun valueOfAnyCase(name: String): ActionType? {
            for (type in entries) {
                if (type.isIdentifier(name)) return type
            }
            return null
        }
    }
}
