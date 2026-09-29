/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import night.commands.CommandManager;
import night.events.EventHandler;
import night.gui.ClickGuiScreen;
import night.managers.AccountManager;
import night.managers.BoostManager;
import night.managers.ChatManager;
import night.managers.ConfigManager;
import night.managers.FontManager;
import night.managers.FriendManager;
import night.managers.ImageManager;
import night.managers.MacroManager;
import night.managers.NightUserManager;
import night.managers.PositionManager;
import night.managers.RenderManager;
import night.managers.RotationManager;
import night.managers.ServerManager;
import night.managers.TargetManager;
import night.managers.TaskManager;
import night.managers.WaypointManager;
import night.managers.WorldManager;
import night.modules.ModuleManager;
import night.pingbypass.PingBypassConfig;
import night.pingbypass.modules.PbModuleManager;
import night.pingbypass.modules.submodules.crystal.ServerAutoCrystal;
import night.pingbypass.modules.submodules.totem.ServerAutoTotem;
import night.pingbypass.protocol.PbCustomPayload;
import night.pingbypass.server.ProxyServer;
import night.pingbypass.server.ProxyServerTickListener;
import night.pingbypass.server.TransferRehook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Night
implements ModInitializer {
    public Night() {
        // Alek registers BlockFinder during Fabric main entrypoint construction.
        night.utils.BlockEntityInstanceTracker.initialize();
    }

    public static final String MOD_NAME = "Cheats MC";
    public static final String MOD_ID = "night";
    public static final String MOD_VERSION = "v26";
    public static final String MINECRAFT_VERSION = "26.2";
    public static final String GIT_HASH = "eeeb439b78";
    public static final String GIT_REVISION = "436";
    public static final String BUILD_DATE = "2026-09-17";
    public static final long UPTIME = System.currentTimeMillis();
    public static final EventHandler EVENT_HANDLER = new EventHandler();
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"Cheats MC");
    private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
    public static String UPDATE_STATUS = "none";
    private static final String VERSION_URL = "";
    private static final String SECRET_KEY = "";
    public static ChatManager CHAT_MANAGER;
    public static FontManager FONT_MANAGER;
    public static FriendManager FRIEND_MANAGER;
    public static WorldManager WORLD_MANAGER;
    public static PositionManager POSITION_MANAGER;
    public static RotationManager ROTATION_MANAGER;
    public static ServerManager SERVER_MANAGER;
    public static RenderManager RENDER_MANAGER;
    public static TargetManager TARGET_MANAGER;
    public static MacroManager MACRO_MANAGER;
    public static TaskManager TASK_MANAGER;
    public static WaypointManager WAYPOINT_MANAGER;
    public static BoostManager BOOST_MANAGER;
    public static NightUserManager NIGHT_USER_MANAGER;
    public static ImageManager IMAGE_MANAGER;
    public static ModuleManager MODULE_MANAGER;
    public static CommandManager COMMAND_MANAGER;
    public static ConfigManager CONFIG_MANAGER;
    public static AccountManager ACCOUNT_MANAGER;
    public static ClickGuiScreen CLICK_GUI;
    public static PingBypassConfig PINGBYPASS_CONFIG;
    public static ProxyServer PROXY_SERVER;
    public static final PbModuleManager PB_MODULE_MANAGER;

    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
        try {
            PayloadTypeRegistry.serverboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(PbCustomPayload.ID, (payload, context) -> {});
        }
        catch (Throwable t) {
            LOGGER.warn("Fabric Networking API not available or optional: {}", (Object)t.getMessage());
        }
        try {
            PINGBYPASS_CONFIG = new PingBypassConfig(FabricLoader.getInstance().getGameDir());
            PINGBYPASS_CONFIG.load();
            LOGGER.info("PingBypass mode: {}", (Object)(PINGBYPASS_CONFIG.isServer() ? "server" : "client"));
            if (PINGBYPASS_CONFIG.isServer()) {
                try {
                    PROXY_SERVER = new ProxyServer(PINGBYPASS_CONFIG);
                    PROXY_SERVER.bind(InetAddress.getByName(PINGBYPASS_CONFIG.getIp()), PINGBYPASS_CONFIG.getPort());
                    EVENT_HANDLER.subscribe(new ProxyServerTickListener(PROXY_SERVER));
                    new TransferRehook();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (PROXY_SERVER != null) {
                            PROXY_SERVER.shutdown();
                        }
                    }, "PingBypass-Shutdown"));
                    LOGGER.info("PingBypass proxy server started on {}:{}", (Object)PINGBYPASS_CONFIG.getIp(), (Object)PINGBYPASS_CONFIG.getPort());
                }
                catch (IOException e) {
                    LOGGER.error("Failed to start PingBypass proxy server", (Throwable)e);
                }
            }
        }
        catch (Throwable t) {
            LOGGER.error("Failed to initialize PingBypass config/server", t);
        }
        Night.initManagers();
    }

    public static synchronized void initManagers() {
        if (CHAT_MANAGER != null) {
            return;
        }
        CHAT_MANAGER = new ChatManager();
        FONT_MANAGER = new FontManager();
        FRIEND_MANAGER = new FriendManager();
        WORLD_MANAGER = new WorldManager();
        POSITION_MANAGER = new PositionManager();
        ROTATION_MANAGER = new RotationManager();
        SERVER_MANAGER = new ServerManager();
        RENDER_MANAGER = new RenderManager();
        TARGET_MANAGER = new TargetManager();
        MACRO_MANAGER = new MacroManager();
        TASK_MANAGER = new TaskManager();
        WAYPOINT_MANAGER = new WaypointManager();
        BOOST_MANAGER = new BoostManager();
        NIGHT_USER_MANAGER = new NightUserManager();
        IMAGE_MANAGER = new ImageManager();
        MODULE_MANAGER = new ModuleManager();
        COMMAND_MANAGER = new CommandManager();
        ACCOUNT_MANAGER = new AccountManager();
        if (PROXY_SERVER != null) {
            PB_MODULE_MANAGER.register(new ServerAutoCrystal());
            PB_MODULE_MANAGER.register(new ServerAutoTotem());
        }
    }

    public static void onPostInitialize() {
        String savedImage;
        Night.initManagers();
        CONFIG_MANAGER = new ConfigManager();
        CLICK_GUI = new ClickGuiScreen();
        if (IMAGE_MANAGER != null && (savedImage = ImageManager.findSavedActiveImage()) != null && !savedImage.equalsIgnoreCase("None") && !savedImage.isEmpty()) {
            IMAGE_MANAGER.setCurrentActiveImage(savedImage);
        }
        LOGGER.info("{} {} has been initialized.", (Object)MOD_NAME, (Object)MOD_VERSION);
    }

    public static void checkForUpdates() {
        UPDATE_EXECUTOR.submit(() -> {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return;
            }
            if (!FabricLoader.getInstance().isModLoaded("night-updater")) {
                return;
            }
            try {
                HttpURLConnection versionConnection = (HttpURLConnection)new URL("").openConnection();
                versionConnection.setRequestMethod("GET");
                versionConnection.connect();
                if (versionConnection.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(versionConnection.getInputStream());
                    JsonObject jsonObject = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                    if (!jsonObject.has("version")) {
                        return;
                    }
                    if (!MOD_VERSION.equalsIgnoreCase(jsonObject.get("version").getAsString())) {
                        UPDATE_STATUS = "update-available";
                    }
                } else {
                    UPDATE_STATUS = "failed-connection";
                }
            }
            catch (IOException exception) {
                UPDATE_STATUS = "failed";
            }
            if (UPDATE_STATUS.equalsIgnoreCase("none")) {
                UPDATE_STATUS = "up-to-date";
            }
        });
    }

    static {
        PB_MODULE_MANAGER = new PbModuleManager();
    }
}

