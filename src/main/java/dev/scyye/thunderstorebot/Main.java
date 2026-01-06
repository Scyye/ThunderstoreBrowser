package dev.scyye.thunderstorebot;

import botcommons.commands.CommandManager;
import botcommons.config.Config;
import botcommons.config.ConfigManager;
import botcommons.menu.MenuManager;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJABuilder;
import dev.scyye.thunderstorebot.command.impl.*;
import dev.scyye.thunderstorebot.utils.CommandUtils;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Main extends ListenerAdapter {
    public static Main instance;
    public TSJA tsja;

    public JDA jda;

    private Main() {
        instance = this;
		Config.makeConfig(new HashMap<>(), "thunderstorebot");


        jda = JDABuilder.createDefault(Config.getInstance().get("token"))
                .setActivity(Activity.customStatus("DM suggestions to me!"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new SuggestionListener(), this)
                .build();

        jda.addEventListener(new MenuManager(jda));

        ConfigManager manager = new ConfigManager("thunderstorebot", jda);
        ConfigManager.Config defaultConfig = new ConfigManager.Config();
        defaultConfig.put("disabledChannels", new String[0]);
        defaultConfig.put("disabledUsers", new String[0]);
        defaultConfig.put("moderatorRoles", new String[0]);
        defaultConfig.put("community", "");

        manager.setDefault(defaultConfig);

        CommandManager.init(jda, (event -> {
            if (event.getUserId().equals(Config.getInstance().get("owner-id")))
                return true;
			return CommandUtils.checkExecution(event);
		}));
        CommandManager.addCommands(
                AddInviteCommand.class,
                CommunityCommand.class,
                LogParseCommand.class,
                MiscCommands.class,
                PackageCommand.class,
                AdminCommands.class,
                ProfileCommand.class,
                ContextCommands.class
        );

        MenuManager.registerMenu(new PackageCommand.PackageSearchMenu(), new LogParseCommand.PluginList(),
                new CommunityCommand.CommunityListMenu(), new MiscCommands.ChangelogCommand());


        tsja = new TSJABuilder().build($ ->
                System.out.println("Successfully connected to Thunderstore API"));
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.getUserId().equals("553652308295155723"))
            return;

        if (event.getReaction().getEmoji().getType().equals(Emoji.Type.CUSTOM)) {
            return;
        }

        if (event.getReaction().getEmoji().asUnicode().equals(Emoji.fromUnicode("U+274C"))) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue($ -> {}, $ -> System.out.println("Failed to delete message"));
        }
        // if its a pin emoji, pin the message
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
    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        System.out.printf("\nReady in %s", event.getGuild().getName());
    }

    public static void main(String[] ignoredArgs) {
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
        new Version("1-4-2024", "1.0.6", """
                * Changed command framework
                * Added more commands
                * Changed the way package-search works
                """, true);
        new Version("14-4-2024", "1.0.7", """
                * Added caching, allowing for faster autocomplete
                * Changed package-info, and package-search to subcommands of `package`
                """, true);
        new Version("23-4-2024", "1.0.8", """
                * Security updates (thanks <@429810730691461130>)
                * Changed community-info and community-list to subcommands of `community`
                """ , true);
        new Version("7-8-2024", "1.1.0", """
                * General fixes
                * Added `soup` command (blame justin)
                * Added admin commands back
                """, false);
        new Version("24-8-2024", "1.1.1", """
                * Fixed community autocomplete
                * Fixed the community and package commands
                * Added profile command
                * General fixes
                """, false);
        new Version("29-8-2024", "1.1.2", """
                * Updated BotCommons
                * Use loose matching in autocomplete.
                """, false);
        new Version("5-1-2026", "1.2", """
                ## KNOWN ISSUE WITH SERVER ENFORCED CHANNEL AND USER BANS
                * USER INSTALLABLE IS HERE LETS GOOOO!!!! (thanks <@704757796599496714> for the motivation)
                * Added `Atomic();` to the credits for helping with testing and ideas.
                * Performance improvements, primarily with caching. (TSJA update soon:tm:)
                * Fixed a bug with DM commands not working.
                * Added /profile command to see info about a modpack profile.
                * Moving to a seperate bot for beta releases, this bot will be stable releases only. after 1.3.
                * Context menu commands for profile commands. Using jda-commands until BotCommons supports it.
                """, true);
        new Main();
    }
}
