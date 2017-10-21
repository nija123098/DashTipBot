package com.github.nija123098.tipbot.commands;

import com.github.nija123098.tipbot.AbstractCommand;
import com.github.nija123098.tipbot.Main;

public class RequestCommand extends AbstractCommand {
    @Override
    public String getHelp() {
        return "Sends a request to the maintainer.";
    }

    @Override
    public Main.Command getCommand() {
        return (invoker, arguments) -> {
            Main.MAINTAINER.getOrCreatePMChannel().sendMessage("User " + invoker.mention() + " requested " + join(arguments));
            return "I sent someone important a message!";
        };
    }
    private static String join(String[] args){
        StringBuilder builder = new StringBuilder();
        for (String s : args) builder.append(s).append(" ");
        return builder.toString();
    }
}
