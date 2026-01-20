package com.pokeskies.actionitems.utils

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.placeholders.PlaceholderManager
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TextUtils {
    fun toNative(text: String): Component {
        return ActionItems.INSTANCE.adventure.toNative(toComponent(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return ActionItems.MINI_MESSAGE.deserialize(text)
    }

    fun parseAllNative(player: ServerPlayer, text: String, additionalPlaceholders: Map<String, String> = emptyMap()): Component {
        return toNative(
            PlaceholderManager.parse(player, text, additionalPlaceholders)
        )
    }
}
