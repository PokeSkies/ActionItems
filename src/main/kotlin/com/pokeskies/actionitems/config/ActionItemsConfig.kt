package com.pokeskies.actionitems.config

import com.google.gson.annotations.SerializedName

class ActionItemsConfig(
    var debug: Boolean = false,
    @SerializedName("interaction_cooldown")
    var interactionCooldown: Long = 250,
    var storage: StorageOptions = StorageOptions()
) {
    override fun toString(): String {
        return "ActionItemsConfig(debug=$debug, storage=$storage)"
    }
}
