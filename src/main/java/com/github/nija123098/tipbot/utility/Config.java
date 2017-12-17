package com.github.nija123098.tipbot.utility;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    public static String TOKEN;
    public static String ENCRYPTION_KEY;
    public static String ENCRYPTION_ALGORITHM;
    public static String REQUIRED_CONFIRMATIONS;

    public static void setUp() throws IOException {
        Map<String, String> map = Files.readAllLines(Paths.get("config.cfg"), Charset.forName("UTF-8")).stream().collect(Collectors.toMap((string) -> string.substring(0, string.indexOf("=")).toUpperCase(), (string) -> string.substring(string.indexOf("=") + 1, string.length())));
        Stream.of(Config.class.getFields()).forEach((field -> {
            try {
                field.set(null, map.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Exception loading config, improperly formed class file", e);
            }
        }));
    }
}
