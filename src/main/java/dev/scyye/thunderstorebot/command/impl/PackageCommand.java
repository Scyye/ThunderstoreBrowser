package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.*;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.cache.CacheCollector;
import dev.scyye.thunderstorebot.utils.CommandUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

import static dev.scyye.thunderstorebot.utils.CommandUtils.*;

@CommandHolder(group = "package")
public class PackageCommand {

	@Command(name = "info", help = "Get info about a package")
	public static void info(GenericCommandEvent event,
							@NotNull @Param(description = "The community to search", autocomplete = true) String community,
							@NotNull @Param(description = "The UUID of the package", autocomplete = true) String uuid) {
		event.deferReply();
		String uuidString = uuid.split(" ")[0];


		if (community.length()>100) {
			event.replyError("Community name too long.").finish();
			return;
		}

		try {
			UUID.fromString(uuidString);
		} catch (Exception ignored) {
			event.replyError("Invalid UUID Format.").finish();
			return;
		}

		if (CommandUtils.validateCommunity(event, community))
			return;

		PackageListing _package = null;
		try {
			_package = TSJAUtils.getPackageById(Bot.bot.tsja, community, UUID.fromString(uuidString));
		} catch (Exception ignored) {}

		if (_package==null) {
			event.reply("Invalid UUID.");
			return;
		}

		event.replyEmbed(new EmbedBuilder()
				.setColor(Color.green)
				.setImage(_package.getVersions()[0].getIcon())
				.setDescription("""
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
                Latest Update Date: %s
                Initial Upload Date: %s
                """.formatted(_package.getName(), _package.getOwner(), "<"+_package.getDonationLink()+">", _package.getVersions()[0].getDescription(), _package.getPackageUrl(), _package.getUniqueId(), _package.getRatingScore(),
						_package.isPinned(), _package.isDeprecated(), _package.hasNsfwContent(), Arrays.toString(_package.getCategories()),
						_package.getVersions()[0].getVersionNumber(), STR."<t:\{_package.getDateUpdated().toInstant().getEpochSecond()}:R>",
						STR."<t:\{_package.getDateCreated().toInstant().getEpochSecond()}:R>"))).finish();
	}

	@AutoCompleteHandler("package info")
	public static void handleInfoAutocomplete(CommandAutoCompleteInteractionEvent event) {
		switch (event.getFocusedOption().getName()) {
			case "community" -> {
				event.replyChoiceStrings(
						CacheCollector.getCommunityAutocomplete(event.getFocusedOption().getValue())
				).queue();
			}
			case "uuid" -> {
				String community = event.getOption("community").getAsString();
				var packages = CacheCollector.communityPackageCache.get(community);

				event.replyChoiceStrings(
						packages.stream()
								.filter(p -> p.getFullName().startsWith(event.getFocusedOption().getValue()))
								.limit(25)
								.map(p -> STR."\{p.getUniqueId().toString()} (\{p.getFullName()})")
								.toList()
				).queue();
			}
			default -> {
				event.replyChoiceStrings("How did you get here?").queue();
			}
		}
	}


	@Command(name = "search", help = "Search for a package on Thunderstore")
	public static void search(GenericCommandEvent event,
							  @Param(description = "The community to search", autocomplete = true) String community,
							  @Param(description = "The search to.. search", required = false, autocomplete = true) String search,
							  @Param(description = "Filter by the author", required = false, autocomplete = true) String author,
							  @Param(description = "Filter by dependencies", required = false, autocomplete = true) String depends) {
		event.deferReply();
		List<PackageListing> result;
		if (!checkExecution(event)) return;

		if (author == null)
			author = "null";
		if (depends == null)
			depends = "null";
		if (search == null)
			search = "null";

		if (!search.equals("null")) {
			if (search.length() > 100) {
				event.reply("Search query too long.").finish();
				return;
			}
		}

		search = sanitizeAndReplace(search);

		if (validateCommunity(event, community))
			return;

		community = community.toLowerCase().replace(" ", "-");

		if (!author.equals("null"))
			author = MarkdownSanitizer.sanitize(author);

		community = community.toLowerCase().replace(" ", "-");
		community = MarkdownSanitizer.sanitize(community);

		result = Arrays.stream(Bot.bot.tsja.getPackages(community, 0)).toList();

		if (!search.equals("null")) {
			String finalSearch = search;
			result = result.stream().filter(p -> p.getName().toLowerCase().contains(finalSearch.toLowerCase())).toList();
		}

		if (!author.equals("null")) {
			String finalAuthor = author;
			result = result.stream().filter(p -> p.getOwner().toLowerCase().contains(finalAuthor.toLowerCase())).toList();
		}

		if (!depends.equals("null")) {
			String finalDepends = depends;
			result = result.stream().filter(p -> Arrays.stream(p.getVersions()[0].getDependencies()).anyMatch(dep -> dep.contains(finalDepends))).toList();
		}

		event.replyMenu("package-search-menu", result.toArray(PackageListing[]::new), search +
				(author.equals("null")? "":"\nBy: " + author), community).finish();
	}

