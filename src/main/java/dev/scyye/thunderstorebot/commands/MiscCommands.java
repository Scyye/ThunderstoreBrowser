package dev.scyye.thunderstorebot.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import dev.scyye.thunderstorebot.versions.Version;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@Interaction
public class MiscCommands {
	@SlashCommand(value = "ping", desc = "Pong!")
	public void ping(CommandEvent event) {
		var time = System.currentTimeMillis();
		event.reply("Pong!", m -> {
			var ping = System.currentTimeMillis() - time;
			m.editMessage("Pong! " + ping + "ms").queue();
		});
	}

	@SlashCommand(value = "changelog", desc = "Displays the bot's changelog", ephemeral = true)
	public void changelog(CommandEvent event) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Changelog");
		for (var v : Version.versions) {
			builder.addField(v.version + (v.beta ? "-beta" : ""), v.changelog, false);
		}
		event.reply(builder);
	}

	@SlashCommand(value = "version", desc = "Displays the bot's version", ephemeral = true)
	public void version(CommandEvent event) {
		var version = Version.versions.get(Version.versions.size()-1);
		event.reply("Version: " + version.version + (version.beta?" beta":"") + "\n" +
				"Release Date: " + version.releaseDate + "\n" +
				"Changelog: \n" + version.changelog);
	}

	@SlashCommand(value = "invite", desc = "Invite the bot to your server!", ephemeral = true)
	public void invite(CommandEvent event) {
		event.reply(event.getJDA().getInviteUrl(Permission.ADMINISTRATOR));
	}

	@SlashCommand(value = "help", desc = "Displays help menu", ephemeral = true)
	public void help(CommandEvent event) {
		event.reply("Help command is not implemented yet!\nAnnoy the shit out of <@553652308295155723>!");
	}

	@SlashCommand(value = "credits", desc = "Display credits for the bot")
	public void credits(CommandEvent event) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("Scyye");
		builder.addField("Thunderstore Java API", "[Scyye](https://github.com/Scyye)", false);
		builder.addField("Bot's Code", "[Scyye](https://github.com/Scyye)", false);
		builder.addField("Art & Other Assets", "[Keyanlux](https://www.youtube.com/@Keyanlux_Deluxe)", false);
		builder.addField("Testers", "[Poppycars](https://github.com/poppycars22/), " +
				"[Anarkey](https://rounds.thunderstore.io/package/Anarkey/Peanut_Butter/), " +
				"[Root](https://ko-fi.com/rootsystem), [Assist](https://ko-fi.com/ascyst)", false);
		event.reply(builder);
	}
}
