package com.pokeskies.actionitems.placeholders.services

import com.pokeskies.actionitems.placeholders.IPlaceholderService
import net.minecraft.server.level.ServerPlayer

class DefaultPlaceholderService : IPlaceholderService {
    override fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return text
            .replace("%player%", player.name.string)
            .replace("%player_uuid%", player.uuid.toString())
            .replace("%player_dimension%", player.serverLevel().dimension().location().asString())
            .replace("%player_pos_x%", player.x.toString())
            .replace("%player_pos_y%", player.y.toString())
            .replace("%player_pos_z%", player.z.toString())
    }
}
