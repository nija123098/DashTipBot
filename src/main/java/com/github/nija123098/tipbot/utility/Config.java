package com.github.nija123098.tipbot.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    public static String DB_USER = "user";
    public static String DB_PASS = "pass";
    public static String DB_HOST = "host";
    public static String DB_NAME = "name";
    public static String DB_PORT = "port";
    public static String TOKEN;
    public static Boolean setUp() throws IOException {
        Map<String, String> map = Files.readAllLines(Paths.get("config.cfg")).stream().collect(Collectors.toMap((string) -> string.substring(0, string.indexOf("=")), (string) -> string.substring(string.indexOf("=") + 1, string.length())));
        Stream.of(Config.class.getFields()).forEach((field -> {
            try {
                field.set(null, map.get(field.getName().toLowerCase()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));
        return null;
    }
}
