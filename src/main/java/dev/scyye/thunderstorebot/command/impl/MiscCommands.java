package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.*;
import botcommons.menu.Menu;
import botcommons.menu.types.PageMenu;
import dev.scyye.thunderstorebot.versions.Version;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.AttachedFile;

import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
@CommandHolder
public class MiscCommands {
	@Command(name = "ping", help = "Pong!", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void ping(GenericCommandEvent event) {
		var time = System.currentTimeMillis();
		event.reply("Pong!", message -> message.editMessageFormat(
				"Pong! %d ms", System.currentTimeMillis() - time
		).queue());
	}


	@Command(name = "echo", help = "Echoes the message", permission = "owner", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD})
	public static void echo(GenericCommandEvent event, @Param(name ="message",description = "The message to echo") String message) {
		event.getSlash().getChannel().sendMessage(message).queue();
		event.replyEphemeral("Success").finish();
	}


	@Command(name = "changelog", help = "Get the bot's changelog", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void changelog(GenericCommandEvent event) {
		event.deferReply();
		event.replyMenu("changelog").finish();
	}

	@Menu(id = "changelog")
	public static class ChangelogCommand extends PageMenu {
		@Override
		public List<EmbedBuilder> getPageData() {
			return Version.versions.reversed().stream().map(
					version -> new EmbedBuilder()
							.setTitle("Changelog v%s%s".formatted(version.version, version.beta ? " beta" : ""))
							.setDescription(version.changelog)
							.addField("Release date", version.releaseDate, false)
			).toList();
		}

		@Override
		public MessageEmbed build() {
			return getPageData().getFirst().build();
		}
	}

	@Command(name = "help", help = "Shows help", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void help(GenericCommandEvent event) {
		event.replyError("annoy the shit out of @scyye to implement this ^v^").finish();
	}

	/*
	@Menu(id = "help")
	public static class HelpMenu extends PageMenu {
		@Override
		public List<EmbedBuilder> getPageData() {
			List<EmbedBuilder> pages = new ArrayList<>();
			EmbedBuilder currentBuilder = new EmbedBuilder();

			Field[] fields = CommandManager.class.getDeclaredFields();

			HashMap<CommandInfo, Method> commands = new HashMap<>();
			HashMap<String, Map.Entry<CommandInfo, Method>> subcommands = new HashMap<>();

			try {
				commands = (HashMap<CommandInfo, Method>) fields[0].get(null);
				subcommands = (HashMap<String, Map.Entry<CommandInfo, Method>>) fields[1].get(null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			for (var command : commands.keySet()) {
				if (currentBuilder.getFields().size() >= 10) {
					pages.add(currentBuilder);
					currentBuilder = new EmbedBuilder();
				}
				currentBuilder.addField(command.name, command.help, false);
			}
			return pages;
		}
	}*/

	@Command(name = "version", help = "Get info on the newest version", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void version(GenericCommandEvent event) {
		var version = Version.versions.getLast();
		event.replySuccess("""
				Version: %s%s
				Release Date: %s
				%s
				Changelog:
				%s
				""".formatted(version.version, version.beta?" beta":"", version.releaseDate, !version.beta?"":"This is a BETA version, bugs should be expected, report any and all issues to @scyye on discord, or by joining https://discord.com/invite/P8W2kBJZWq", version.changelog)).finish();
	}

	@Command(name = "invite", help = "Generate an invite link for the bot", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void invite(GenericCommandEvent event) {
		event.replySuccess(event.getJDA().getInviteUrl(Permission.ADMINISTRATOR)).finish();
	}

	@Command(name = "credits", help = "Displays credits for the bot", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void credits(GenericCommandEvent event) {
		event.replyEmbed(new EmbedBuilder()
				.setAuthor("Scyye")
				.addField("Thunderstore Java API", "[Scyye](https://github.com/Scyye)", false)
				.addField("Bot's Code", "[Scyye](https://github.com/Scyye)", false)
				.addField("Art & Other Assets", "[Keyanlux](https://www.youtube.com/@Keyanlux_Deluxe)", false)
				.addField("Testers", "[Poppycars](https://github.com/poppycars22/),  " +
						"[Anarkey](https://thunderstore.io/c/rounds/p/Anarkey/Peanut_Butter/), " +
						"[Root](https://ko-fi.com/rootsystem), [Assist](https://ko-fi.com/ascyst), [Atomic();](https://atomictyler.dev/)", false)
		).finish();
	}

	@Command(name = "soup", help = "Soup.", userContext = {InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL})
	public static void soup(GenericCommandEvent event) {
		event.reply("Soup. That's what this command does, its just fucking soup.").finish(message ->
				message.editMessageAttachments(AttachedFile.fromData(Path.of("thunderstorebot-assets", "soup.png"))).queue());
	}
}
