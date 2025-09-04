package com.pokeskies.actionitems.placeholders

import net.minecraft.server.level.ServerPlayer

interface IPlaceholderService {
    fun parsePlaceholders(player: ServerPlayer, text: String): String
}
