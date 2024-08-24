package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import botcommons.config.GuildConfig;
import com.google.gson.Gson;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.*;


@CommandHolder(group = "admin")
public class AdminCommands {
	@Command(name = "toggle-channel", help = "Toggles the usage of the bot in a channel", permission = "MANAGE_SERVER")
	public static void toggleChannel(GenericCommandEvent event,
									  @Param(description = "The channel to toggle", autocomplete = true) GuildChannel channel) {
		GuildConfig config = event.getConfig();
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				event.getMember().getRoles().stream().map(ISnowflake::getId).noneMatch(config.get("moderatorRoles")::contains)) {
			event.replyError("You do not have permission to use this command.").ephemeral().finish();
			return;
		}

		if (config.get("disabledChannels", List.class).contains(channel.getId())) {
			config.set("disabledChannels", ((List<String>) config.get("disabledChannels", List.class)).remove(channel.getId()));
			event.replySuccess("Enabled channel.").ephemeral().finish();
		} else {
			config.set("disabledChannels", ((List<String>) config.get("disabledChannels", List.class)).add(channel.getId()));
			event.replySuccess("Disabled channel.").ephemeral().finish();
		}
	}

	@Command(name = "toggle-user", help = "Toggles a user's access to the bot", permission = "owner")
	public static void toggleUser(GenericCommandEvent event, @Param(
			description = "The user to toggle") User user) {
		GuildConfig config = event.getConfig();

		List<String> disabledUsers = new ArrayList<>(Arrays.stream(config.get("disabledUsers", String[].class)).toList());

		if (Arrays.stream(config.get("disabledUsers", String[].class)).toList().contains(user.getId())) {
			config.set("disabledUsers", disabledUsers.remove(user.getId()));
			event.replySuccess("Enabled user.").ephemeral().finish();
		} else {
			config.set("disabledUsers", disabledUsers.add(user.getId()));
			event.replySuccess("Disabled user.").ephemeral().finish();
		}
	}

	@Command(name = "toggle-mod-role", help = "Adds a role to the moderator roles", permission = "MANAGE_SERVER")
	public static void toggleModRole(GenericCommandEvent event, @Param(
			description = "The role to toggle") Role role) {
		GuildConfig config = event.getConfig();
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				event.getMember().getRoles().stream().map(ISnowflake::getId).noneMatch(config.get("moderatorRoles")::contains)) {
			event.replyError("You do not have permission to use this command.").ephemeral().finish();
			return;
		}

		if (Arrays.stream(config.get("moderatorRoles", String[].class)).toList().contains(role.getId())) {
			config.set("moderatorRoles", Arrays.stream((config.get("moderatorRoles", String[].class))).toList().remove(role.getId()));
			event.replySuccess("Removed moderator role.").ephemeral().finish();
		} else {
			config.set("moderatorRoles", ((List<String>) config.get("moderatorRoles", List.class)).add(role.getId()));
			event.replySuccess("Added moderator role.").ephemeral().finish();
		}
	}

	@Command(name = "view-config", help = "View the server's config", permission = "MANAGE_SERVER")
	public static void viewConfig(GenericCommandEvent event) {
		GuildConfig config = event.getConfig();
		EmbedBuilder builder = new EmbedBuilder();

		System.out.println(new Gson().toJson(config));
		event.replyEmbed(builder.setTitle("Server Config")
				.setDescription(new Gson().toJson(config)))
				.ephemeral().finish();
	}

	@Command(name = "rename", help = "Rename the bot", permission = "MANAGE_SERVER")
	public static void rename(GenericCommandEvent event, @Param(description = "The new name") String name) {
		if (name.length()>32) {
			event.replyError("Must not be over 32 characters").finish();
			return;
		}

		event.getGuild().getSelfMember().modifyNickname(name).queue(unused -> event.replySuccess("Renamed.").finish(), error -> {
			UUID uuid = UUID.randomUUID();
			event.replyError("Failed to rename.\nContact <@553652308295155723> with this error code: " + uuid).finish();
			System.out.println("Error code: " + uuid);
			System.out.println("Error: " + error.getMessage());
			error.printStackTrace();
		});
	}

	@Command(name = "community", help = "Set's the default allowed community for the server", permission = "MANAGE_SERVER")
	public static void community(GenericCommandEvent event,
								 @Param(description = "Set to \"null\" to clear") String community) {
		GuildConfig config = event.getConfig();

		if (community.equals("null")) {
			config.set("community", "");
			event.replySuccess("Cleared community.").ephemeral().finish();
			return;
		}
		if (Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier).noneMatch(s -> s.equals(community))) {
			event.replyError("Invalid community.").ephemeral().finish();
			return;
		}
		config.set("community", community);
		event.replySuccess("Set community to " + community).ephemeral().finish();
	}
}

