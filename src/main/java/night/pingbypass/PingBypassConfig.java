/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package night.pingbypass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingBypassConfig.class);
    public static final String DEFAULT_SERVER = "false";
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String DEFAULT_PORT = "25565";
    public static final String DEFAULT_PASSWORD = "";
    private static final String KEY_SERVER = "pb.server";
    private static final String KEY_IP = "pb.ip";
    private static final String KEY_PORT = "pb.port";
    private static final String KEY_PASSWORD = "pb.password";
    private final Properties properties = new Properties();
    private final Path configPath;
    private boolean loaded;

    public PingBypassConfig(Path runDirectory) {
        this.configPath = runDirectory.resolve(Paths.get("night", "pingbypass.properties"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() {
        try {
            Path parentDir = this.configPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir, new FileAttribute[0]);
            }
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                Path oldConfigPath = this.configPath.getParent().getParent().resolve(Paths.get("euclient", "pingbypass.properties"));
                if (Files.exists(oldConfigPath, new LinkOption[0])) {
                    Files.copy(oldConfigPath, this.configPath, StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    this.createDefaults();
                }
            }
            try (InputStream in = Files.newInputStream(this.configPath, new OpenOption[0]);){
                this.properties.load(in);
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to load PingBypass config from {}", (Object)this.configPath, (Object)e);
        }
        finally {
            this.loaded = true;
        }
    }

    private void createDefaults() throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty(KEY_SERVER, DEFAULT_SERVER);
        defaults.setProperty(KEY_IP, DEFAULT_IP);
        defaults.setProperty(KEY_PORT, DEFAULT_PORT);
        defaults.setProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
        try (OutputStream out = Files.newOutputStream(this.configPath, new OpenOption[0]);){
            defaults.store(out, "PingBypass configuration");
        }
    }

    public boolean isServer() {
        return Boolean.parseBoolean(this.getProperty(KEY_SERVER, DEFAULT_SERVER));
    }

    public String getIp() {
        return this.getProperty(KEY_IP, DEFAULT_IP);
    }

    public int getPort() {
        String portStr = this.getProperty(KEY_PORT, DEFAULT_PORT);
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                LOGGER.error("PingBypass port out of range (1-65535): {}, falling back to default 25565", (Object)port);
                return 25565;
            }
            return port;
        }
        catch (NumberFormatException e) {
            LOGGER.error("PingBypass port is not a valid integer: '{}', falling back to default 25565", (Object)portStr);
            return 25565;
        }
    }

    public String getPassword() {
        return this.getProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
    }

    public boolean hasPassword() {
        String password = this.getPassword();
        return password != null && !password.isEmpty();
    }

    public String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return this.properties.getProperty(key, defaultValue);
    }

    public Properties toProperties() {
        Properties result = new Properties();
        result.setProperty(KEY_SERVER, String.valueOf(this.isServer()));
        result.setProperty(KEY_IP, this.getIp());
        result.setProperty(KEY_PORT, String.valueOf(this.getPort()));
        result.setProperty(KEY_PASSWORD, this.getPassword());
        return result;
    }

    public boolean isLoaded() {
        return this.loaded;
    }
}

