/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  lombok.Generated
 *  net.minecraft.client.User
 *  net.minecraft.client.multiplayer.ProfileKeyPairManager
 */
package night.managers;
import java.io.Writer;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import night.Night;
import night.accounts.Account;
import night.accounts.AccountType;
import night.accounts.MicrosoftAuth;
import night.mixins.accessors.MinecraftClientAccessor;
import night.utils.IMinecraft;

public class AccountManager
implements IMinecraft {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Account> accounts = new CopyOnWriteArrayList<Account>();
    private Account currentAccount;

    public AccountManager() {
        this.loadAccounts();
    }

   public void loadAccounts() {
      try {
         Path path = this.getAccountsPath();
         if (!Files.exists(path)) {
            return;
         }

         try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type listType = (new TypeToken<List<Account>>() {}).getType();
            List<Account> loaded = GSON.fromJson(reader, listType);
            if (loaded != null) {
               this.accounts.clear();
               this.accounts.addAll(loaded);
            }
         }
      } catch (Exception e) {
         Night.LOGGER.error("Failed to load accounts.json", e);
      }
   }

    public void saveAccounts() {
        try {
            Path path = this.getAccountsPath();
            if (path.getParent() != null && !Files.exists(path.getParent(), new LinkOption[0])) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(this.accounts, (Appendable)writer);
            }
        }
        catch (Exception e) {
            Night.LOGGER.error("Failed to save accounts.json", (Throwable)e);
        }
    }

    private Path getAccountsPath() {
        return Paths.get("Cheats MC", "accounts.json");
    }

    private void applySession(User user, String accessToken) {
        MinecraftClientAccessor accessor = (MinecraftClientAccessor)mc;
        accessor.setUser(user);
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
                UserApiService userApiService = authService.createUserApiService(accessToken);
                accessor.setUserApiService(userApiService);
                ProfileKeyPairManager keyPairManager = ProfileKeyPairManager.create((UserApiService)userApiService, (User)user, (Path)AccountManager.mc.gameDirectory.toPath());
                accessor.setProfileKeyPairManager(keyPairManager);
                keyPairManager.prepareKeyPair();
            } else {
                accessor.setUserApiService(UserApiService.OFFLINE);
                accessor.setProfileKeyPairManager(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
            }
        }
        catch (Exception e) {
            Night.LOGGER.warn("Failed to initialize ProfileKeyPairManager for account: " + user.getName(), (Throwable)e);
        }
    }

    public boolean login(Account account) {
        if (account == null) {
            return false;
        }
        try {
            if (account.getType() == AccountType.OFFLINE) {
                User user = new User(account.getName(), account.getProfileId(), "", Optional.empty(), Optional.empty());
                this.applySession(user, "");
                this.currentAccount = account;
                account.setLastUsed(System.currentTimeMillis());
                this.saveAccounts();
                return true;
            }
            if (account.getType() == AccountType.MICROSOFT) {
                MicrosoftAuth.AuthResult result;
                if (account.getRefreshToken() != null && !account.getRefreshToken().isEmpty() && (result = MicrosoftAuth.refreshAccount(account.getRefreshToken())).success()) {
                    account.setName(result.username());
                    account.setUuid(result.uuid());
                    account.setAccessToken(result.accessToken());
                    if (result.refreshToken() != null && !result.refreshToken().isEmpty()) {
                        account.setRefreshToken(result.refreshToken());
                    }
                    User user = new User(account.getName(), account.getProfileId(), account.getAccessToken(), Optional.empty(), Optional.empty());
                    this.applySession(user, account.getAccessToken());
                    this.currentAccount = account;
                    account.setLastUsed(System.currentTimeMillis());
                    this.saveAccounts();
                    return true;
                }
                if (account.getAccessToken() != null && !account.getAccessToken().isEmpty()) {
                    User user = new User(account.getName(), account.getProfileId(), account.getAccessToken(), Optional.empty(), Optional.empty());
                    this.applySession(user, account.getAccessToken());
                    this.currentAccount = account;
                    account.setLastUsed(System.currentTimeMillis());
                    this.saveAccounts();
                    return true;
                }
                return false;
            }
            if (account.getType() == AccountType.SESSION) {
                User user = new User(account.getName(), account.getProfileId(), account.getAccessToken() != null ? account.getAccessToken() : "", Optional.empty(), Optional.empty());
                this.applySession(user, account.getAccessToken());
                this.currentAccount = account;
                account.setLastUsed(System.currentTimeMillis());
                this.saveAccounts();
                return true;
            }
        }
        catch (Exception e) {
            Night.LOGGER.error("Failed to login to account: " + account.getName(), (Throwable)e);
            return false;
        }
        return false;
    }

    public Account loginOffline(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        username = username.trim();
        Account found = null;
        for (Account acc : this.accounts) {
            if (acc.getType() != AccountType.OFFLINE || !acc.getName().equalsIgnoreCase(username)) continue;
            found = acc;
            break;
        }
        if (found == null) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
            found = new Account(username, uuid.toString(), AccountType.OFFLINE);
            this.accounts.add(found);
        }
        this.login(found);
        return found;
    }

    public void addAccount(Account account) {
        if (account == null) {
            return;
        }
        this.accounts.removeIf(a -> a.getName().equalsIgnoreCase(account.getName()) && a.getType() == account.getType());
        this.accounts.add(account);
        this.saveAccounts();
    }

    public void removeAccount(Account account) {
        if (account == null) {
            return;
        }
        this.accounts.remove(account);
        if (this.currentAccount == account) {
            this.currentAccount = null;
        }
        this.saveAccounts();
    }

    public String getCurrentName() {
        if (mc.getUser() != null) {
            return mc.getUser().getName();
        }
        return this.currentAccount != null ? this.currentAccount.getName() : "Unknown";
    }

    @Generated
    public List<Account> getAccounts() {
        return this.accounts;
    }

    @Generated
    public Account getCurrentAccount() {
        return this.currentAccount;
    }

    @Generated
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }
}

