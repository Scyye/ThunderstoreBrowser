package dev.scyye.thunderstorebot.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.interactions.autocomplete.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import dev.scyye.botcommons.command.slash.SlashCommandEvent;
import dev.scyye.botcommons.config.ServerConfig;
import dev.scyye.botcommons.menu.PaginatedMenuHandler;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Interaction
public class Commands {

	static List<String> communities;

	static {
		try {
			communities = Arrays.stream(Bot.bot.tsja.retrieveCommunities(Path.of("tsja-cache", "communitiy-cache.json")))
					.map(Community::getIdentifier)
					.collect(Collectors.toList());
		} catch (IOException | URISyntaxException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Commands() throws IOException, URISyntaxException, InterruptedException {
	}

	@AutoComplete(value = {"package search", "package author", "package info"})
	public void onAutocompleteCommunity(AutoCompleteEvent event) {
		if (event.getName().equals("community")) {
			event.replyChoices(communities.stream()
					.filter(community -> community.contains(event.getValue()))
					.map(community -> new Command.Choice(community, community))
					.toList());
			return;
		}

		event.replyChoices();
	}

	@SlashCommand(value = "package search")
	public void onPackageGet(CommandEvent event,
							 @Param(name = "community", value = "The community to retrieve from") String community,
							 @Optional @Param(name = "search", value = "The search") String search) {
		try {
			community = handleCommunityConfig(event, community);

			PackageListing[] result;
			if (!checkExecution(event)) return;

			if (search != null) {
				if (search.length() > 100) {
					event.reply("Search query too long.");
					return;
				}

				if (community.length() > 100) {
					event.reply("Community name too long.");
					return;
				}
			}

			search = sanitizeAndReplace(search);

			if (!validateCommunity(event, community))
				return;

			community = community.toLowerCase().replace(" ", "-");

			result = search != null ?
					TSJAUtils.getPackagesByName(Bot.bot.tsja, community, search) :
					Bot.bot.tsja.getPackages(community, null);

			List<PaginatedMenuHandler.Page> pages = buildPages(result, search, community);

			SlashCommandEvent.from(event).replyMenu(pages.stream().map(page -> page.content).toList().toArray(MessageCreateData[]::new));
		} catch (Exception e) {
			e.printStackTrace();
			event.reply("An error occurred while executing this command.");
		}
	}

	// Add other methods here...

	private String handleCommunityConfig(CommandEvent event, String community) {
		if (!ServerConfig.configs.get(event.getGuild().getId()).get("community", String.class).isEmpty())
			community = ServerConfig.configs.get(event.getGuild().getId()).get("community", String.class);

		return community;
	}

	private boolean checkExecution(CommandEvent event) {
		return TestCommands.checkExecute(event, e ->
				!ServerConfig.configs.get(event.getGuild().getId()).get("disabledChannels", List.class).contains(e.getChannel().getId())
						&& !ServerConfig.configs.get(event.getGuild().getId()).get("disabledUsers", List.class).contains(e.getUser().getId()));
	}

	private String sanitizeAndReplace(String input) {
		return (input == null) ? null:MarkdownSanitizer.sanitize(input).replace(" ", "_");
	}

	private boolean validateCommunity(CommandEvent event, String community) {
		if (community == null || community.isEmpty())
			event.reply("No community provided. How did you even get here?");

		community = MarkdownSanitizer.sanitize(community.toLowerCase().replace(" ", "-"));

		final String finalCommunity = community;
		boolean success = Arrays.stream(Bot.bot.tsja.getCommunities())
				.anyMatch(c -> c.getIdentifier().equals(finalCommunity));

		if (!success)
			event.reply("Invalid Community. Do `/community all` for a list of valid communities.");

		return success;
	}

	private List<PaginatedMenuHandler.Page> buildPages(PackageListing[] result, String search, String community) {
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

		return pages;
	}
}
