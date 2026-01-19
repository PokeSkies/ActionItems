package com.pokeskies.actionitems

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pokeskies.actionitems.commands.BaseCommand
import com.pokeskies.actionitems.config.ConfigManager
import com.pokeskies.actionitems.economy.EconomyType
import com.pokeskies.actionitems.economy.IEconomyService
import com.pokeskies.actionitems.item.actions.Action
import com.pokeskies.actionitems.item.actions.ActionType
import com.pokeskies.actionitems.item.requirements.Requirement
import com.pokeskies.actionitems.item.requirements.RequirementType
import com.pokeskies.actionitems.placeholders.PlaceholderManager
import com.pokeskies.actionitems.storage.IStorage
import com.pokeskies.actionitems.storage.StorageType
import com.pokeskies.actionitems.utils.CompoundTagAdaptor
import com.pokeskies.actionitems.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActionItems : ModInitializer {
    companion object {
        lateinit var INSTANCE: ActionItems

        var MOD_ID = "actionitems"
        var MOD_NAME = "ActionItems"

        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File
    var storage: IStorage? = null

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()
    lateinit var placeholderManager: PlaceholderManager

    val molangRuntime: MoLangRuntime = MoLangRuntime().setup()

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("ActionItems-Async-%d")
        .setDaemon(true)
        .build())

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Action::class.java, ActionType.Adapter())
        .registerTypeAdapter(Requirement::class.java, RequirementType.Adapter())
        .registerTypeAdapter(EconomyType::class.java, EconomyType.Adapter())
        .registerTypeAdapter(StorageType::class.java, StorageType.Adapter())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeAdapter(CompoundTag::class.java, CompoundTagAdaptor())
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
            this.storage = null
        }

        this.economyServices = IEconomyService.getLoadedEconomyServices()
        this.placeholderManager = PlaceholderManager()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            this.placeholderManager.registerServices()
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _: MinecraftServer ->
            ItemManager.load()
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
            this.storage = null
        }

        this.economyServices = IEconomyService.getLoadedEconomyServices()
    }

    fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return placeholderManager.parse(player, text)
    }

    fun getLoadedEconomyServices(): Map<EconomyType, IEconomyService> {
        return this.economyServices
    }

    fun getEconomyService(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] }
    }

    fun getEconomyServiceOrDefault(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] } ?: this.economyServices.values.firstOrNull()
    }
}
