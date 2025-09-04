package com.pokeskies.actionitems.data;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;
import java.util.UUID;

public class UserData {
    @BsonProperty("_id")
    public UUID uuid;
    @BsonProperty
    public HashMap<String, Long> cooldowns;
    @BsonProperty
    public HashMap<String, Integer> uses;

    public UserData(UUID uuid) {
        this.uuid = uuid;
        this.cooldowns = new HashMap<>();
        this.uses = new HashMap<>();
    }

    public UserData(UUID uuid, HashMap<String, Long> cooldowns, HashMap<String, Integer> uses) {
        this.uuid = uuid;
        this.cooldowns = cooldowns;
        this.uses = uses;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", cooldowns=" + cooldowns +
                ", uses=" + uses +
                '}';
    }
}
