package com.github.nija123098.tipbot.utility;

import com.github.nija123098.tipbot.Bot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.nija123098.tipbot.utility.Database.*;

public class FormatHelper {
    private static final String BOT_MENTION;
    private static final String BOT_MENTION_SECOND;

    static {
        BOT_MENTION = Bot.DISCORD_CLIENT.getOurUser().mention(true);
        BOT_MENTION_SECOND = Bot.DISCORD_CLIENT.getOurUser().mention(false);
    }

    private static final Matcher MATCHER = Pattern.compile("([0-9])+").matcher("");
    public static IUser getUserFromMention(String mention) {
        try {
            if (!FormatHelper.MATCHER.reset(mention).find()) return null;
            return Bot.DISCORD_CLIENT.getUserByID(Long.parseLong(FormatHelper.MATCHER.group()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getCommandContent(MessageReceivedEvent event) {
        String content = event.getMessage().getContent();
        String prefix = event.getChannel().isPrivate() ? null : getValue(PREFIXES, event.getGuild(), "~");
        if (prefix != null && content.startsWith(prefix)) return content.substring(prefix.length());
        else if (content.startsWith(BOT_MENTION)) return content.substring(FormatHelper.BOT_MENTION.length());
        else if (content.startsWith(BOT_MENTION_SECOND)) return content.substring(FormatHelper.BOT_MENTION_SECOND.length());
        else if (event.getChannel().isPrivate()) {
            for (int i = 0; i < content.length(); i++) if (Character.isLetter(content.charAt(i))) return content.substring(i);
            return null;
        } else return null;
    }

    public static String removeExtraSpace(String content){
        int length, newLength;
        do {
            length = content.length();
            content = content.replace("  ", " ");
            newLength = content.length();
        } while (length != newLength);
        return content.startsWith(" ") ? content.substring(1) : content;
    }

    public static Double softParseDouble(String s){
        try {
            double val = Double.parseDouble(s);
            if (Bot.DISCORD_CLIENT.getUserByID((long) val) != null) return null;
            return val;
        } catch (NumberFormatException e){
            return null;
        }
    }
}
