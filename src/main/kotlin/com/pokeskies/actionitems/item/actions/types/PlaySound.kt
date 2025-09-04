package com.pokeskies.actionitems.item.actions.types

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.RequirementOptions
import com.pokeskies.actionitems.utils.FlexibleListAdaptorFactory
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class PlaySound(
    type: ActionType = ActionType.PLAYSOUND,
    requirements: RequirementOptions? = RequirementOptions(),
    private val sound: String = "",
    private val source: String? = null,
    private val volume: Float = 1.0F,
    private val pitch: Float = 1.0F
) : Action(type, requirements) {
    override fun executeAction(player: ServerPlayer) {
        if (sound.isEmpty()) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound ID was empty")
            return
        }

        val soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.parse(sound))

        var category = if (source == null) SoundSource.MASTER else SoundSource.entries.firstOrNull { it.name.equals(source, true) }
        if (category == null) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound Source '$source' was not found, defaulting to MASTER")
            category = SoundSource.MASTER
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), SoundEvent($soundEvent), Category($category): $this")

        player.server.executeIfPossible {
            player.playNotifySound(
                soundEvent,
                category,
                volume,
                pitch,
            )
        }
    }

    override fun toString(): String {
        return "PlaySound(requirements=$requirements, sound='$sound', source=$source, volume=$volume, pitch=$pitch)"
    }
}