	@AutoCompleteHandler("package search")
	public void handleSearchAutocomplete(CommandAutoCompleteInteractionEvent event) {
		switch (event.getFocusedOption().getName()) {
			case "community" -> {
				event.replyChoiceStrings(
						CacheCollector.getCommunityAutocomplete(event.getFocusedOption().getValue())
				).queue();
			}
			case "search", "depends" -> {
				String community = event.getOption("community").getAsString();
				var packages = CacheCollector.communityPackageCache.get(community);

				event.replyChoiceStrings(
						packages.stream()
								.filter(p -> p.getName().toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
								.limit(25)
								.map(PackageListing::getName)
								.toList()
				).queue();
			}
			case "author" -> {
				String community = event.getOption("community").getAsString();
				var authors = CacheCollector.communityAuthorCache.get(community);

				event.replyChoiceStrings(
						authors.stream()
								.filter(a -> a.toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()))
								.limit(25)
								.toList()
				).queue();
			}
			default -> {
				event.replyChoiceStrings("What the fuck. How are you seeing this? if you are, ping/DM @scyye with more info, ty").queue();
			}
		}
	}

	@Menu(id="package-search-menu")
	public static class PackageSearchMenu extends PageMenu {
		public PackageListing[] result;
		public String search;
		public String community;

		public PackageSearchMenu() {

		}

		public PackageSearchMenu(PackageListing[] result, String search, String community) {
			this.result = result;
			this.search = search;
			this.community = community;
		}

		@Override
		public List<EmbedBuilder> getPageData() {
			return buildPages(result, search, community);
		}

		private List<EmbedBuilder> buildPages(PackageListing[] result, String search, String community) {
			List<EmbedBuilder> pages = new ArrayList<>();

			String footer = "Community: " + community;
			if (!search.equals("null"))
				footer += "\nSearch: "+search;

			EmbedBuilder currentPage =
					new EmbedBuilder()
							.setTitle("Mods Page 1/"+result.length/5)
							.setFooter(footer)
							.setColor(0x00ff00);

			int onPage = 0;
			int page = 1;
			for (PackageListing p : result) {
				if (onPage == 5) {
					onPage = 0;
					pages.add(currentPage);
					page = page + 1;
					currentPage = new EmbedBuilder()
							.setTitle("Mods Page " + page + STR."/\{result.length / 5}")
							.setFooter("Search: " + search)
							.setColor(0x00ff00);
				}
				String packageName = MarkdownUtil.underline(p.isDeprecated() ? STR."~~\{p.getName()}~~" : p.getName());
				String ownerLink = MarkdownUtil.maskedLink(p.getOwner(), STR."https://thunderstore.io/c/\{community}/p/\{p.getOwner()}/");
				String downloadLink = MarkdownUtil.maskedLink("here", STR."<\{p.getVersions()[0].getDownloadUrl()}>");

				currentPage.addField(packageName, STR."Page: \{p.getPackageUrl().toString()}\nCreated By: \{ownerLink}\nDownload: \{downloadLink}", false);

				onPage++;
			}
			pages.add(currentPage);

			return pages;
		}
	}
}
