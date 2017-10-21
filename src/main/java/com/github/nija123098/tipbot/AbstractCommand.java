package com.github.nija123098.tipbot;

public abstract class AbstractCommand {
    public String getName(){
        return this.getClass().getSimpleName().toLowerCase().replace("command", "");
    }
    public abstract String getHelp();
    public String getFullHelp(){
        return getHelp();
    }
    public abstract Main.Command getCommand();
}
