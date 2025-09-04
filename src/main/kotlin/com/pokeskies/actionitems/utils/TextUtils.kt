package com.pokeskies.actionitems.utils

import com.pokeskies.actionitems.ActionItems
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return ActionItems.INSTANCE.adventure.toNative(ActionItems.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return ActionItems.MINI_MESSAGE.deserialize(text)
    }
}
