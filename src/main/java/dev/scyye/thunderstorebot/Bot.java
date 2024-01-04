package dev.scyye.thunderstorebot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.scyye.botcommons.config.Config;
import dev.scyye.botcommons.config.ServerConfig;
import dev.scyye.botcommons.menu.PaginationListener;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJABuilder;
import dev.scyye.thunderstorebot.commands.APICommands;
import dev.scyye.thunderstorebot.utils.SuggestionListener;
import dev.scyye.thunderstorebot.versions.Version;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Bot extends ListenerAdapter {
    public static Bot bot;
    public TSJA tsja;

    public JDA jda;

    Config config;

    private Bot() {
        config = Config.makeConfig(new HashMap<>(){{
            put("token", "TOKEN");
        }}, "thunderstorebot");

        tsja = new TSJABuilder()
                .setUpdateCacheTime(10000)
                .build();
        jda = JDABuilder.createDefault(config.get("token"))
                .setActivity(Activity.customStatus("DM suggestions to me!"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new SuggestionListener(), this)
                .build();

        jda.addEventListener(new PaginationListener(jda));
    }

    public static void main(String[] args) {
        bot = new Bot();
        JDACommands.start(bot.jda, bot.getClass(), APICommands.class.getPackageName());



        new Version("23-10-2023", "1.0.0", """
                * Created bot
                  * Added API commands
                  * Added admin commands
                * Added suggestion system
                * Added config system
                * Added menus
                """, true);
        new Version("23-10-2023", "1.0.1", """
                * Fixed the bot not letting you react to messages
                * Hopefully fixed an issue regarding rate limits
                """, true);
        new Version("27-10-2023", "1.0.2", """
                * Added /admin rename to rename the bot
                * Added `Poppycars` and `Anarkey` to the credits
                * Fixed reported issues
                """, true);
        new Version("29-10-2023", "1.0.3", """
                * Updated how configs work
                  * Added a new config for default community
                  * Added a command to change the default community
                * Added `Root` to the credits
                * Fixed reported issues
                """, true);
        new Version("29-10-2023", "1.0.3", """
                * Updated TSJA
                * Allowed cleaning of bot messages easily
                * Added `Ascyst` to the credits
                """, true);
        new Version("12-11-2023", "1.0.4", """
                * Updated Dependencies
                 * Started using [BotCommons](https://github.com/Scyye/BotCommons)
                * Fix issue with config
                """, true);
        new Version("25-11-2023", "1.0.5", """
                * Bot is back online!
                """, true);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        ServerConfig.createConfig("thunderstorebot", event.getGuild().getId(),
                new HashMap<>(){{
                    put("disabledChannels", new String[]{});
                    put("disabledUsers", new String[]{});
                    put("moderatorRoles", new String[]{});
                    put("community", "");
                }});

    }
}
