package com.github.nija123098.tipbot.utility;

import com.github.nija123098.tipbot.Bot;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.util.MessageBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Database {
    public static final String BALANCES = "balances";
    public static final String RECEIVED = "received";
    public static final String RECEIVING_ADDRESSES = "receiving_addresses";
    public static final String PREFERRED_CURRENCY = "preferred_currency";
    public static final String ANNOUNCEMENT_CHANNEL = "announcement_channel";
    public static final String PREFIXES = "prefixes";

    private static final Map<String, Map<Long, String>> MAP = new HashMap<>();
    private static final AtomicReference<Path> MOST_RECENT_FILE = new AtomicReference<>();

    private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(2, r -> {
        Thread thread = new Thread(r, "Database-Thread");
        thread.setDaemon(true);
        return thread;
    });

    static {
        List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, Database.class.getFields());
        fields.forEach(field -> {
            try {
                MAP.put((String) field.get(Database.class), new HashMap<>());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        Runnable saveData = () -> {
            List<String> strings = new ArrayList<>(1000);
            fields.forEach(field -> {
                try {
                    strings.add((String) field.get(Database.class));
                    MAP.get(field.get(Database.class)).forEach((l, a) -> strings.add(String.valueOf(l) + "=" + a));
                } catch (IllegalAccessException ignored) {}// this can't happen
            });
            Path path = getSaveFilePath();
            try {
                MOST_RECENT_FILE.set(path);
                Files.write(path, strings, StandardOpenOption.CREATE);
            } catch (IOException e) {
                System.out.println();
                strings.forEach(System.out::println);
                System.out.println();
                throw new RuntimeException("Could not save file, dumped data to console", e);
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(saveData, "Shutdown-Save-Thread"));
        EXECUTOR_SERVICE.scheduleWithFixedDelay(saveData, 1, 1, TimeUnit.HOURS);

        Cipher cipher;
        Key key;
        try {
            key = new SecretKeySpec(Config.ENCRYPTION_KEY.getBytes("UTF-8"), Config.ENCRYPTION_ALGORITHM);
            cipher = Cipher.getInstance(Config.ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new Error("Error with encryption settings", e);
        }
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            Path encryptedFile = Paths.get("encrypted", "encrypted-" + MOST_RECENT_FILE.get().getFileName());
            if (Files.exists(encryptedFile)) return;
            try {
                encryptedFile.toFile().getParentFile().mkdirs();
                Files.write(encryptedFile, cipher.doFinal(Files.readAllBytes(MOST_RECENT_FILE.get())), StandardOpenOption.CREATE);
                new MessageBuilder(Bot.DISCORD_CLIENT).withChannel(Bot.MAINTAINER.getOrCreatePMChannel()).withContent("Encrypted backup").withFile(encryptedFile.toFile()).send();
            } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
                throw new Error("Error with encryption", e);
            }
        }, 5, 5, TimeUnit.SECONDS);

        File[] files = new File("data").listFiles();
        Matcher matcher = Pattern.compile("([0-9])+.txt").matcher("");
        if (files != null) {
            Stream.of(files).filter(file -> matcher.reset(file.getName()).matches()).map(file -> Long.parseLong(file.getName().substring(0, file.getName().length() - 4))).reduce((a, b) -> a > b ? a : b).ifPresent((time) -> {
                try {
                    AtomicReference<Map<Long, String>> activeMap = new AtomicReference<>();
                    Path path = Paths.get("data" + File.separator + time + ".txt");
                    MOST_RECENT_FILE.set(path);
                    Files.readAllLines(path).forEach(s -> {
                        if (Character.isLetter(s.charAt(0))) activeMap.set(MAP.get(s));
                        else {
                            int equalsIndex = s.indexOf("=");
                            activeMap.get().put(Long.parseLong(s.substring(0, equalsIndex)), s.substring(equalsIndex + 1));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void setValue(String table, IDiscordObject object, String value) {
        MAP.get(table).put(object.getLongID(), value);
    }

    public static void resetValue(String table, IDiscordObject object) {
        MAP.get(table).remove(object.getLongID());
    }

    public static String getValue(String table, IDiscordObject user, String defaul) {
        return MAP.get(table).getOrDefault(user.getLongID(), defaul);
    }

    private static Path getSaveFilePath() {
        Path path = Paths.get("data" + File.separator + System.currentTimeMillis() + ".txt");
        path.getParent().toFile().mkdirs();
        return path;
    }
}
