package dev.scyye.thunderstorebot.command.impl;

import com.google.gson.GsonBuilder;
import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.thunderstoreapi.api.TSJAUtils;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstoreapi.api.entities.packages.PackageListing;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.utils.CommandUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Command(name = "package-info", help="Get info about a package")
public class PackageInfoCommand implements ICommand {
	@Override
	public void handle(GenericCommandEvent event) {
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
				event.replyChoiceStrings(Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier)
						.filter(string -> string.startsWith(value) || string.contains(
								value
						)).limit(24).toList()).queue();
				System.out.println(System.currentTimeMillis()-time + "ms to get communities");
				break;
			}
			case "uuid": {
				var time = System.currentTimeMillis();
				String community = event.getOption("community").getAsString();

				List<PackageListing> packages = Arrays.stream(Bot.bot.tsja.getPackages(community, null)).filter(
						packageListing -> packageListing.getFullName().contains(value)
				).toList();
				List<String> ac = new ArrayList<>();
				int count = 0;
				for (PackageListing packageListing : packages) {
					if (count > 24)
						break;
					ac.add(packageListing.getUniqueId().toString() + " (" + packageListing.getFullName() +")");
					count++;
				}
				event.replyChoiceStrings(ac).queue();
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
