/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.util.Util
 */
package night.accounts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import night.Night;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    public static final String SCOPE = "XboxLive.signin XboxLive.offline_access";
    public static final String REDIRECT_PATH = "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

    public static BrowserLoginSession startBrowserLogin(Consumer<String> statusConsumer) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        String redirectUri = "http://localhost:" + port + REDIRECT_PATH;
        String state = UUID.randomUUID().toString().replace("-", "");
        String loginUrl = "https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&prompt=select_account&state=" + state;
        CompletableFuture authCodeFuture = new CompletableFuture();
        server.createContext(REDIRECT_PATH, exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            Map<String, String> queryParams = MicrosoftAuth.parseQueryParams(query);
            if (queryParams.containsKey("code")) {
                String code = queryParams.get("code");
                String responseHtml = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Cheats MC</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #6450a0;box-shadow:0 8px 24px rgba(0,0,0,0.5);}h1{color:#55ffff;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Successful!</h1><p>You can close this browser tab and return to Minecraft.</p></div></body></html>";
                byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody();){
                    os.write(bytes);
                }
                authCodeFuture.complete(code);
            } else {
                String error = queryParams.getOrDefault("error_description", queryParams.getOrDefault("error", "Login was cancelled or failed"));
                String responseHtml = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Cheats MC</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #a03030;}h1{color:#ff5555;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Cancelled</h1><p>" + error + "</p></div></body></html>";
                byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(400, bytes.length);
                try (OutputStream os = exchange.getResponseBody();){
                    os.write(bytes);
                }
                authCodeFuture.completeExceptionally(new RuntimeException(error));
            }
            new Thread(() -> {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                server.stop(0);
            }).start();
        });
        server.start();
        CompletionStage resultFuture = authCodeFuture.thenApplyAsync(code -> {
            try {
                statusConsumer.accept("Exchanging authorization code with Microsoft...");
                HashMap<String, String> tokenParams = new HashMap<String, String>();
                tokenParams.put("client_id", CLIENT_ID);
                tokenParams.put("grant_type", "authorization_code");
                tokenParams.put("code", (String)code);
                tokenParams.put("redirect_uri", redirectUri);
                tokenParams.put("scope", SCOPE);
                JsonObject tokenJson = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(tokenParams));
                if (!tokenJson.has("access_token")) {
                    String err = tokenJson.has("error_description") ? tokenJson.get("error_description").getAsString() : "Failed to obtain Microsoft token.";
                    return new AuthResult(false, null, null, null, null, err);
                }
                String msAccessToken = tokenJson.get("access_token").getAsString();
                String msRefreshToken = tokenJson.has("refresh_token") ? tokenJson.get("refresh_token").getAsString() : "";
                return MicrosoftAuth.completeMinecraftLogin(msAccessToken, msRefreshToken, statusConsumer);
            }
            catch (Exception e) {
                Night.LOGGER.error("Error during Microsoft login exchange", (Throwable)e);
                return new AuthResult(false, null, null, null, null, e.getMessage());
            }
        });
        try {
            Util.getPlatform().openUri(URI.create(loginUrl));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new BrowserLoginSession(loginUrl, (CompletableFuture<AuthResult>)resultFuture, () -> {
            authCodeFuture.cancel(true);
            try {
                server.stop(0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    public static AuthResult refreshAccount(String refreshToken) {
        try {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("client_id", CLIENT_ID);
            params.put("grant_type", "refresh_token");
            params.put("refresh_token", refreshToken);
            params.put("scope", SCOPE);
            JsonObject tokenJson = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(params));
            if (!tokenJson.has("access_token")) {
                String err = tokenJson.has("error_description") ? tokenJson.get("error_description").getAsString() : "Failed to refresh Microsoft token.";
                return new AuthResult(false, null, null, null, null, err);
            }
            String msAccessToken = tokenJson.get("access_token").getAsString();
            String newRefreshToken = tokenJson.has("refresh_token") ? tokenJson.get("refresh_token").getAsString() : refreshToken;
            return MicrosoftAuth.completeMinecraftLogin(msAccessToken, newRefreshToken, s -> {});
        }
        catch (Exception e) {
            Night.LOGGER.error("Failed to refresh Microsoft account", (Throwable)e);
            return new AuthResult(false, null, null, null, null, e.getMessage());
        }
    }

    public static AuthResult completeMinecraftLogin(String msAccessToken, String refreshToken, Consumer<String> statusConsumer) throws Exception {
        statusConsumer.accept("Authenticating with Xbox Live...");
        JsonObject xblReq = new JsonObject();
        JsonObject xblProps = new JsonObject();
        xblProps.addProperty("AuthMethod", "RPS");
        xblProps.addProperty("SiteName", "user.auth.xboxlive.com");
        xblProps.addProperty("RpsTicket", "d=" + msAccessToken);
        xblReq.add("Properties", (JsonElement)xblProps);
        xblReq.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xblReq.addProperty("TokenType", "JWT");
        JsonObject xblJson = MicrosoftAuth.postJson("https://user.auth.xboxlive.com/user/authenticate", xblReq);
        String xblToken = xblJson.get("Token").getAsString();
        String userHash = xblJson.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        statusConsumer.accept("Requesting XSTS authorization...");
        JsonObject xstsReq = new JsonObject();
        JsonObject xstsProps = new JsonObject();
        xstsProps.addProperty("SandboxId", "RETAIL");
        JsonArray userTokens = new JsonArray();
        userTokens.add(xblToken);
        xstsProps.add("UserTokens", (JsonElement)userTokens);
        xstsReq.add("Properties", (JsonElement)xstsProps);
        xstsReq.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xstsReq.addProperty("TokenType", "JWT");
        JsonObject xstsJson = MicrosoftAuth.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsReq);
        if (xstsJson.has("XErr")) {
            long xerr = xstsJson.get("XErr").getAsLong();
            if (xerr == 2148916233L) {
                return new AuthResult(false, null, null, null, null, "This Microsoft account does not have an Xbox Live account.");
            }
            if (xerr == 2148916238L) {
                return new AuthResult(false, null, null, null, null, "This account is a child account and must be added to a Family.");
            }
            return new AuthResult(false, null, null, null, null, "XSTS Error: " + xerr);
        }
        String xstsToken = xstsJson.get("Token").getAsString();
        statusConsumer.accept("Logging into Minecraft Services...");
        JsonObject mcReq = new JsonObject();
        mcReq.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        JsonObject mcLoginJson = MicrosoftAuth.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcReq);
        if (!mcLoginJson.has("access_token")) {
            String errorMsg = "Failed to obtain Minecraft access token.";
            if (mcLoginJson.has("errorMessage")) {
                errorMsg = mcLoginJson.get("errorMessage").getAsString();
            } else if (mcLoginJson.has("error")) {
                errorMsg = mcLoginJson.get("error").getAsString();
            }
            return new AuthResult(false, null, null, null, null, errorMsg);
        }
        String mcAccessToken = mcLoginJson.get("access_token").getAsString();
        statusConsumer.accept("Retrieving Minecraft profile...");
        JsonObject profileJson = MicrosoftAuth.getJson("https://api.minecraftservices.com/minecraft/profile", mcAccessToken);
        if (!profileJson.has("name") || !profileJson.has("id")) {
            return new AuthResult(false, null, null, null, null, "You do not own Minecraft on this Microsoft account.");
        }
        String username = profileJson.get("name").getAsString();
        String uuid = profileJson.get("id").getAsString();
        statusConsumer.accept("Logged in as " + username + "!");
        return new AuthResult(true, username, uuid, mcAccessToken, refreshToken, null);
    }

    private static JsonObject postForm(String urlStr, String urlParameters) throws Exception {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        try (OutputStream os = conn.getOutputStream();){
            os.write(urlParameters.getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader reader = conn.getResponseCode() >= 200 && conn.getResponseCode() < 400 ? new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader((Reader)reader).getAsJsonObject();
    }

    private static JsonObject postJson(String urlStr, JsonObject jsonBody) throws Exception {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        try (OutputStream os = conn.getOutputStream();){
            os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader reader = conn.getResponseCode() >= 200 && conn.getResponseCode() < 400 ? new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader((Reader)reader).getAsJsonObject();
    }

    private static JsonObject getJson(String urlStr, String bearerToken) throws Exception {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        InputStreamReader reader = conn.getResponseCode() >= 200 && conn.getResponseCode() < 400 ? new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader((Reader)reader).getAsJsonObject();
    }

    private static String encodeParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static Map<String, String> parseQueryParams(String query) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (query == null || query.isEmpty()) {
            return map;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                continue;
            }
            if (pair.length != 1) continue;
            map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
        }
        return map;
    }

    public record BrowserLoginSession(String loginUrl, CompletableFuture<AuthResult> future, Runnable cancelAction) {
    }

    public record AuthResult(boolean success, String username, String uuid, String accessToken, String refreshToken, String errorMessage) {
    }
}

