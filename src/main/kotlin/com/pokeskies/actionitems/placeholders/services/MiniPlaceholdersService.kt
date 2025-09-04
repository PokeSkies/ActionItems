package com.pokeskies.actionitems.placeholders.services

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.placeholders.IPlaceholderService
import com.pokeskies.actionitems.utils.Utils
import io.github.miniplaceholders.api.MiniPlaceholders
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.server.level.ServerPlayer

class MiniPlaceholdersService : IPlaceholderService {
    private val miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder().build())
        .build()

    init {
        Utils.printInfo("MiniPlaceholders mod found! Enabling placeholder integration...")
    }

    override fun parsePlaceholders(player: ServerPlayer, text: String): String {
        val resolver = TagResolver.resolver(
            MiniPlaceholders.getGlobalPlaceholders(),
            MiniPlaceholders.getAudiencePlaceholders(player)
        )

        return ActionItems.INSTANCE.adventure!!.toNative(
            miniMessage.deserialize(text, resolver)
        ).string
    }
}
