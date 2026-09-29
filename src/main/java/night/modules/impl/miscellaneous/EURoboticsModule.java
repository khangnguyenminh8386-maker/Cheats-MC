/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.miscellaneous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.Generated;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.ChangePitchEvent;
import night.events.impl.ChangeYawEvent;
import night.events.impl.PlayerMoveEvent;
import night.events.impl.TickEvent;
import night.events.impl.ToggleModuleEvent;
import night.events.impl.UnfilteredKeyInputEvent;
import night.events.impl.UnfilteredMouseInputEvent;
import night.mixins.accessors.KeyboardHandlerAccessor;
import night.mixins.accessors.MouseAccessor;
import night.modules.Module;
import night.modules.impl.movement.HitboxDesyncModule;
import night.modules.impl.movement.HoleSnapModule;
import night.settings.impl.ModeSetting;
import night.settings.impl.StringSetting;
import night.utils.IMinecraft;
import night.utils.minecraft.MovementUtils;

public class EURoboticsModule
extends Module {
    public ModeSetting side = new ModeSetting("Side", "The side that this Minecraft instance is on.", "Client", new String[]{"Client", "Server"});
    public StringSetting port = new StringSetting("Port", "The port that will be used for communication.", "4311");
    private String target = "";
    private final Client client = new Client();
    private final Server server = new Server();

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
            return;
        }
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("update;" + EURoboticsModule.mc.player.getName().getString());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onToggleModule(ToggleModuleEvent event) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("module;" + event.getModule().getName() + ";" + event.getModule().isToggled());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onUnfilteredKeyInput(UnfilteredKeyInputEvent event) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("key;" + event.getKey() + ";" + event.getScancode() + ";" + event.getAction() + ";" + event.getModifiers());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onUnfilteredMouseInput(UnfilteredMouseInputEvent event) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("mouse;" + event.getButton() + ";" + event.getAction() + ";" + event.getMods());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onChangeYaw(ChangeYawEvent event) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("yaw;" + event.getYaw());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onChangePitch(ChangePitchEvent event) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("pitch;" + event.getPitch());
        }
        catch (IOException exception) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.getNull() || EURoboticsModule.mc.player.fallDistance >= 5.0) {
            return;
        }
        if (this.side.getValue().equalsIgnoreCase("Client")) {
            return;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= 0.25) {
            return;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= Mth.square((double)1.5) && (EURoboticsModule.mc.options.keyUp.isDown() || EURoboticsModule.mc.options.keyDown.isDown() || EURoboticsModule.mc.options.keyLeft.isDown() || EURoboticsModule.mc.options.keyRight.isDown())) {
            return;
        }
        if (Night.MODULE_MANAGER.getModule(HoleSnapModule.class).isToggled()) {
            return;
        }
        if (Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class).isToggled()) {
            return;
        }
        MovementUtils.moveTowards(event, Vec3.upFromBottomCenterOf((Vec3i)player.blockPosition(), (double)0.0), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
    }

    @Override
    @SubscribeEvent
    public void onEnable() {
        if (this.side.getValue().equalsIgnoreCase("Client")) {
            try {
                if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
                    if (this.client.getPrimarySocket() != null) {
                        this.client.stopConnection();
                    }
                    return;
                }
                this.client.startConnection("127.0.0.1", Integer.parseInt(this.port.getValue()));
            }
            catch (IOException exception) {
                Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)exception);
                Night.CHAT_MANAGER.error("Failed to establish a connection to the EURobotics server.");
                this.setToggled(false);
            }
        } else {
            try {
                if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
                    if (this.server.getServerSocket() != null) {
                        this.server.stopConnection();
                    }
                    return;
                }
                new Thread(() -> {
                    try {
                        this.server.startConnection(Integer.parseInt(this.port.getValue()));
                    }
                    catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                }).start();
            }
            catch (IOException exception) {
                Night.LOGGER.error("An exception has been thrown by serverside EU Robotics!", (Throwable)exception);
                Night.CHAT_MANAGER.error("Failed to start the EURobotics server.");
                this.setToggled(false);
            }
        }
    }

    public boolean shouldStep() {
        if (!this.isToggled()) {
            return false;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= 0.25) {
            return false;
        }
        return !(EURoboticsModule.mc.player.distanceToSqr(player.position()) <= Mth.square((double)1.5)) || !EURoboticsModule.mc.options.keyUp.isDown() && !EURoboticsModule.mc.options.keyDown.isDown() && !EURoboticsModule.mc.options.keyLeft.isDown() && !EURoboticsModule.mc.options.keyRight.isDown();
    }

    private Player getPlayer() {
        if (this.target.isEmpty()) {
            return null;
        }
        for (Player player : EURoboticsModule.mc.level.players()) {
            if (player == EURoboticsModule.mc.player || !player.getName().getString().equals(this.target)) continue;
            return player;
        }
        return null;
    }

    @Generated
    public void setTarget(String target) {
        this.target = target;
    }

    public static class Client {
        private Socket primarySocket;
        private PrintWriter primaryOut;
        private Socket secondarySocket;
        private PrintWriter secondaryOut;
        private Socket tertiarySocket;
        private PrintWriter tertiaryOut;

        public void startConnection(String ip, int port) throws IOException {
            this.primarySocket = new Socket(ip, port);
            this.primaryOut = new PrintWriter(this.primarySocket.getOutputStream(), true);
            this.secondarySocket = new Socket(ip, port + 1);
            this.secondaryOut = new PrintWriter(this.secondarySocket.getOutputStream(), true);
            this.tertiarySocket = new Socket(ip, port + 2);
            this.tertiaryOut = new PrintWriter(this.tertiarySocket.getOutputStream(), true);
        }

        public void stopConnection() throws IOException {
            this.primaryOut.close();
            this.primarySocket.close();
        }

        public void sendMessage(String msg) throws IOException {
            this.primaryOut.println(msg);
            this.secondaryOut.println(msg);
            this.tertiaryOut.println(msg);
        }

        @Generated
        public Socket getPrimarySocket() {
            return this.primarySocket;
        }

        @Generated
        public PrintWriter getPrimaryOut() {
            return this.primaryOut;
        }

        @Generated
        public Socket getSecondarySocket() {
            return this.secondarySocket;
        }

        @Generated
        public PrintWriter getSecondaryOut() {
            return this.secondaryOut;
        }

        @Generated
        public Socket getTertiarySocket() {
            return this.tertiarySocket;
        }

        @Generated
        public PrintWriter getTertiaryOut() {
            return this.tertiaryOut;
        }
    }

    public static class Server {
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private BufferedReader in;

        public void startConnection(int port) throws IOException {
            String inputLine;
            this.serverSocket = new ServerSocket(port);
            this.clientSocket = this.serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            while ((inputLine = this.in.readLine()) != null) {
                Module module;
                String[] split = inputLine.split(";");
                if (split[0].equalsIgnoreCase("module") && split.length == 3 && (module = Night.MODULE_MANAGER.getModule(split[1])) != null) {
                    Night.TASK_MANAGER.submit(() -> module.setToggled(Boolean.parseBoolean(split[2])));
                }
                if (split[0].equalsIgnoreCase("update") && split.length == 2) {
                    Night.MODULE_MANAGER.getModule(EURoboticsModule.class).setTarget(split[1]);
                }
                if (split[0].equalsIgnoreCase("key") && split.length == 5 && IMinecraft.mc.keyboardHandler != null) {
                    int key = Integer.parseInt(split[1]);
                    int scancode = Integer.parseInt(split[2]);
                    int action = Integer.parseInt(split[3]);
                    int mods = Integer.parseInt(split[4]);
                    Night.TASK_MANAGER.submit(() -> ((KeyboardHandlerAccessor)IMinecraft.mc.keyboardHandler).invokeKeyPress(IMinecraft.mc.getWindow().handle(), action, new KeyEvent(key, scancode, mods)));
                }
                if (split[0].equalsIgnoreCase("mouse") && split.length == 4 && IMinecraft.mc.mouseHandler != null) {
                    int button = Integer.parseInt(split[1]);
                    int action = Integer.parseInt(split[2]);
                    int mods = Integer.parseInt(split[3]);
                    Night.TASK_MANAGER.submit(() -> ((MouseAccessor)IMinecraft.mc.mouseHandler).invokeOnButton(IMinecraft.mc.getWindow().handle(), new MouseButtonInfo(button, mods), action));
                }
                if (split[0].equalsIgnoreCase("yaw") && split.length == 2 && IMinecraft.mc.player != null) {
                    IMinecraft.mc.player.setYRot(Float.parseFloat(split[1]));
                }
                if (split[0].equalsIgnoreCase("pitch") && split.length == 2 && IMinecraft.mc.player != null) {
                    IMinecraft.mc.player.setXRot(Float.parseFloat(split[1]));
                }
                if (Night.MODULE_MANAGER.getModule(EURoboticsModule.class).isToggled()) continue;
                this.stopConnection();
                break;
            }
        }

        public void stopConnection() throws IOException {
            this.in.close();
            this.clientSocket.close();
            this.serverSocket.close();
        }

        @Generated
        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }

        @Generated
        public Socket getClientSocket() {
            return this.clientSocket;
        }

        @Generated
        public BufferedReader getIn() {
            return this.in;
        }
    }
}

