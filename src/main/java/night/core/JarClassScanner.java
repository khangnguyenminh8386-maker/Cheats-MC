/*
 * Decompiled with CFR 0.152.
 */
package night.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

public final class JarClassScanner {
    private JarClassScanner() {
    }

   public static List<Class<?>> findSubtypesOf(Class<?> baseType, Class<?> anchorClass) {
      List<Class<?>> results = new ArrayList<>();
      ClassLoader loader = anchorClass.getClassLoader();
      URL location = anchorClass.getProtectionDomain().getCodeSource() != null ? anchorClass.getProtectionDomain().getCodeSource().getLocation() : null;
      List<String> classNames = new ArrayList<>();
      if (location != null) {
         try {
            File file;
            try {
               file = new File(location.toURI());
            } catch (Throwable ex) {
               file = new File(location.getPath());
            }

            if (file.isDirectory()) {
               classNames = scanDirectory(file);
            }
         } catch (Throwable var13) {
         }

         if (classNames.isEmpty()) {
            JarEntry entry;
            try (
               InputStream in = location.openStream();
               JarInputStream jis = new JarInputStream(in);
            ) {
               while ((entry = jis.getNextJarEntry()) != null) {
                  String name = entry.getName();
                  if (!entry.isDirectory() && name.endsWith(".class") && !name.contains("module-info")) {
                     classNames.add(toClassName(name));
                  }
               }
            } catch (Throwable var17) {
            }
         }
      }

      for (String className : classNames) {
         Class<?> clazz;
         try {
            clazz = Class.forName(className, false, loader);
         } catch (Throwable ignored) {
            continue;
         }

         if (clazz != baseType && baseType.isAssignableFrom(clazz)) {
            int mods = clazz.getModifiers();
            if (!Modifier.isAbstract(mods) && !Modifier.isInterface(mods)) {
               results.add(clazz);
            }
         }
      }

      return results;
   }

    private static List<String> scanJar(File jarFile) {
        ArrayList<String> names = new ArrayList<String>();
        try (JarFile jar = new JarFile(jarFile);){
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".class") || name.contains("module-info")) continue;
                names.add(JarClassScanner.toClassName(name));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return names;
    }

   private static List<String> scanDirectory(File dir) {
      Path root = dir.toPath();

      try (Stream<Path> walk = Files.walk(root)) {
         return walk.filter(p -> p.toString().endsWith(".class") && !p.toString().contains("module-info"))
            .map(p -> toClassName(root.relativize(p).toString().replace(File.separatorChar, '/')))
            .toList();
      } catch (Exception ignored) {
         return List.of();
      }
   }

    private static String toClassName(String entryName) {
        return entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
    }
}

