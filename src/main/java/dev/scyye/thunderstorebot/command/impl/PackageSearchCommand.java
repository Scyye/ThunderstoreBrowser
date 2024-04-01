package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
import dev.scyye.thunderstoreapi.api.TSJA;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import static dev.scyye.thunderstorebot.utils.CommandUtils.*;

import java.util.*;

@Command(name = "package-search", help = "Search for a package on Thunderstore")
public class PackageSearchCommand implements ICommand {
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

			if (!validateCommunity(event, community))
				return;

			community = community.toLowerCase().replace(" ", "-");

			if (!Objects.equals(author, "null"))
				author = MarkdownSanitizer.sanitize(author);

			community = community.toLowerCase().replace(" ", "-");
			community = MarkdownSanitizer.sanitize(community);

			result = Arrays.stream(Bot.bot.tsja.getPackages(community, null)).toList();

			if (search != null && !search.equals("null")) {
				String finalSearch = search;
				result.stream().filter(p -> p.getName().toLowerCase().contains(finalSearch.toLowerCase())).toList();
			}

			if (author != null && !author.equals("null")) {
				String finalAuthor = author;
				result.stream().filter(p -> p.getOwner().toLowerCase().contains(finalAuthor.toLowerCase())).toList();
			}

			if (depends != null && !depends.equals("null")) {
				String finalDepends = depends;
				result.stream().filter(p -> Arrays.stream(p.getVersions()[0].getDependencies()).anyMatch(dep -> dep.contains(finalDepends))).toList();
			}

			result = result.stream().limit(25).toList();


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

	HashMap<String, PackageListing[]> communityPackages = HashMap.newHashMap(0);

	@Override
	public void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
		switch (event.getFocusedOption().getName()) {
			case "community" -> {
				List<String> communities = communityPackages.keySet().isEmpty() ?
						Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier).toList()
						: communityPackages.keySet().stream().toList();

				event.replyChoiceStrings(
						communities.stream()
								.filter(id -> id.contains(event.getFocusedOption().getValue()))
								.limit(25)
								.toArray(String[]::new)
				).queue();
			}
			case "search", "depends" -> {
				String community = event.getOption("community").getAsString();
				var val = communityPackages.getOrDefault(community, new PackageListing[0]);
				var packages = val.length==0 || !communityCheckPackage(community, val)?
						TSJAUtils.getPackagesByName(Bot.bot.tsja, community,
								event.getFocusedOption().getValue())
						: communityPackages.get(community);
				communityPackages.put(community, packages);

				event.replyChoiceStrings(
						Arrays.stream(packages)
								.filter(p -> p.getName().toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()))
								.limit(25)
								.map(PackageListing::getName)
								.toList()
				).queue();
			}
			case "author" -> {
				String community = event.getOption("community").getAsString();
				var val = communityPackages.getOrDefault(community, new PackageListing[0]);
				var packages = val.length==0 || !communityCheckPackage(community, val)?
						TSJAUtils.getPackagesByName(Bot.bot.tsja, community,
								event.getFocusedOption().getValue())
						: communityPackages.get(community);
				communityPackages.put(community, packages);

				event.replyChoiceStrings(
						Arrays.stream(packages).filter(
								p -> p.getOwner().toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase())
						).limit(25).map(PackageListing::getOwner).toList()
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
				com.getIdentifier(), null
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
				String packageName = MarkdownUtil.underline(p.isDeprecated() ? "~~" + p.getName() + "~~" : p.getName());
				String ownerLink = MarkdownUtil.maskedLink(p.getOwner(), "<" + TSJA.getTeamUrl(community, p.getOwner()) + ">");
				String downloadLink = MarkdownUtil.maskedLink("here", "<" + p.getVersions()[0].getDownloadUrl() + ">");

				currentPage.addField(packageName, "Page: " + p.getPackageUrl().toString() + "\nCreated By: " + ownerLink + "\nDownload: " + downloadLink, false);

				onPage++;
			}
			pages.add(currentPage);

			return pages;
		}
	}

}
