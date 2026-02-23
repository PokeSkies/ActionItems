package com.pokeskies.actionitems.item

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

class ActionItem(
    val display: DisplayItem = DisplayItem(),
    val actions: List<Action> = emptyList(),
    val requirements: RequirementOptions? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val cooldown: Int = 30,
    val limit: Int = 0,
    val consume: Boolean = true,
    @SerializedName("random_actions")
    val randomActions: RandomActions? = null,
    // Will return one day when I figure out left click detection!
//    @SerializedName("click_types", alternate = ["click_type"])
//    val clickTypes: List<ClickType> = emptyList(),
) {
    lateinit var id: String

    fun hasRequirements(player: ServerPlayer): Boolean {
        if (requirements?.checkRequirements(player) == false) {
            requirements.executeDenyActions(player)
            return false
        }
        requirements?.executeSuccessActions(player)
        return true
    }

    fun execute(player: ServerPlayer) {
        executeActions(player)
        executeRandomActions(player)
    }

    private fun executeActions(player: ServerPlayer) {
        actions.forEach { action -> action.executeAction(player) }
    }

    private fun executeRandomActions(player: ServerPlayer) {
        randomActions?.executeRandom(player)
    }

    fun createItemStack(player: ServerPlayer): ItemStack {
        val stack = display.createItemStack(player)
        val dataComponents = DataComponentPatch.builder()
        val tag = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: CompoundTag()
        tag.putString(ActionItems.MOD_ID, id)
        dataComponents.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
        stack.applyComponents(dataComponents.build())
        return stack
    }
}
