package com.github.nija123098.tipbot;

import com.github.nija123098.tipbot.utility.Config;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Decrypt {
    static void decrypt(String[] args) {
        try {
            Config.setUp();
            Key key = new SecretKeySpec(Config.ENCRYPTION_KEY.getBytes("UTF-8"), Config.ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(Config.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            Path input = Paths.get(args[0], Arrays.copyOfRange(args, 1, args.length));
            Files.write(input.getParent().resolve("decrypted-" + input.getName(input.getNameCount() - 1)), cipher.doFinal(Files.readAllBytes(input)), StandardOpenOption.CREATE);
        } catch (IOException e){
            throw new Error("Error with loading or saving files", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Error("Error with loading cryptography settings", e);
        }
    }
}
