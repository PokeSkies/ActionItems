package com.pokeskies.actionitems

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.pokeskies.actionitems.config.ConfigManager
import com.pokeskies.actionitems.data.UserData
import com.pokeskies.actionitems.item.ActionItem
import com.pokeskies.actionitems.utils.TextUtils
import com.pokeskies.actionitems.utils.Utils
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import java.util.*
import java.util.concurrent.TimeUnit

object ItemManager {
    private val dataCache: LoadingCache<UUID, UserData> = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build { key ->
            ActionItems.INSTANCE.storage?.getUser(key) ?: UserData(key)
        }

    private val interactionCache: MutableMap<UUID, Long> = mutableMapOf()

    fun load() {
        registerEvents()
    }

    fun isActionItem(itemStack: ItemStack): ActionItem? {
        val data = itemStack.components.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        if (!data.contains(ActionItems.MOD_ID)) return null
        val id = data.getString(ActionItems.MOD_ID)
        return getActionItem(id)
    }

    fun getActionItem(id: String): ActionItem? {
        return ConfigManager.ITEMS[id]
    }

    fun resetCooldown(player: ServerPlayer, id: String? = null): Boolean {
        val userData = dataCache.get(player.uuid)
        if (id == null) {
            if (userData.cooldowns.isNotEmpty()) {
                userData.cooldowns.clear()
            }
        } else {
            if (userData.cooldowns.containsKey(id)) {
                userData.cooldowns.remove(id)
            }
        }

        if (ActionItems.INSTANCE.storage?.saveUser(userData) != true) {
            Utils.printError("Failed to save user data for ${player.uuid} while attempting to reset cooldowns")
            return false
        }
        dataCache.put(player.uuid, userData)
        return true
    }

    fun resetUses(player: ServerPlayer, id: String? = null): Boolean {
        val userData = dataCache.get(player.uuid)
        if (id == null) {
            if (userData.uses.isNotEmpty()) {
                userData.uses.clear()
            }
        } else {
            if (userData.uses.containsKey(id)) {
                userData.uses.remove(id)
            }
        }

        if (ActionItems.INSTANCE.storage?.saveUser(userData) != true) {
            Utils.printError("Failed to save user data for ${player.uuid} while attempting to reset uses")
            return false
        }
        dataCache.put(player.uuid, userData)
        return true
    }

    private fun registerEvents() {
        UseItemCallback.EVENT.register(UseItemCallback { p, world, hand ->
            if (world.isClientSide) return@UseItemCallback InteractionResultHolder.pass(p.getItemInHand(hand))
            val player = p as ServerPlayer
            val itemStack = player.getItemInHand(hand)
            val actionItem = isActionItem(itemStack) ?: return@UseItemCallback InteractionResultHolder.pass(itemStack)

            if (interactionCache.contains(player.uuid) && interactionCache[player.uuid]!! > System.currentTimeMillis()) {
                return@UseItemCallback InteractionResultHolder.fail(itemStack)
            } else {
                interactionCache[player.uuid] = System.currentTimeMillis() + ConfigManager.CONFIG.interactionCooldown
            }

            val userData = dataCache.get(player.uuid)

            // If cooldowns or limits are set, we need to check user data
            if (actionItem.cooldown > 0) {
                userData.cooldowns[actionItem.id]?.let { cooldown ->
                    if (cooldown + (actionItem.cooldown * 1000) > System.currentTimeMillis()) {
                        player.sendSystemMessage(
                            TextUtils.toNative(
                                "<red>You must wait before using this item again.</red>"
                            )
                        )
                        return@UseItemCallback InteractionResultHolder.fail(itemStack)
                    }
                }
            }
            if (actionItem.limit > 0) {
                val usageCount = userData.uses[actionItem.id] ?: 0
                if (usageCount >= actionItem.limit) {
                    player.sendSystemMessage(
                        TextUtils.toNative(
                            "<red>You have reached the usage limit for this item.</red>"
                        )
                    )
                    return@UseItemCallback InteractionResultHolder.fail(itemStack)
                }
            }

            if (!actionItem.hasRequirements(player)) {
                return@UseItemCallback InteractionResultHolder.fail(itemStack)
            }

            // If cooldowns or limits are set, update user data
            var dataModified = false
            if (actionItem.cooldown > 0) {
                dataModified = true
                userData.cooldowns[actionItem.id] = System.currentTimeMillis()
            }

            if (actionItem.limit > 0) {
                dataModified = true
                val usageCount = userData.uses[actionItem.id] ?: 0
                userData.uses[actionItem.id] = usageCount + 1
            }

            if (dataModified) {
                if (ActionItems.INSTANCE.storage?.saveUser(userData) != true) {
                    Utils.printError("Failed to save user data for ${player.uuid} while attempting to use an Action Item ${actionItem.id}")
                    player.sendMessage(Component.text("Failed to save user data, please contact an admin!").color(NamedTextColor.RED))
                    return@UseItemCallback InteractionResultHolder.fail(itemStack)
                }
                dataCache.put(player.uuid, userData)
            }

            if (actionItem.consume) itemStack.shrink(1)
            actionItem.execute(player)

            return@UseItemCallback InteractionResultHolder.pass(itemStack)
        })
    }
}
