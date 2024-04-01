package dev.scyye.thunderstorebot.command.impl;

import dev.scyye.botcommons.commands.Command;
import dev.scyye.botcommons.commands.CommandInfo;
import dev.scyye.botcommons.commands.GenericCommandEvent;
import dev.scyye.botcommons.commands.ICommand;
import dev.scyye.botcommons.menu.Menu;
import dev.scyye.botcommons.menu.impl.PageMenu;
import dev.scyye.thunderstorebot.versions.Version;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

public class MiscCommands {
	@Command(name = "ping", help = "Pong!")
	public static class PingCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			var time = System.currentTimeMillis();
			event.reply("Pong!", message -> {
				message.editMessageFormat(
						"Pong! %d ms", System.currentTimeMillis() - time
				).queue();
			});
		}
	}

	@Command(name = "echo", help = "Echoes the message")
	public static class EchoCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.getChannel().sendMessage(event.getArg(0, String.class)).queue();
			event.replyEphemeral("Success");
		}

		@Override
		public CommandInfo.Option[] getArguments() {
			return new CommandInfo.Option[]{
					CommandInfo.Option.required("message", "The message to echo", OptionType.STRING, false)
			};
		}
	}

	@Command(name = "changelog", help = "Get the bot's changelog")
	@Menu(id = "changelog")
	public static class ChangelogCommand extends PageMenu implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.replyMenu("changelog");
		}

		@Override
		public List<EmbedBuilder> getPageData() {
			return Version.versions.stream().map(
					version -> new EmbedBuilder()
							.setTitle(STR."Changelog v\{version.version} \{version.beta?"-beta":""}")
							.setDescription(version.changelog)
							.addField("Release date", version.releaseDate, false)
			).toList();
		}
	}

	@Command(name = "help", help = "Shows help")
	public static class HelpCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.reply("Help command isnt implemented yet\nAnnoy the shit out of @scyye");
		}
	}

	@Command(name = "version", help = "Get info on the newest version")
	public static class VersionCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			var version = Version.versions.getLast();
			event.replySuccess(STR."Version: \{version.version}\{version.beta ? " beta" : ""}\nRelease Date: \{version.releaseDate}\nChangelog: \n\{version.changelog}");
		}
	}

	@Command(name = "invite", help = "Generate an invite link for the bot")
	public static class InviteCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.replySuccess(event.getJDA().getInviteUrl(Permission.ADMINISTRATOR));
		}
	}

	@Command(name = "credits", help = "Displays credits for the bot")
	public static class CreditsCommand implements ICommand {
		@Override
		public void handle(GenericCommandEvent event) {
			event.replyEmbed(new EmbedBuilder()
					.setAuthor("Scyye")
					.addField("Thunderstore Java API", "[Scyye](https://github.com/Scyye)", false)
					.addField("Bot's Code", "[Scyye](https://github.com/Scyye)", false)
					.addField("Art & Other Assets", "[Keyanlux](https://www.youtube.com/@Keyanlux_Deluxe)", false)
					.addField("Testers", "[Poppycars](https://github.com/poppycars22/), " +
							"[Anarkey](https://thunderstore.io/c/rounds/p/Anarkey/Peanut_Butter/), " +
							"[Root](https://ko-fi.com/rootsystem), [Assist](https://ko-fi.com/ascyst)", false)
			);
		}
	}
}
