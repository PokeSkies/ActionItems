package com.pokeskies.actionitems.item.actions

import com.google.gson.*
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class Action(
    var type: ActionType,
    val requirements: RequirementOptions? = RequirementOptions()
) {
    abstract fun executeAction(player: ServerPlayer)

    internal class Adapter : JsonSerializer<Action>, JsonDeserializer<Action> {
        override fun serialize(src: Action, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ActionType = ActionType.valueOfAnyCase(value) ?: run {
                throw JsonParseException("Unknown Action Type: $value. Valid Types are: ${ActionType.entries.joinToString { it.name }}")
            }

            if (!type.areRequiredModsInstalled()) {
                throw JsonParseException("The Action Type is not supported without the following mods installed: ${type.requiredMods.joinToString()}")
            }

            return try {
                val result: Action = context.deserialize(json, type.clazz)
                result.type = type
                result
            } catch (e: NullPointerException) {
                throw JsonParseException("Error while deserialize Action Type: $value", e)
            }
        }
    }

    override fun toString(): String {
        return "Action(type=$type, requirements=$requirements)"
    }
}
