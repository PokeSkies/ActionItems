package com.pokeskies.actionitems.commands.subcommands

import com.mojang.brigadier.arguments.IntegerArgumentType
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

class GiveCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("give")
            .requires(Permissions.require("${ActionItems.MOD_ID}.command.give", 2))
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggest(ConfigManager.ITEMS.keys.stream(), builder)
                }
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx ->
                            execute(
                                ctx,
                                StringArgumentType.getString(ctx,  "id"),
                                EntityArgument.getPlayers(ctx, "player"),
                                IntegerArgumentType.getInteger(ctx, "amount")
                            )
                        }
                    )
                    .executes { ctx ->
                        execute(
                            ctx,
                            StringArgumentType.getString(ctx,  "id"),
                            EntityArgument.getPlayers(ctx, "player")
                        )
                    }
                )
                .executes { ctx ->
                    executeSelf(
                        ctx,
                        StringArgumentType.getString(ctx,  "id")
                    )
                }
            )
            .build()
    }

    companion object {
        fun executeSelf(
            ctx: CommandContext<CommandSourceStack>,
            id: String
        ): Int {
            val player = ctx.source.player
            if (player == null) {
                ctx.source.sendMessage(
                    Component.text("You must be a player to run this command or provide a player argument!").color(NamedTextColor.RED)
                )
                return 0
            }

            return execute(ctx, id, listOf(player))
        }

        fun execute(
            ctx: CommandContext<CommandSourceStack>,
            id: String,
            players: Collection<ServerPlayer>,
            amount: Int = 1
        ): Int {
            val actionItem = ItemManager.getActionItem(id)
            if (actionItem == null) {
                ctx.source.sendMessage(
                    Component.text("Could not find an action item with the ID $id!").color(NamedTextColor.RED)
                )
                return 0
            }

            for (player in players) {
                player.inventory.placeItemBackInInventory(actionItem.createItemStack(player).copyWithCount(amount))
            }

            ctx.source.sendMessage(
                Component.text("Gave ${players.size} player(s) $amount of action item $id.")
                    .color(NamedTextColor.GREEN)
            )
            return 1
        }
    }
}
