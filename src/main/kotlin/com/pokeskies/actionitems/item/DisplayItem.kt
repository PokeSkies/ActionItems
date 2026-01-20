package com.pokeskies.actionitems.item

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.placeholders.PlaceholderManager
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
import net.minecraft.network.chat.Style
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
    fun createItemStack(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack {
        val stack = getBaseItem(player, placeholders) ?: return ItemStack(Items.AIR)

        if (components != null) {
            // Parses the nbt and attempts to replace any placeholders
            val nbtCopy = components.copy()
            for (key in components.allKeys) {
                val element = components.get(key)
                if (element != null) {
                    if (element is StringTag) {
                        nbtCopy.putString(key, element.asString)
                    } else if (element is ListTag) {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(entry.asString))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        nbtCopy.put(key, parsed)
                    }
                }
            }

            DataComponentPatch.CODEC.decode(ActionItems.INSTANCE.nbtOpts, nbtCopy).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (customModelData != null) {
            dataComponents.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(customModelData))
        }

        name?.let { name ->
            dataComponents.set(
                DataComponents.ITEM_NAME, TextUtils.parseAllNative(player, name, placeholders))
        }

        if (lore.isNotEmpty()) {
            val parsedLore: MutableList<String> = mutableListOf()
            for (line in lore.stream().map { it }.toList()) {
                val parsedLine = PlaceholderManager.parse(player, line, placeholders)
                if (parsedLine.contains("\n")) {
                    parsedLine.split("\n").forEach { parsedLore.add(it) }
                } else {
                    parsedLore.add(parsedLine)
                }
            }
            dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative(it)) as Component
            }.toList()))
        }

        stack.applyComponents(dataComponents.build())

        return stack
    }

    private fun getBaseItem(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack? {
        if (item.isEmpty()) return null

        val parsedItem = PlaceholderManager.parse(player, item, placeholders)

        // Handles player head parsing
        if (parsedItem.startsWith("playerhead", true)) {
            val headStack = ItemStack(Items.PLAYER_HEAD)

            var uuid: UUID? = null
            if (parsedItem.contains("-")) {
                val arg = parsedItem.replace("playerhead-", "")
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
                        headStack.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.PROFILE, ResolvableProfile(Optional.empty(), Optional.empty(), properties))
                            .build())
                        return headStack
                    }
                }
            } else {
                // CASE: Only "playerhead" is provided, use the viewing player's UUID
                uuid = player.uuid
            }

            if (uuid != null) {
                val gameProfile = ActionItems.INSTANCE.server.profileCache?.get(uuid)
                if (gameProfile != null && gameProfile.isPresent) {
                    headStack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile(gameProfile.get()))
                        .build())
                    return headStack
                }
            }

            Utils.printError("Error while attempting to parse Player Head: $parsedItem")
            return headStack
        }

        val newItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(parsedItem))
        if (newItem.isEmpty) {
            Utils.printError("Error while getting Item, defaulting to AIR: $parsedItem")
            return ItemStack(Items.AIR)
        }

        return ItemStack(newItem.get())
    }

    override fun toString(): String {
        return "DisplayItem(item='$item', name=$name, lore=$lore, components=$components, " +
                "customModelData=$customModelData)"
    }
}
