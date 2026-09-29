/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  org.apache.commons.lang3.StringUtils
 *  org.lwjgl.glfw.GLFW
 */
package night.utils.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class KeyboardUtils {
    public static String getKeyName(int key) {
        return switch (key) {
            case -1 -> "Unknown";
            case 256 -> "Esc";
            case 96 -> "Grave Accent";
            case 161 -> "World 1";
            case 162 -> "World 2";
            case 283 -> "Print Screen";
            case 284 -> "Pause";
            case 260 -> "Insert";
            case 261 -> "Delete";
            case 268 -> "Home";
            case 266 -> "Page Up";
            case 267 -> "Page Down";
            case 269 -> "End";
            case 258 -> "Tab";
            case 341 -> "Left Control";
            case 345 -> "Right Control";
            case 342 -> "Left Alt";
            case 346 -> "Right Alt";
            case 340 -> "Left Shift";
            case 344 -> "Right Shift";
            case 265 -> "Arrow Up";
            case 264 -> "Arrow Down";
            case 263 -> "Arrow Left";
            case 262 -> "Arrow Right";
            case 39 -> "Apostrophe";
            case 259 -> "Backspace";
            case 280 -> "Caps Lock";
            case 348 -> "Menu";
            case 343 -> "Left Super";
            case 347 -> "Right Super";
            case 257 -> "Enter";
            case 335 -> "Numpad Enter";
            case 282 -> "Num Lock";
            case 32 -> "Space";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 302 -> "F13";
            case 303 -> "F14";
            case 304 -> "F15";
            case 305 -> "F16";
            case 306 -> "F17";
            case 307 -> "F18";
            case 308 -> "F19";
            case 309 -> "F20";
            case 310 -> "F21";
            case 311 -> "F22";
            case 312 -> "F23";
            case 313 -> "F24";
            case 314 -> "F25";
            case -2 -> "Right Click";
            case -3 -> "Middle Click";
            case -4 -> "Button 3";
            case -5 -> "Button 4";
            case 0 -> "None";
            default -> {
                String keyName = GLFW.glfwGetKeyName((int)key, (int)0);
                if (keyName == null) {
                    yield "Unknown";
                }
                yield StringUtils.capitalize((String)keyName);
            }
        };
    }

    public static int getKeyNumber(String name) {
        return switch (name.toLowerCase()) {
            case "esc" -> {
                int var3_3;
                yield var3_3 = 256;
            }
            case "grave" -> {
                int var3_4;
                yield var3_4 = 96;
            }
            case "world1" -> {
                int var3_5;
                yield var3_5 = 161;
            }
            case "world2" -> {
                int var3_6;
                yield var3_6 = 162;
            }
            case "prtscr" -> {
                int var3_7;
                yield var3_7 = 283;
            }
            case "pause" -> {
                int var3_8;
                yield var3_8 = 284;
            }
            case "insert" -> {
                int var3_9;
                yield var3_9 = 260;
            }
            case "delete" -> {
                int var3_10;
                yield var3_10 = 261;
            }
            case "home" -> {
                int var3_11;
                yield var3_11 = 268;
            }
            case "pgup" -> {
                int var3_12;
                yield var3_12 = 266;
            }
            case "pgdown" -> {
                int var3_13;
                yield var3_13 = 267;
            }
            case "end" -> {
                int var3_14;
                yield var3_14 = 269;
            }
            case "tab" -> {
                int var3_15;
                yield var3_15 = 258;
            }
            case "lctrl" -> {
                int var3_16;
                yield var3_16 = 341;
            }
            case "rctrl" -> {
                int var3_17;
                yield var3_17 = 345;
            }
            case "lalt" -> {
                int var3_18;
                yield var3_18 = 342;
            }
            case "ralt" -> {
                int var3_19;
                yield var3_19 = 346;
            }
            case "lshift" -> {
                int var3_20;
                yield var3_20 = 340;
            }
            case "rshift" -> {
                int var3_21;
                yield var3_21 = 344;
            }
            case "up" -> {
                int var3_22;
                yield var3_22 = 265;
            }
            case "down" -> {
                int var3_23;
                yield var3_23 = 264;
            }
            case "left" -> {
                int var3_24;
                yield var3_24 = 263;
            }
            case "right" -> {
                int var3_25;
                yield var3_25 = 262;
            }
            case "apostrophe" -> {
                int var3_26;
                yield var3_26 = 39;
            }
            case "backspace" -> {
                int var3_27;
                yield var3_27 = 259;
            }
            case "capslock" -> {
                int var3_28;
                yield var3_28 = 280;
            }
            case "menu" -> {
                int var3_29;
                yield var3_29 = 348;
            }
            case "lsuper" -> {
                int var3_30;
                yield var3_30 = 343;
            }
            case "rsuper" -> {
                int var3_31;
                yield var3_31 = 347;
            }
            case "enter" -> {
                int var3_32;
                yield var3_32 = 257;
            }
            case "numenter" -> {
                int var3_33;
                yield var3_33 = 335;
            }
            case "numlock" -> {
                int var3_34;
                yield var3_34 = 282;
            }
            case "space" -> {
                int var3_35;
                yield var3_35 = 32;
            }
            case "f1" -> {
                int var3_36;
                yield var3_36 = 290;
            }
            case "f2" -> {
                int var3_37;
                yield var3_37 = 291;
            }
            case "f3" -> {
                int var3_38;
                yield var3_38 = 292;
            }
            case "f4" -> {
                int var3_39;
                yield var3_39 = 293;
            }
            case "f5" -> {
                int var3_40;
                yield var3_40 = 294;
            }
            case "f6" -> {
                int var3_41;
                yield var3_41 = 295;
            }
            case "f7" -> {
                int var3_42;
                yield var3_42 = 296;
            }
            case "f8" -> {
                int var3_43;
                yield var3_43 = 297;
            }
            case "f9" -> {
                int var3_44;
                yield var3_44 = 298;
            }
            case "f10" -> {
                int var3_45;
                yield var3_45 = 299;
            }
            case "f11" -> {
                int var3_46;
                yield var3_46 = 300;
            }
            case "f12" -> {
                int var3_47;
                yield var3_47 = 301;
            }
            case "f13" -> {
                int var3_48;
                yield var3_48 = 302;
            }
            case "f14" -> {
                int var3_49;
                yield var3_49 = 303;
            }
            case "f15" -> {
                int var3_50;
                yield var3_50 = 304;
            }
            case "f16" -> {
                int var3_51;
                yield var3_51 = 305;
            }
            case "f17" -> {
                int var3_52;
                yield var3_52 = 306;
            }
            case "f18" -> {
                int var3_53;
                yield var3_53 = 307;
            }
            case "f19" -> {
                int var3_54;
                yield var3_54 = 308;
            }
            case "f20" -> {
                int var3_55;
                yield var3_55 = 309;
            }
            case "f21" -> {
                int var3_56;
                yield var3_56 = 310;
            }
            case "f22" -> {
                int var3_57;
                yield var3_57 = 311;
            }
            case "f23" -> {
                int var3_58;
                yield var3_58 = 312;
            }
            case "f24" -> {
                int var3_59;
                yield var3_59 = 313;
            }
            case "f25" -> {
                int var3_60;
                yield var3_60 = 314;
            }
            case "rclick" -> {
                int var3_61;
                yield var3_61 = -2;
            }
            case "mclick" -> {
                int var3_62;
                yield var3_62 = -3;
            }
            case "button3" -> {
                int var3_63;
                yield var3_63 = -4;
            }
            case "button4" -> {
                int var3_64;
                yield var3_64 = -5;
            }
            case "none" -> {
                int var3_65;
                yield var3_65 = 0;
            }
            default -> {
                try {
                    int var3_66;
                    yield var3_66 = InputConstants.getKey((String)("key.keyboard." + name)).getValue();
                }
                catch (NumberFormatException exception) {
                    int var3_67;
                    yield var3_67 = 0;
                }
            }
        };
    }

    public static boolean isBindDown(int bind) {
        if (bind == 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.screen() != null) {
            return false;
        }
        if (mc.getWindow() == null) {
            return false;
        }
        if (bind >= 0) {
            return InputConstants.isKeyDown((Window)mc.getWindow(), (int)bind);
        }
        int button = -bind - 1;
        return GLFW.glfwGetMouseButton((long)mc.getWindow().handle(), (int)button) == 1;
    }
}

