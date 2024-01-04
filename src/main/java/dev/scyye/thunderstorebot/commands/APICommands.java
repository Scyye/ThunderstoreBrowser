package dev.scyye.thunderstorebot.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.interactions.autocomplete.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import dev.scyye.botcommons.config.ServerConfig;
import dev.scyye.botcommons.menu.PaginatedMenuHandler;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstoreapi.exceptions.NoSuchCommunityException;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.utils.Constants;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class APICommands {

    List<String> communities = Stream.of(Bot.bot.tsja.getCommunities())
            .map(Community::getIdentifier)
            .toList();

    @AutoComplete(value = {"package search", "package author", "package info"})
    public void onAutocompleteCommunity(AutoCompleteEvent event) {
        if (event.getName().equals("community")) {
            event.replyChoices(communities.stream().filter(
                            community -> community.contains(event.getValue()))
                    .map(community -> new Command.Choice(community, community))
                    .toList()); return;
        }

        event.replyChoices();
    }

    @SlashCommand(value = "package search")
    public void onPackageGet(CommandEvent event,
                             @Param(name = "community", value = "The community to retrieve from") String community,
                             @Optional @Param(name = "search", value = "The search") String search) {
        if (!ServerConfig.configs.get(event.getGuild().getId()).get("community").equals(""))
            community = ServerConfig.configs.get(event.getGuild().getId()).get("community", String.class);

        PackageListing[] result;

        if (!TestCommands.checkExecute(event, e ->
                !ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
                && !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId())))
            return;

        if (search!=null)
            if (search.length()>100) {
                event.reply("Search query too long.");
                return;
            }

        if (community!=null)
            if (community.length()>100) {
                event.reply("Community name too long.");
                return;
            }

        if (search!=null)
            search = MarkdownSanitizer.sanitize(search).replace(" ", "_");



        if (community!=null) {
            community = community.toLowerCase().replace(" ", "-");
            community = MarkdownSanitizer.sanitize(community);

            boolean success = false;

            for (var c : Bot.bot.tsja.getCommunities()) {
                if (c.getIdentifier().equals(community)) {
                    success=true;
                    break;
                }
            }

            if (!success) {
                event.reply("Invalid Community. Do `/community all` for a list of valid communities.");
                return;
            }
        } else {
            community = "all";
        }

        if (search != null) {
            result = new TSJAUtils().getPackagesByName(Bot.bot.tsja, community, search);
        } else {
            result = Bot.bot.tsja.getPackages(community, null);
        }

        PaginatedMenuHandler.Menu menu;
        List<PaginatedMenuHandler.Page> pages = new ArrayList<>();
        PaginatedMenuHandler.Page currentPage =
                new PaginatedMenuHandler.Page(MessageCreateData.fromContent("# Mods\nSearch: " + search + "\n\n\n"));

        StringBuilder builder = new StringBuilder();

        for (PackageListing p : result) {
            if (builder.length() > 1500) {
                currentPage.append(builder.toString()); // Append builder to the current page
                builder.setLength(0); // Clear the builder
                pages.add(currentPage); // Add the current page to the list of pages
                currentPage = new PaginatedMenuHandler.Page(MessageCreateData.fromContent("")); // Create a new page
            }

            String packageName = p.isDeprecated() ? "~~" + p.getName() + "~~" : p.getName();
            String packageLink = MarkdownUtil.maskedLink(packageName, "<" + p.getPackageUrl() + ">");
            String ownerLink = MarkdownUtil.maskedLink(p.getOwner(), "<" + TSJA.getTeamUrl(community, p.getOwner()) + ">");
            String downloadLink = MarkdownUtil.maskedLink("here", "<" + p.getVersions()[0].getDownloadUrl() + ">");
            String content = String.format("%s by %s, download %s (%s)\n", packageLink, ownerLink, downloadLink, p.getUniqueId());
            builder.append(content);
        }

        currentPage.append(builder.toString());
        pages.add(currentPage);

        event.reply("MENU LOADING...", msg -> {
            PaginatedMenuHandler.addMenu(PaginatedMenuHandler.buildMenu(msg, pages.stream().map(page -> page.content).toList().toArray(
                    MessageCreateData[]::new)));
        });
    }

    @SlashCommand(value = "package info")
    public void onPackageInfo(CommandEvent event, @Param(name = "community", value = "The community to retrieve from") String community,
                              @Param(name = "uuid", value = "The UUID to get info about.") String uuidString) {
        if (!ServerConfig.configs.get(event.getGuild().getId()).get("community").equals(""))
            community = ServerConfig.configs.get(event.getGuild().getId()).get("community", String.class);

        if (!TestCommands.checkExecute(event, e ->
                !ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
                        && !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId())))
            return;

        if (community!=null)
            if (community.length()>100) {
                event.reply("Community name too long.");
                return;
            }

        try {
            UUID.fromString(uuidString);
        } catch (Exception ignored) {
            event.reply("Invalid UUID Format.");
            return;
        }
        boolean success = false;

        for (var c : Bot.bot.tsja.getCommunities()) {
            if (c.getIdentifier().equals(community)) {
                success=true;
                break;
            }
        }

        if (!success) {
            event.reply("Invalid Community. Do `/community all` for a list of valid communities.");
            return;
        }
        PackageListing _package = null;
        try {
            _package = new TSJAUtils().getPackageById(Bot.bot.tsja, community, UUID.fromString(uuidString));
        } catch (Exception ignored) {}

        if (_package==null) {
            event.reply("Invalid UUID.");
            return;
        }

        event.reply("""
                Name: %s
                Owner: %s
                Donate: %s
                Description: %s
                Url: %s
                UUID: %s
                Rating: %s
                
                Pinned: %s
                Deprecated: %s
                NSFW: %s
                
                Categories: %s
                Latest Version: %s
                """, _package.getName(), _package.getOwner(), "<"+_package.getDonationLink()+">", _package.getVersions()[0].getDescription(), _package.getPackageUrl(), _package.getUniqueId(), _package.getRatingScore(),
                _package.isPinned(), _package.isDeprecated(), _package.hasNsfwContent(), Arrays.toString(_package.getCategories()),
                _package.getVersions()[0].getVersionNumber());
    }

    @SlashCommand(value = "package author")
    public void onPackageAuthor(CommandEvent event,
                                @Param(name = "author", value = "The author to filter by") String author,
                                @Param(name = "community", value = "The community to retrieve from") String community) {
        if (!TestCommands.checkExecute(event, e ->
                !ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
                        && !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId())))
            return;

        if (!ServerConfig.configs.get(event.getGuild().getId()).get("community").equals(""))
            community = ServerConfig.configs.get(event.getGuild().getId()).get("community", String.class);

        PackageListing[] result;
        if (author!=null)
            author = MarkdownSanitizer.sanitize(author);



        community = community.toLowerCase().replace(" ", "-");
        community = MarkdownSanitizer.sanitize(community);

        result = author==null?Bot.bot.tsja.getPackages(community, null):new TSJAUtils().getPackagesByAuthor(Bot.bot.tsja, community, author);

        StringBuilder builder = new StringBuilder("# Mods\nby " + author + "\n\n\n");

        // loop through results
        for (PackageListing p : result)
            builder.append(String.format("%s, download %s (%s)\n",
                    MarkdownUtil.maskedLink(p.isDeprecated() ? "~~" + p.getName() + "~~" : p.getName(), "<" + p.getPackageUrl() + ">"),
                    MarkdownUtil.maskedLink("here", "<" + p.getVersions()[0].getDownloadUrl() + ">"),
                    p.getUniqueId()));

        event.reply("MENU LOADING...", message -> PaginatedMenuHandler.addMenu(
                PaginatedMenuHandler.buildMenu(message, MessageCreateData.fromContent(builder.toString()))));
    }

    @SlashCommand(value = "community all")
    public void onCommunityListAll(CommandEvent event) {
        if (!TestCommands.checkExecute(event, e ->
                !ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
                        && !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId())))
            return;

        ServerConfig config = ServerConfig.configs.get(event.getGuild().getId());

        StringBuilder builder = new StringBuilder("# Communities\n\n" +
                (!config.get("community").equals("") ? "Current community: " + config.get("community", String.class) + "\n\n" : ""
            ));
        for (var community : Bot.bot.tsja.getCommunities())
            builder.append(String.format("%s (%s)\n", community.getName(), community.getIdentifier()));

        event.reply("MENU LOADING...", message -> PaginatedMenuHandler.addMenu(
                PaginatedMenuHandler.buildMenu(message, builder.toString(), 400)));
    }

    @SlashCommand(value = "community info")
    public void onCommunityInfo(CommandEvent event, @Param(name = "community", value = "The community to gather info about") String c) throws NoSuchCommunityException {
        if (!TestCommands.checkExecute(event, e ->
                !ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
                        && !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId())))
            return;

        Community community = new TSJAUtils().getCommunityByIdentifier(Bot.bot.tsja, c).orElse(null);

        if (community==null) {
            event.reply(Constants.incorrectCommunityResponse(c));
            return;
        }

        PackageListing[] packages = Bot.bot.tsja.getPackages(c, null);

        event.reply(String.format("""
                ID: %s
                Name: %s
                
                Discord: <%s>
                Wiki: <%s>
                
                Require Package Approval: %s
                Total packages: %s
                Latest package: %s
                """, community.getIdentifier(), community.getName(), community.getDiscordUrl(), community.getWikiUrl(),
                community.isRequirePackageListingApproval(), packages.length, packages[0].getName() + "("+packages[0].getUniqueId()+")"));
    }
}
