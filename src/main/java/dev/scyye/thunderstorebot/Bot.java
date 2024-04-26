package dev.scyye.thunderstorebot;

import dev.scyye.botcommons.commands.CommandManager;
import dev.scyye.botcommons.config.Config;
import dev.scyye.botcommons.config.ConfigManager;
import dev.scyye.botcommons.menu.MenuManager;
import dev.scyye.botcommons.menu.impl.HelpMenu;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJABuilder;
import dev.scyye.thunderstorebot.cache.CacheCollector;
import dev.scyye.thunderstorebot.command.impl.*;
import dev.scyye.thunderstorebot.utils.SuggestionListener;
import dev.scyye.thunderstorebot.versions.Version;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.internal.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Bot extends ListenerAdapter {
    public static Bot bot;
    public TSJA tsja;

    public JDA jda;

    Config config;

    private Bot() {
        Config.botName="thunderstorebot";
        config = Config.makeConfig(new HashMap<>(){{
            put("token", "TOKEN");
        }});

        tsja = new TSJABuilder()
                .setUpdateCacheTime(10000)
                .build();
        jda = JDABuilder.createDefault(config.get("token"))
                .setActivity(Activity.customStatus("DM suggestions to me!"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new SuggestionListener(), this, new CommandManager())
                .build();

        jda.addEventListener(new MenuManager(jda));
        jda.addEventListener(new ConfigManager(new HashMap<>(){{
            put("prefix", "!");
            put("disabledChannels", "[]");
            put("disabledUsers", "[]");
            put("moderatorRoles", "[]");
            put("community", "");
        }}));

        CommandManager.addSubcommands(new PackageCommand(), new PackageCommand.PackageInfoCommand(), new PackageCommand.PackageSearchCommand());
        CommandManager.addSubcommands(new CommunityCommand(), new CommunityCommand.CommunityInfoCommand(), new CommunityCommand.CommunityListCommand());
        MenuManager.registerMenu(new PackageCommand.PackageSearchCommand.PackageSearchMenu(), new LogParseCommand.PluginList(),
                new CommunityCommand.CommunityListCommand(), new MiscCommands.ChangelogCommand(), new HelpMenu());
        CommandManager.addCommands(new LogParseCommand(), new AddInviteCommand(),
                new LogParseCommand.PluginList(),
                new MiscCommands.PingCommand(), new MiscCommands.EchoCommand(), new MiscCommands.ChangelogCommand(),
                new MiscCommands.VersionCommand(), new MiscCommands.CreditsCommand(), new MiscCommands.InviteCommand(),
                new HelpMenu());
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        event.getJDA().retrieveUserById(event.getMessageAuthorId()).queue(user -> {
            if (!event.getUserId().equals("553652308295155723"))
                return;

            if (event.getReaction().getEmoji().asUnicode().equals(Emoji.fromUnicode("U+274C"))) {
                if (!user.isBot() && !event.getUserId().equals("553652308295155723"))
                    return;
                event.getChannel().deleteMessageById(event.getMessageId()).queue();
            }
            // if its a pin emoji, pin the message
            System.out.println(event.getReaction().getEmoji().getName());
            if (event.getReaction().getEmoji().getName().contains("\uD83D\uDCCC")) {
                System.out.println("2");
                event.getChannel().retrievePinnedMessages().queue(messages -> {
                    if (messages.stream().map(Message::getId).toList().contains(event.getMessageId())) {
                        event.getChannel().unpinMessageById(event.getMessageId()).queue();
                        System.out.println("Unpinned message");
                    } else {
                        event.getChannel().pinMessageById(event.getMessageId()).queue();
                        System.out.println("Pinned message");
                    }
                });
            }
        });
    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        System.out.println("Ready in " + event.getGuild().getName());
    }

    public static void main(String[] args) {
        bot = new Bot();
        //JDACommands.start(bot.jda, bot.getClass(), APICommands.class.getPackageName());

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                CacheCollector.init();
            }
        };

        // Every 20 minutes, update the cache
        timer.schedule(task, 1000 * 60 * 20, 1000 * 60 * 2);
        CacheCollector.init();

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
        new Version("1-4-2024", "1.1.0", """
                * Changed command framework
                * Added more commands
                * Changed the way package-search works
                """, true);
        new Version("14-4-2024", "1.1.1", """
                * Added caching, allowing for faster autocomplete
                * Changed package-info, and package-search to subcommands of `package`
                """, true);
        new Version("23-4-2024", "1.1.2", """
                * Security updates (thanks <@429810730691461130>
                * Changed community-info and community-list to subcommands of `community`
                """ , true);
    }
}
