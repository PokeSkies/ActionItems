package com.pokeskies.actionitems.item

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.utils.RandomCollection
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type
import kotlin.random.Random

class RandomActions(
    val min: Int = 1,
    val max: Int = 1,
    @SerializedName("allow_duplicates")
    val allowDuplicates: Boolean = false,
    val actions: List<RandomEntry> = emptyList()
) {
    fun executeRandom(player: ServerPlayer) {
        if (actions.isEmpty()) return

        val minSafe = min.coerceAtLeast(0)
        val maxSafe = max.coerceAtLeast(minSafe)
        val amount = if (maxSafe == minSafe) minSafe else Random.nextInt(minSafe, maxSafe + 1)

        val entries = actions.toMutableList()
        repeat(amount) {
            if (entries.isEmpty()) return

            val entryIndex = getRandomEntryIndexed(entries)
            val entry = entries[entryIndex]

            entry.execute(player)

            if (!allowDuplicates) {
                entries.removeAt(entryIndex)
            }
        }
    }

    // Get as indexes so we can remove entries if duplicates aren't allowed
    private fun getRandomEntryIndexed(list: List<RandomEntry>): Int {
        val rc = RandomCollection<Int>()
        for ((i, e) in list.withIndex()) {
            if (e.weight > 0.0) rc.add(e.weight, i)
        }

        return if (rc.size() == 0) {
            Utils.printError("Total weight of Random Actions is <= 0 or weights are non-positive. Falling back to a complete random pick.")
            Random.nextInt(list.size)
        } else rc.next()
    }

    class Adapter : JsonDeserializer<RandomActions> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RandomActions {
            val obj = json.asJsonObject

            val min = if (obj.has("min")) obj.get("min").asInt else 1
            val max = if (obj.has("max")) obj.get("max").asInt else min
            val allow = if (obj.has("allow_duplicates")) obj.get("allow_duplicates").asBoolean else false

            val arr = obj.getAsJsonArray("actions")

            val entries = mutableListOf<RandomEntry>()
            for (elem in arr) {
                entries.add(parseEntry(elem, context))
            }

            return RandomActions(min, max, allow, entries)
        }

        private fun parseEntry(elem: JsonElement, context: JsonDeserializationContext): RandomEntry {
            val obj = elem.asJsonObject
            val weight = if (obj.has("weight")) obj.get("weight").asDouble else 1.0

            if (obj.has("actions")) {
                val arr = obj.getAsJsonArray("actions")
                val actions = mutableListOf<Action>()
                for (entry in arr) {
                    actions.add(
                        try {
                            context.deserialize<Action>(entry, Action::class.java)
                        } catch (ex: Exception) {
                            throw JsonParseException("Failed to parse grouped action entry in random_actions: ${ex.message}", ex)
                        }
                    )
                }
                return RandomEntry(weight, actions)
            }

            val action = try {
                context.deserialize<Action>(elem, Action::class.java)
            } catch (ex: Exception) {
                throw JsonParseException("Failed to parse action entry action in random_actions: ${ex.message}", ex)
            }
            return RandomEntry(weight, listOf(action))
        }
    }
}

data class RandomEntry(
    val weight: Double = 1.0,
    val actions: List<Action> = emptyList(),
) {
    fun execute(player: ServerPlayer) {
        for (action in actions) {
            action.executeAction(player)
        }
    }
}
