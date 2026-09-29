/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.Expose
 *  com.google.gson.annotations.SerializedName
 */
package night.accounts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import night.accounts.AccountType;

public class Account {
    @Expose
    @SerializedName(value="name")
    private String name;
    @Expose
    @SerializedName(value="uuid")
    private String uuid;
    @Expose
    @SerializedName(value="type")
    private AccountType type;
    @Expose
    @SerializedName(value="refreshToken")
    private String refreshToken;
    @Expose
    @SerializedName(value="accessToken")
    private String accessToken;
    @Expose
    @SerializedName(value="lastUsed")
    private long lastUsed;

    public Account() {
    }

    public Account(String name, String uuid, AccountType type) {
        this.name = name;
        this.uuid = uuid;
        this.type = type;
        this.lastUsed = System.currentTimeMillis();
    }

    public Account(String name, String uuid, AccountType type, String refreshToken, String accessToken) {
        this.name = name;
        this.uuid = uuid;
        this.type = type;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.lastUsed = System.currentTimeMillis();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public AccountType getType() {
        return this.type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public UUID getProfileId() {
        try {
            if (this.uuid != null && !this.uuid.isEmpty()) {
                if (this.uuid.contains("-")) {
                    return UUID.fromString(this.uuid);
                }
                return UUID.fromString(this.uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})", "$1-$2-$3-$4-$5"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes(StandardCharsets.UTF_8));
    }
}

