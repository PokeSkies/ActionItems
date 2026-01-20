package com.pokeskies.actionitems.item.requirements

import com.google.gson.*
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class Requirement(
    var type: RequirementType? = null,
    val comparison: ComparisonType = ComparisonType.EQUALS
) {
    abstract fun checkRequirements(player: ServerPlayer): Boolean

    open fun allowedComparisons(): List<ComparisonType> {
        return emptyList()
    }

    fun checkComparison(): Boolean {
        if (!allowedComparisons().contains(comparison)) {
            Utils.printError("Error while executing a $type Requirement check! Comparison ${comparison.identifier} is not allowed: ${allowedComparisons().map { it.identifier }}")
            return false
        }
        return true
    }

    internal class Adapter : JsonSerializer<Requirement>, JsonDeserializer<Requirement> {
        override fun serialize(src: Requirement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Requirement {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: RequirementType = RequirementType.valueOfAnyCase(value) ?: run {
                throw JsonParseException("Unknown Requirement Type: $value. Valid Types are: ${RequirementType.entries.joinToString { it.name }}")
            }

            if (!type.areRequiredModsInstalled()) {
                throw JsonParseException("The Requirement Type is not supported without the following mods installed: ${type.requiredMods.joinToString()}")
            }

            return try {
                val result: Requirement = context.deserialize(json, type.clazz)
                result.type = type
                result
            } catch (e: NullPointerException) {
                throw JsonParseException("Error while deserialize Requirement Type: $type", e)
            }
        }
    }

    override fun toString(): String {
        return "Requirement(type=$type, comparison=$comparison)"
    }
}
