package com.github.nija123098.tipbot.command;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand {
    public List<String> getNames() {
        return Collections.singletonList(this.getClass().getSimpleName().toLowerCase().replace("command", ""));
    }

    public abstract String getHelp();

    public String getFullHelp() {
        return getHelp();
    }

    public abstract Command getCommand();
}
