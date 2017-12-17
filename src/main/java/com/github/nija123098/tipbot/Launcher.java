package com.github.nija123098.tipbot;

public class Launcher {
    public static void main(String[] args) {
        if (args.length == 0) Bot.initialize();
        else Decrypt.decrypt(args);
    }
}
