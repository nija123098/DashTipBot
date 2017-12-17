package com.github.nija123098.tipbot.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class TransactionLog {
    private static final Path PATH;

    static {
        PATH = Paths.get("transactions" + File.separator + System.currentTimeMillis() + ".txt");
        PATH.getParent().toFile().mkdirs();
    }

    public static void log(String log) throws IOException {
        Files.write(PATH, Collections.singletonList(log), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
}
