package com.github.nija123098.tipbot.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class TransationLog {
    private static final Path PATH;
    static {
        PATH = Paths.get("Transaction-Log-" + System.currentTimeMillis());
    }
    public static void log(String log) throws IOException {
        Files.write(PATH, Collections.singletonList(log), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
}
