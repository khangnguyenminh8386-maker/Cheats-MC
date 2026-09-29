/*
 * Decompiled with CFR 0.152.
 */
package night.utils.system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class FileUtils {
    public static void resetFile(String path) throws IOException {
        if (Files.exists(Paths.get(path, new String[0]), new LinkOption[0])) {
            new File(path).delete();
        }
        Files.createFile(Paths.get(path, new String[0]), new FileAttribute[0]);
    }

    public static void createDirectory(String path) throws IOException {
        if (!Files.exists(Paths.get(path, new String[0]), new LinkOption[0])) {
            Files.createDirectories(Paths.get(path, new String[0]), new FileAttribute[0]);
        }
    }

    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path, new String[0]), new LinkOption[0]);
    }

    public static void playSound(File file, float volume) {
        if (file.exists()) {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(file));
                ((FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(20.0f * (float)Math.log10(volume));
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> readLines(File file) {
        List<String> messages = new ArrayList<String>();
        if (file.exists()) {
            try (Stream<String> lines = Files.lines(Paths.get(file.getPath(), new String[0]));){
                messages = lines.filter(l -> !l.isEmpty()).toList();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return messages;
    }
}

