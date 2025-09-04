package com.pokeskies.actionitems.storage.file

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.config.ConfigManager
import com.pokeskies.actionitems.data.UserData
import com.pokeskies.actionitems.storage.IStorage
import java.util.*
import java.util.concurrent.CompletableFuture

class FileStorage : IStorage {
    private var fileData: FileData = ConfigManager.loadFile(STORAGE_FILENAME, FileData(), "", true)

    companion object {
        private const val STORAGE_FILENAME = "storage.json"
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = fileData.userdata[uuid]
        return userData ?: UserData(uuid)
    }

    override fun saveUser(userData: UserData): Boolean {
        fileData.userdata[userData.uuid] = userData
        return ConfigManager.saveFile(STORAGE_FILENAME, fileData)
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            getUser(uuid)
        }, ActionItems.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(userData)
        }, ActionItems.INSTANCE.asyncExecutor)
    }
}
