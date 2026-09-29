/*
 * Decompiled with CFR 0.152.
 */
package night.core;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class AesCrypto {
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private AesCrypto() {
    }

    static byte[] decrypt(byte[] ivAndCiphertext, byte[] key) {
        try {
            byte[] iv = new byte[12];
            System.arraycopy(ivAndCiphertext, 0, iv, 0, 12);
            int cipherLen = ivAndCiphertext.length - 12;
            byte[] ciphertext = new byte[cipherLen];
            System.arraycopy(ivAndCiphertext, 12, ciphertext, 0, cipherLen);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(2, (Key)new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return cipher.doFinal(ciphertext);
        }
        catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}

