package com.pokeskies.actionitems.config

import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.actionitems.ActionItems
import com.pokeskies.actionitems.item.ActionItem
import com.pokeskies.actionitems.utils.Utils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

object ConfigManager {
    private var assetPackage = "assets/${ActionItems.MOD_ID}"

    lateinit var CONFIG: ActionItemsConfig
    var ITEMS: MutableMap<String, ActionItem> = mutableMapOf()

    fun load() {
        // Load defaulted configs if they do not exist
        copyDefaults()

        // Load all files
        CONFIG = loadFile("config.json", ActionItemsConfig())

        loadItems()
    }

    private fun copyDefaults() {
        val classLoader = ActionItems::class.java.classLoader

        ActionItems.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
        attemptDefaultDirectoryCopy(classLoader, "items")
    }

    fun <T : Any> loadFile(filename: String, default: T, path: String = "", create: Boolean = false): T {
        var dir = ActionItems.INSTANCE.configDir
        if (path.isNotEmpty()) {
            dir = dir.resolve(path)
        }
        val file = File(dir, filename)
        var value: T = default
        try {
            Files.createDirectories(ActionItems.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = ActionItems.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(ActionItems.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = ActionItems.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(ActionItems.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = ActionItems.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            file.mkdirs()
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = ActionItems.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.forEach { sourceFile ->
                        val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                        if (Files.isDirectory(sourceFile)) {
                            // Create subdirectories in the destination
                            destinationFile.mkdirs()
                        } else {
                            // Copy files to the destination
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }

    private fun loadItems() {
        ITEMS.clear()

        val dir = ActionItems.INSTANCE.configDir.resolve("items")
        if (dir.exists() && dir.isDirectory) {
            val filePaths = Files.walk(dir.toPath())
                .filter { p: Path -> p.toString().endsWith(".json") }
                .collect(Collectors.toList())

            for (filePath in filePaths) {
                val file = filePath.toFile()
                if (file.isFile) {
                    val id = file.name.substring(0, file.name.lastIndexOf(".json"))
                    val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                    try {
                        ActionItems.INSTANCE.gson.fromJson(JsonParser.parseReader(jsonReader), ActionItem::class.java).let {
                            it.id = id
                            ITEMS[id] = it
                        }
                        Utils.printInfo("Successfully read and loaded the Action Item file ${file.name}!")
                    } catch (ex: Exception) {
                        Utils.printError("Error while trying to parse the file ${file.name} as a Action Item!")
                        ex.printStackTrace()
                    }
                }
            }
        } else {
            Utils.printError("The `items` directory either does not exist or is not a directory!")
        }
    }
}
