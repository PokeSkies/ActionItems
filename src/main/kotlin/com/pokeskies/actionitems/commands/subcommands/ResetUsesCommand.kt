package com.pokeskies.actionitems.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.ItemManager
import com.pokeskies.actionitems.config.ConfigManager
import com.pokeskies.actionitems.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

class ResetUsesCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("resetuses")
            .requires(Permissions.require("${ActionItems.MOD_ID}.command.resetuses", 2))
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("id", StringArgumentType.string())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.ITEMS.keys.stream(), builder)
                    }
                    .executes { ctx ->
                        execute(
                            ctx,
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx,  "id"),
                        )
                    }
                )
                .executes { ctx ->
                    execute(
                        ctx,
                        EntityArgument.getPlayer(ctx, "player")
                    )
                }
            )
            .build()
    }

    companion object {
        fun execute(
            ctx: CommandContext<CommandSourceStack>,
            player: ServerPlayer,
            id: String? = null,
        ): Int {
            if (id != null) {
                val actionItem = ItemManager.getActionItem(id)
                if (actionItem == null) {
                    ctx.source.sendMessage(
                        Component.text("Could not find an action item with the ID $id!").color(NamedTextColor.RED)
                    )
                    return 0
                }
            }

            if (!ItemManager.resetUses(player, id)) {
                ctx.source.sendMessage(
                    Component.text("Failed to reset uses for player ${player.name.string}!")
                        .color(NamedTextColor.RED)
                )
                return 0
            }

            ctx.source.sendMessage(
                Component.text("Successfully reset uses for player ${player.name.string}!")
                    .color(NamedTextColor.GREEN)
            )
            return 1
        }
    }
}
