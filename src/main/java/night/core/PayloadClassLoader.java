/*
 * Decompiled with CFR 0.152.
 */
package night.core;

import java.io.IOException;
import java.io.InputStream;
import night.core.AesCrypto;

final class PayloadClassLoader
extends ClassLoader {
    private final ClassLoader jarResourceLoader;
    private final byte[] key;

    PayloadClassLoader(ClassLoader parent, ClassLoader jarResourceLoader, byte[] key) {
        super(parent);
        this.jarResourceLoader = jarResourceLoader;
        this.key = key;
    }

    @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException {
      String resourcePath = name.replace('.', '/') + ".enc";

      try (InputStream in = this.jarResourceLoader.getResourceAsStream(resourcePath)) {
         if (in == null) {
            throw new ClassNotFoundException(name);
         }

         byte[] encrypted = in.readAllBytes();
         byte[] decrypted = AesCrypto.decrypt(encrypted, this.key);
         return this.defineClass(name, decrypted, 0, decrypted.length);
      } catch (IOException e) {
         throw new ClassNotFoundException(name, e);
      }
   }
}

