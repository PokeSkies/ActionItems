package com.pokeskies.actionitems.item

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.TextUtils
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

open class DisplayItem(
    val item: String = "",
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
    @SerializedName("custom_model_data")
    val customModelData: Int? = null,
) {
    fun createItemStack(viewer: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack {
        val stack = createBaseItem(viewer)

        if (components != null) {
            DataComponentPatch.CODEC.decode(ActionItems.INSTANCE.nbtOpts, parseNBT(viewer, components)).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (customModelData != null) {
            dataComponents.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(customModelData))
        }

        if (name != null)
            dataComponents.set(DataComponents.ITEM_NAME, TextUtils.toNative(replacePlaceholders(name, placeholders)))

        if (lore.isNotEmpty()) {
            val parsedLore: MutableList<String> = mutableListOf()
            for (line in lore.stream().map { it }.toList()) {
                if (line.contains("\n")) {
                    line.split("\n").forEach { parsedLore.add(it) }
                } else {
                    parsedLore.add(line)
                }
            }
            dataComponents.set(DataComponents.LORE, ItemLore(
                parsedLore.stream().map { line ->
                    Component.empty().withStyle { it.withItalic(false) }
                        .append(TextUtils.toNative(replacePlaceholders(line, placeholders)))
                }.toList() as List<Component>
            ))
        }

        stack.applyComponents(dataComponents.build())

        return stack
    }

    private fun createBaseItem(player: ServerPlayer): ItemStack {
        if (item.isEmpty()) return ItemStack(Items.BARRIER, 1)

        // Handles player head parsing
        if (item.startsWith("playerhead", true)) {
            val itemStack = ItemStack(Items.PLAYER_HEAD, 1)

            var uuid: UUID? = null
            if (item.contains("-")) {
                val arg = item.replace("playerhead-", "")
                if (arg.isNotEmpty()) {
                    if (arg.contains("-")) {
                        // CASE: UUID format
                        try {
                            uuid = UUID.fromString(arg)
                        } catch (_: Exception) {}
                    } else if (arg.length <= 16) {
                        // CASE: Player name format
                        val targetPlayer = ActionItems.INSTANCE.server.playerList?.getPlayerByName(arg)
                        if (targetPlayer != null) {
                            uuid = targetPlayer.uuid
                        }
                    } else {
                        // CASE: Game Profile format
                        val properties = PropertyMap()
                        properties.put("textures", Property("textures", arg))
                        itemStack.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.PROFILE, ResolvableProfile(Optional.empty(), Optional.empty(), properties))
                            .build())
                        return itemStack
                    }
                }
            } else {
                // CASE: Only "playerhead" is provided, use the viewing player's UUID
                uuid = player.uuid
            }

            if (uuid != null) {
                val gameProfile = ActionItems.INSTANCE.server.profileCache?.get(uuid)
                if (gameProfile != null && gameProfile.isPresent) {
                    itemStack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile(gameProfile.get()))
                        .build())
                    return itemStack
                }
            }

            Utils.printError("Error while attempting to parse Player Head: $item")
            return itemStack
        }

        val newItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(item))

        if (!newItem.isPresent) {
            Utils.printError("Error while getting Item, defaulting to Barrier: $item")
            return ItemStack(Items.BARRIER, 1)
        }

        return ItemStack(newItem.get(), 1)
    }

    private fun parseNBT(player: ServerPlayer, tag: CompoundTag): CompoundTag {
        val parsedNBT = tag.copy()
        for (key in parsedNBT.allKeys) {
            var element = parsedNBT.get(key)
            if (element != null) {
                when (element) {
                    is StringTag -> {
                        element = StringTag.valueOf(element.asString)
                    }
                    is ListTag -> {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(entry.asString))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        element = parsed
                    }
                    is CompoundTag -> {
                        element = parseNBT(player, element)
                    }
                }

                if (element != null) {
                    parsedNBT.put(key, element)
                }
            }
        }
        return parsedNBT
    }

    private fun replacePlaceholders(text: String, placeholders: Map<String, String>): String {
        var result = text
        placeholders.forEach { (key, value) ->
            result = result.replace(key, value)
        }
        return result
    }

    override fun toString(): String {
        return "DisplayItem(item='$item', name=$name, lore=$lore, components=$components, " +
                "customModelData=$customModelData)"
    }
}
