package com.pokeskies.actionitems.storage.database.sql

import com.google.gson.reflect.TypeToken
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.config.StorageOptions
import com.pokeskies.actionitems.data.UserData
import com.pokeskies.actionitems.storage.IStorage
import com.pokeskies.actionitems.storage.StorageType
import com.pokeskies.actionitems.storage.database.sql.providers.MySQLProvider
import com.pokeskies.actionitems.storage.database.sql.providers.SQLiteProvider
import java.lang.reflect.Type
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: StorageOptions) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
    private val cooldownMapType: Type = object : TypeToken<HashMap<String, Long>>() {}.type
    private val usesMapType: Type = object : TypeToken<HashMap<String, Int>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    userData.cooldowns = ActionItems.INSTANCE.gson.fromJson(result.getString("cooldowns"), cooldownMapType)
                    userData.uses = ActionItems.INSTANCE.gson.fromJson(result.getString("uses"), usesMapType)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(userData: UserData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid, `cooldowns`, `uses`) VALUES ('%s', '%s', '%s')",
                    userData.uuid.toString(),
                    ActionItems.INSTANCE.gson.toJson(userData.cooldowns),
                    ActionItems.INSTANCE.gson.toJson(userData.uses),
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                val result = getUser(uuid)
                result
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, ActionItems.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(userData)
        }, ActionItems.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
