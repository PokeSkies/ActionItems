package com.pokeskies.actionitems.storage.database.sql.providers

import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.config.StorageOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: StorageOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File(ActionItems.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
