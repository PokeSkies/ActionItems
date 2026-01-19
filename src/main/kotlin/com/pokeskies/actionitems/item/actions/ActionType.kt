package com.pokeskies.actionitems.item.actions

import com.google.gson.*
import com.pokeskies.actionitems.item.actions.types.*
import net.fabricmc.loader.api.FabricLoader
import java.lang.reflect.Type


enum class ActionType(val identifier: String, val clazz: Class<*>, val aliases: List<String> = emptyList()) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java),
    MESSAGE("message", MessagePlayer::class.java),
    BROADCAST("broadcast", MessageBroadcast::class.java),
    PLAY_SOUND("play_sound", PlaySound::class.java, listOf("playsound")),
    GIVE_XP("give_xp", GiveXP::class.java),
    CURRENCY_DEPOSIT("currency_deposit", CurrencyDeposit::class.java),
    CURRENCY_WITHDRAW("currency_withdraw", CurrencyWithdraw::class.java),
    CURRENCY_SET("currency_set", CurrencySet::class.java),
    GIVE_ITEM("give_item", GiveItem::class.java),
    MOLANG("molang", MolangAction::class.java),
    TAKE_ITEM("take_item", TakeItem::class.java);

    fun isIdentifier(name: String): Boolean {
        if (name.equals(identifier, true)) return true
        if (aliases.any { name.equals(it, true) }) return true
        return false
    }

    companion object {
        fun valueOfAnyCase(name: String): ActionType? {
            for (type in entries) {
                if (type.isIdentifier(name)) return type
            }
            return null
        }
    }

    internal class Adapter : JsonSerializer<Action>, JsonDeserializer<Action> {
        override fun serialize(src: Action, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            if (value == "molang" && !FabricLoader.getInstance().isModLoaded("cobblemon")) {
                throw JsonParseException("Molang action is not supported without the Cobblemon mod")
            }
            val type: ActionType? = ActionType.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize action type: $value", e)
            }
        }
    }
}
