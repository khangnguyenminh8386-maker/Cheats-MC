/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.UserApiService
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.User
 *  net.minecraft.client.multiplayer.ProfileKeyPairManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package night.mixins.accessors;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Minecraft.class})
public interface MinecraftClientAccessor {
    @Invoker(value="startUseItem")
    public void invokeStartUseItem();

    @Accessor(value="user")
    public User getUser();

    @Mutable
    @Accessor(value="user")
    public void setUser(User var1);

    @Accessor(value="userApiService")
    public UserApiService getUserApiService();

    @Mutable
    @Accessor(value="userApiService")
    public void setUserApiService(UserApiService var1);

    @Accessor(value="profileKeyPairManager")
    public ProfileKeyPairManager getProfileKeyPairManager();

    @Mutable
    @Accessor(value="profileKeyPairManager")
    public void setProfileKeyPairManager(ProfileKeyPairManager var1);
}

