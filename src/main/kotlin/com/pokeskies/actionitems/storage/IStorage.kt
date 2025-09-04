package com.pokeskies.actionitems.storage

import com.pokeskies.actionitems.storage.StorageType
import com.pokeskies.actionitems.config.StorageOptions
import com.pokeskies.actionitems.data.UserData
import com.pokeskies.actionitems.storage.database.MongoStorage
import com.pokeskies.actionitems.storage.database.sql.SQLStorage
import com.pokeskies.actionitems.storage.file.FileStorage
import java.util.*
import java.util.concurrent.CompletableFuture

interface IStorage {
    companion object {
        fun load(config: StorageOptions): IStorage {
            return when (config.type) {
                StorageType.JSON -> FileStorage()
                StorageType.MONGO -> MongoStorage(config)
                StorageType.MYSQL, StorageType.SQLITE -> SQLStorage(config)
            }
        }
    }

    fun getUser(uuid: UUID): UserData
    fun saveUser(userData: UserData): Boolean

    fun getUserAsync(uuid: UUID): CompletableFuture<UserData>
    fun saveUserAsync(userData: UserData): CompletableFuture<Boolean>

    fun close() {}
}
