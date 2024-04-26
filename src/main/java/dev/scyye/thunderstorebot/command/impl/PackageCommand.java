package dev.scyye.thunderstorebot.command.impl;

import com.google.gson.GsonBuilder;
import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.cache.CacheCollector;
import dev.scyye.thunderstorebot.utils.CommandUtils;
import dev.scyye.thunderstorebot.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

import static dev.scyye.thunderstorebot.utils.CommandUtils.*;

@Command(name = "package", help="Get info about a package")
public class PackageCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent event) {
		event.replyError("Invalid usage. Use either `/package info` or `/package search`.");
	}

	@Command(name = "info", help = "Get info about a package")
	public static class PackageInfoCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.deferReply();
			String community = event.getArg(0, String.class);
			String uuidString = event.getArg(1, String.class);
			boolean raw = event.getArg(2, Boolean.class);
			uuidString = uuidString.split(" ")[0];

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

			if (raw) {
				String json = new GsonBuilder().setPrettyPrinting().create().toJson(_package);
				List<EmbedBuilder> replies = new ArrayList<>();
				while (json.length() > 2000) {
					replies.add(new EmbedBuilder().setColor(Color.green).setDescription(json.substring(0, 2000)));
					json = json.substring(2000);
				}
				replies.add(new EmbedBuilder().setColor(Color.green).setDescription(json));
				for (EmbedBuilder reply : replies) {
					if (event.getSlashCommandInteraction()!=null) {
						if (event.getSlashCommandInteraction().isAcknowledged())
							event.getSlashCommandInteraction().getHook().sendMessageEmbeds(reply.build()).queue();
						else
							event.replyEmbed(reply);
					} else {
						event.replyEmbed(reply);
					}
				}
				event.replySuccess(json);
				return;
			}

			event.replyEmbed(new EmbedBuilder()
					.setColor(Color.green)
					.setAuthor("test", "https://youtube.com/",_package.getVersions()[0].getIcon())
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
							STR."<t:\{_package.getDateCreated().toInstant().getEpochSecond()}:R>")));
		}

		@Override
		public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
			String value = event.getFocusedOption().getValue();
			switch (event.getFocusedOption().getName()) {
				case "community": {
					var time = System.currentTimeMillis();
					event.replyChoiceStrings(
							CacheCollector.getCommunityAutocomplete(event.getFocusedOption().getValue())
					).queue();
					System.out.println(System.currentTimeMillis()-time + "ms to get communities");
					break;
				}
				case "uuid": {
					var time = System.currentTimeMillis();
					String community = event.getOption("community").getAsString();

					var packages = CacheCollector.communityPackageCache.get(community);

					System.out.println(packages.size() + " packages");

					event.replyChoiceStrings(
							packages.stream()
									.filter(p -> p.getFullName().toLowerCase().contains(value.toLowerCase()))
									.limit(25)
									.map(p -> p.getUniqueId().toString() + " (" + p.getFullName() + ")")
									.toList()
					).queue();

					System.out.println(System.currentTimeMillis()-time + "ms to get UUIDs");

					break;
				}
			}
		}

		@Override
		public CommandInfo.Option[] getArguments() {
			return new CommandInfo.Option[]{
					CommandInfo.Option.required("community", "The community to search", OptionType.STRING, true),
					CommandInfo.Option.required("uuid", "The UUID of the package", OptionType.STRING, true),
					CommandInfo.Option.optional("raw", "Get raw JSON", OptionType.BOOLEAN, false, false)
			};
		}
	}

	@Command(name = "search", help = "Search for a package on Thunderstore")
	public static class PackageSearchCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.deferReply();
			try {
				List<PackageListing> result;
				if (!checkExecution(event)) return;
				String community = event.getArg(0, String.class);
				String search = event.getArg("search", String.class);
				String author = event.getArg("author", String.class);
				String depends = event.getArg("depends", String.class);

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

				if (validateCommunity(event, community))
					return;

				community = community.toLowerCase().replace(" ", "-");

				if (!Objects.equals(author, "null"))
					author = MarkdownSanitizer.sanitize(author);

				community = community.toLowerCase().replace(" ", "-");
				community = MarkdownSanitizer.sanitize(community);

				result = Arrays.stream(Bot.bot.tsja.getPackages(community, 0)).toList();

				if (search != null && !search.equals("null")) {
					String finalSearch = search;
					result = result.stream().filter(p -> p.getName().toLowerCase().contains(finalSearch.toLowerCase())).toList();
				}

				if (!author.equals("null")) {
					String finalAuthor = author;
					result = result.stream().filter(p -> p.getOwner().toLowerCase().contains(finalAuthor.toLowerCase())).toList();
				}

				if (depends != null && !depends.equals("null")) {
					result = result.stream().filter(p -> Arrays.stream(p.getVersions()[0].getDependencies()).anyMatch(dep -> dep.contains(depends))).toList();
				}

				//result = result.stream().limit(25).toList();


				event.replyMenu("package-search-menu", result.toArray(PackageListing[]::new), search + (author.equals("null")? "":"\nBy: " + author), community);


				//	event.replyMenu("package-search-menu", result, search, community);

				//SlashCommandEvent.from(event).replyMenu(pages.stream().map(page -> page.content).toList().toArray(MessageCreateData[]::new));
			} catch (Exception e) {
				e.printStackTrace();
				event.reply("An error occurred while executing this command.");
			}
		}

		@Override
		public CommandInfo.Option[] getArguments() {
			return new CommandInfo.Option[]{
					CommandInfo.Option.required("community", "The community to search", OptionType.STRING, true),
					CommandInfo.Option.optional("search", "The search to.. search", OptionType.STRING, "null", true),
					CommandInfo.Option.optional("author", "Filter by the author", OptionType.STRING, "null", true),
					CommandInfo.Option.optional("depends", "Filter by dependencies", OptionType.STRING, "null", true)
			};
		}
		@Override
		public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
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

		boolean communityCheckPackage(String community, PackageListing[] packages) {
			var com = TSJAUtils.getCommunityByIdentifier(Bot.bot.tsja, community).orElse(null);
			if (com==null || packages.length<1)
				return false;



			return Utils.containsOne(Arrays.stream(packages).filter(packageListing ->
					packageListing.getVersions()[0].getDependencies().length>0).toArray(), Bot.bot.tsja.getPackages(
					com.getIdentifier(), 0
			));
		}

		@Menu(id="package-search-menu")
		public static class PackageSearchMenu extends PageMenu {
			public PackageListing[] result;
			public String search;
			public String community;

			public PackageSearchMenu() {

			}

			public PackageSearchMenu(PackageListing[] result, String search, String community) {
				this.result=result;
				this.search=search;
				this.community=community;
			}

			@Override
			public List<EmbedBuilder> getPageData() {
				return buildPages(result, search, community);
			}

			private List<EmbedBuilder> buildPages(PackageListing[] result, String search, String community) {
				List<EmbedBuilder> pages = new ArrayList<>();
				EmbedBuilder currentPage =
						new EmbedBuilder()
								.setTitle("Mods")
								.setFooter("Search: " + search)
								.setColor(0x00ff00);

				int onPage = 0;
				int page = 1;
				for (PackageListing p : result) {
					if (onPage == 5) {
						onPage = 0;
						pages.add(currentPage);
						page = page + 1;
						currentPage = new EmbedBuilder()
								.setTitle(STR."Mods Page " + page + STR."/\{result.length / 5 + 1}")
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
}
