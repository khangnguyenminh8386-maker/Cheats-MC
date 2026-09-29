/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.Vec3
 */
package night.modules.impl.miscellaneous;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.SubscribeEvent;
import night.events.impl.BreakBlockEvent;
import night.events.impl.ChatInputEvent;
import night.events.impl.ConsumeItemEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.TickEvent;
import night.modules.Module;
import night.modules.RegisterModule;
import night.settings.impl.BooleanSetting;
import night.settings.impl.ModeSetting;
import night.settings.impl.NumberSetting;
import night.settings.impl.StringSetting;
import night.utils.system.MathUtils;
import night.utils.system.Timer;

@RegisterModule(name="Announcer", description="Announces your actions in chat.", category=Module.Category.MISCELLANEOUS)
public class AnnouncerModule
extends Module {
    public StringSetting watermark = new StringSetting("Watermark", "The client name that will be used in the announcer.", "Cheats MC");
    public ModeSetting language = new ModeSetting("Language", "The language that will be used for the announcer.", "English", new String[]{"English", "German", "French", "Japanese", "Finnish", "Russian", "Spanish", "Swedish", "Turkish", "Dutch", "Greek", "Chinese", "Italian", "Norwegian", "Romanian", "Czech", "Portuguese", "Slovenian", "Polish", "Korean", "Lithuanian", "Indonesian", "Hungarian", "Random"});
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting distance = new BooleanSetting("Distance", "Announces your distance travelled in chat.", true);
    public BooleanSetting blocksMined = new BooleanSetting("BlocksMined", "Announces blocks mined in chat.", true);
    public BooleanSetting blocksPlaced = new BooleanSetting("BlocksPlaced", "Announces blocks placed in chat.", true);
    public BooleanSetting eating = new BooleanSetting("Eating", "Announces when you eat in chat.", true);
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
    private final Timer messageTimer = new Timer();
    private final Timer distanceTimer = new Timer();
    private Vec3 lastPos;
    private int mined;
    private int placed;
    private int eaten;

    @Override
    public void onEnable() {
        this.queue.clear();
        this.messageTimer.reset();
        this.distanceTimer.reset();
        this.lastPos = null;
        this.mined = 0;
        this.placed = 0;
        this.eaten = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.getNull()) {
            return;
        }
        if (this.messageTimer.hasTimeElapsed(this.delay.getValue().intValue() * 1000) && !this.queue.isEmpty()) {
            String message = this.queue.poll();
            if (this.clientside.getValue()) {
                Night.CHAT_MANAGER.message(message);
            } else {
                AnnouncerModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + message);
            }
            this.messageTimer.reset();
        }
        if (this.lastPos == null) {
            this.lastPos = new Vec3(AnnouncerModule.mc.player.xo, AnnouncerModule.mc.player.yo, AnnouncerModule.mc.player.zo);
        }
        double traveled = Math.abs(this.lastPos.x - AnnouncerModule.mc.player.xo) + Math.abs(this.lastPos.y - AnnouncerModule.mc.player.yo) + Math.abs(this.lastPos.z - AnnouncerModule.mc.player.zo);
        if (this.distance.getValue() && traveled > 1.0 && this.distanceTimer.hasTimeElapsed(10000) && this.queue.size() <= 5) {
            this.queue.add(this.getDistanceMessage("" + MathUtils.round(traveled, 1)));
            this.lastPos = new Vec3(AnnouncerModule.mc.player.xo, AnnouncerModule.mc.player.yo, AnnouncerModule.mc.player.zo);
            this.distanceTimer.reset();
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakBlockEvent event) {
        if (this.getNull()) {
            return;
        }
        ++this.mined;
        if (this.blocksMined.getValue() && (double)this.mined >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(this.getMineMessage("" + this.mined));
            this.mined = 0;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        ServerboundUseItemOnPacket packet;
        if (this.getNull()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundUseItemOnPacket && AnnouncerModule.mc.player.getItemInHand((packet = (ServerboundUseItemOnPacket)packet2).getHand()).getItem() instanceof BlockItem) {
            ++this.placed;
            if (this.blocksPlaced.getValue() && (double)this.placed >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
                this.queue.add(this.getPlaceMessage("" + this.placed));
                this.placed = 0;
            }
        }
    }

    @SubscribeEvent
    public void onConsumeItem(ConsumeItemEvent event) {
        if (this.getNull() || event.getStack().get(DataComponents.FOOD) == null) {
            return;
        }
        ++this.eaten;
        if (this.eating.getValue() && (double)this.eaten >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(this.getEatMessage(this.eaten + " " + event.getStack().getHoverName().getString()));
            this.eaten = 0;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent event) {
        if (this.getNull()) {
            return;
        }
        this.messageTimer.reset();
    }

    private String getDistanceMessage(String replacement) {
        String[] messages = new String[]{"I just flew " + replacement + " meters thanks to " + this.watermark.getValue() + "!", "Ich bin gerade " + replacement + " Meter weit geflogen, dank " + this.watermark.getValue() + "!", "Je viens de voler " + replacement + " m\u00e8tres gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + replacement + "\u30e1\u30fc\u30c8\u30eb\u98db\u3093\u3060\u3088\uff01", "Lensin juuri " + replacement + " metri\u00e4 " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u043f\u0440\u043e\u043b\u0435\u0442\u0435\u043b " + replacement + " \u043c\u0435\u0442\u0440\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "Acabo de volar " + replacement + " metros gracias a " + this.watermark.getValue() + "!", "Jag fl\u00f6g just " + replacement + " meter tack vare " + this.watermark.getValue() + "!", this.watermark.getValue() + " sayesinde " + replacement + " metre u\u00e7tum!", "Ik heb net " + replacement + " meter gevlogen dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c0\u03ad\u03c4\u03b1\u03be\u03b1 " + replacement + " \u03bc\u03ad\u03c4\u03c1\u03b1 \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!", "\u6211\u521a\u521a\u98de\u4e86" + replacement + "\u7c73\uff0c\u591a\u4e8f\u4e86" + this.watermark.getValue() + "!", "Ho appena volato per " + replacement + " metri grazie ad " + this.watermark.getValue() + "!", "Jeg fl\u00f8y nettopp " + replacement + " meter takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am zburat " + replacement + " de metri datorit\u0103 lui " + this.watermark.getValue() + "!", "D\u00edky " + this.watermark.getValue() + " jsem pr\u00e1v\u011b ulet\u011bl " + replacement + " metr\u016f!", "Acabei de voar " + replacement + " metros gra\u00e7as ao " + this.watermark.getValue() + "!", "Z " + this.watermark.getValue() + " sem pravkar preletel " + replacement + " metrov!", "W\u0142a\u015bnie przelecia\u0142em " + replacement + " metr\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 " + replacement + "\ub97c \ube44\ud589\ud588\uc2b5\ub2c8\ub2e4!", this.watermark.getValue() + " d\u0117ka k\u0105 tik nuskridau " + replacement + " metrus!", "Saya baru saja terbang sejauh " + replacement + " meter berkat " + this.watermark.getValue() + "!", replacement + " m\u00e9tert rep\u00fcltem az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!"};
        return messages[this.getLanguageIndex()];
    }

    private String getMineMessage(String replacement) {
        String[] messages = new String[]{"I just mined " + replacement + " blocks thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + replacement + " Bl\u00f6cke abgebaut, dank " + this.watermark.getValue() + "!", "Je viens d'extraire " + replacement + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + replacement + "\u30d6\u30ed\u30c3\u30af\u63a1\u6398\u3057\u305f\u3068\u3053\u308d\u3067\u3059\uff01", "Louhin juuri " + replacement + " lohkoa " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0434\u043e\u0431\u044b\u043b " + replacement + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de minar " + replacement + " bloques gracias a " + this.watermark.getValue() + "!", "Jag har precis tagit fram " + replacement + " block tack vare " + this.watermark.getValue() + "!", this.watermark.getValue() + " sayesinde az \u00f6nce " + replacement + " blok kazd\u0131m!", "Ik heb net " + replacement + " blokken gedolven dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03b5\u03be\u03cc\u03c1\u03c5\u03be\u03b1 " + replacement + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!", "\u6211\u521a\u521a\u5f00\u91c7\u4e86" + replacement + "\u4e2a\u533a\u5757\uff0c\u611f\u8c22" + this.watermark.getValue() + "!", "Ho appena estratto " + replacement + " blocchi grazie ad " + this.watermark.getValue() + "!", "Jeg har nettopp utvunnet " + replacement + " blokker takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am minat " + replacement + " de blocuri datorit\u0103 lui " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem vyt\u011b\u017eil " + replacement + " blok\u016f d\u00edky " + this.watermark.getValue() + "!", "Acabei de extrair " + replacement + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!", "Zahvaljujo\u010d " + this.watermark.getValue() + " sem pravkar izkopal " + replacement + " blokov!", "W\u0142a\u015bnie wydoby\u0142em " + replacement + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + replacement + " \ube14\ub85d\uc744 \ucc44\uad74\ud588\uc2b5\ub2c8\ub2e4!", this.watermark.getValue() + " d\u0117ka k\u0105 tik i\u0161kasiau " + replacement + " blokus!", "Saya baru saja menambang " + replacement + " blok berkat " + this.watermark.getValue() + "!", "Most b\u00e1ny\u00e1sztam " + replacement + " blokkot az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!"};
        return messages[this.getLanguageIndex()];
    }

    private String getPlaceMessage(String replacement) {
        String[] messages = new String[]{"I just placed " + replacement + " blocks thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + replacement + " Bl\u00f6cke dank " + this.watermark.getValue() + " platziert!", "Je viens de placer " + replacement + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + replacement + "\u500b\u306e\u30d6\u30ed\u30c3\u30af\u3092\u7f6e\u3044\u305f\u3068\u3053\u308d\u3060!", "Sijoitin juuri " + replacement + " lohkoa " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0440\u0430\u0437\u043c\u0435\u0441\u0442\u0438\u043b " + replacement + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de colocar " + replacement + " bloques gracias a " + this.watermark.getValue() + "!", "Jag har precis placerat " + replacement + " block tack vare " + this.watermark.getValue() + "!", "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + replacement + " blok yerle\u015ftirdim!", "Ik heb net " + replacement + " blokken geplaatst dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c4\u03bf\u03c0\u03bf\u03b8\u03ad\u03c4\u03b7\u03c3\u03b1 " + replacement + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!", "\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "\uff0c\u6211\u521a\u521a\u653e\u4e86 " + replacement + " \u5757!", "Ho appena piazzato " + replacement + " blocchi grazie a " + this.watermark.getValue() + "!", "Jeg har nettopp plassert " + replacement + " blokker takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am plasat " + replacement + " blocuri datorit\u0103 lui " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem um\u00edstil " + replacement + " blok\u016f d\u00edky " + this.watermark.getValue() + "!", "Acabei de colocar " + replacement + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!", "Pravkar sem postavil " + replacement + " blokov zahvaljujo\u010d " + this.watermark.getValue() + "!", "W\u0142a\u015bnie umie\u015bci\u0142em " + replacement + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 XXX \ube14\ub85d\uc744 \ubc30\uce58\ud588\uc2b5\ub2c8\ub2e4!", "K\u0105 tik \u012fd\u0117jau " + replacement + " blok\u0173 d\u0117ka " + this.watermark.getValue() + "!", "Saya baru saja menempatkan blok " + replacement + " berkat " + this.watermark.getValue() + "!", "Most helyeztem el " + replacement + " blokkot a " + this.watermark.getValue() + "-nek k\u00f6sz\u00f6nhet\u0151en!"};
        return messages[this.getLanguageIndex()];
    }

    private String getEatMessage(String replacement) {
        String[] messages = new String[]{"I just ate " + replacement + " thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + replacement + " gegessen, dank " + this.watermark.getValue() + "!", "Je viens de manger " + replacement + " gr\u00e2ce \u00e0 " + this.watermark.getValue() + " !", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + replacement + "\u3092\u98df\u3079\u305f\u3088!", "S\u00f6in juuri " + replacement + " kiitos " + this.watermark.getValue() + "!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0441\u044a\u0435\u043b " + replacement + " \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de comer " + replacement + " gracias a " + this.watermark.getValue() + "!", "Jag \u00e5t just " + replacement + " tack vare " + this.watermark.getValue() + "!", "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + replacement + " yedim!", "Ik heb net " + replacement + " gegeten dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03ad\u03c6\u03b1\u03b3\u03b1 " + replacement + " \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!", "\u6211\u521a\u5403\u4e86 " + replacement + "\uff0c\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "!", "Ho appena mangiato " + replacement + " grazie a " + this.watermark.getValue() + "!", "Jeg spiste nettopp " + replacement + " takket v\u00e6re " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem sn\u011bdl " + replacement + " d\u00edky " + this.watermark.getValue() + "!", "Acabei de comer " + replacement + " gra\u00e7as a " + this.watermark.getValue() + "!", "Pravkar sem pojedel " + replacement + " zaradi " + this.watermark.getValue() + "!", "W\u0142a\u015bnie zjad\u0142em " + replacement + " dzi\u0119ki " + this.watermark.getValue() + "!", "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + replacement + "\ub97c \uba39\uc5c8\uc5b4\uc694!", "K\u0105 tik suvalgiau " + replacement + " d\u0117l " + this.watermark.getValue() + "!", "Saya baru saja makan " + replacement + " berkat " + this.watermark.getValue() + "!", "Most ettem " + replacement + "-t, h\u00e1la " + this.watermark.getValue() + "-nek!"};
        return messages[this.getLanguageIndex()];
    }

    private int getLanguageIndex() {
        return switch (this.language.getValue()) {
            case "German" -> 1;
            case "French" -> 2;
            case "Japanese" -> 3;
            case "Finnish" -> 4;
            case "Russian" -> 5;
            case "Spanish" -> 6;
            case "Swedish" -> 7;
            case "Turkish" -> 8;
            case "Dutch" -> 9;
            case "Greek" -> 10;
            case "Chinese" -> 11;
            case "Italian" -> 12;
            case "Norwegian" -> 13;
            case "Romanian" -> 14;
            case "Czech" -> 15;
            case "Portuguese" -> 16;
            case "Slovenian" -> 17;
            case "Polish" -> 18;
            case "Korean" -> 19;
            case "Lithuanian" -> 20;
            case "Indonesian" -> 21;
            case "Hungarian" -> 22;
            case "Random" -> (int)MathUtils.random(22.0, 0.0);
            default -> 0;
        };
    }
}

