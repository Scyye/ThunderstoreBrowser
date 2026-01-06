package dev.scyye.thunderstorebot.command.impl;

import botcommons.commands.Command;
import botcommons.commands.CommandHolder;
import botcommons.commands.GenericCommandEvent;
import botcommons.commands.Param;
import botcommons.config.ConfigManager;
import com.google.gson.Gson;
import dev.scyye.thunderstoreapi.cache.CacheCollector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.*;

@SuppressWarnings("unused")
@CommandHolder(group = "admin")
public class AdminCommands {
	@Command(name = "toggle-channel", help = "Toggles the usage of the bot in a channel", permission = "MANAGE_SERVER")
	public static void toggleChannel(GenericCommandEvent event,
									  @Param(description = "The channel to toggle", autocomplete = true) GuildChannel channel) {
		ConfigManager.Config config = event.getConfig();

		List<String> disabledChannels = new ArrayList<>(Arrays.stream(config.get("disabledChannels", String[].class)).toList());
		if (disabledChannels.contains(channel.getId())) {
			disabledChannels.remove(channel.getId());
			config.put("disabledChannels", disabledChannels);
			event.replySuccess("Enabled channel.").ephemeral().finish();
		} else {
			disabledChannels.add(channel.getId());
			config.put("disabledChannels", disabledChannels);
			event.replySuccess("Disabled channel.").ephemeral().finish();
		}
	}

	@Command(name = "toggle-user", help = "Toggles a user's access to the bot", permission = "owner")
	public static void toggleUser(GenericCommandEvent event, @Param(
			description = "The user to toggle") User user) {
		ConfigManager.Config config = event.getConfig();

		List<String> disabledUsers = new ArrayList<>(Arrays.stream(config.get("disabledUsers", String[].class)).toList());

		if (disabledUsers.contains(user.getId())) {
			disabledUsers.remove(user.getId());
			config.put("disabledUsers", disabledUsers);
			event.replySuccess("Enabled user.").ephemeral().finish();
		} else {
			disabledUsers.add(user.getId());
			config.put("disabledUsers", disabledUsers);
			event.replySuccess("Disabled user.").ephemeral().finish();
		}
	}

	@Command(name = "toggle-mod-role", help = "Adds a role to the moderator roles", permission = "MANAGE_SERVER", userContext = {InteractionContextType.GUILD})
	public static void toggleModRole(GenericCommandEvent event, @Param(
			description = "The role to toggle") Role role) {
		ConfigManager.Config config = event.getConfig();

		List<String> moderatorRoles = new ArrayList<>(Arrays.stream(config.get("moderatorRoles", String[].class)).toList());
		if (moderatorRoles.contains(role.getId())) {
			moderatorRoles.remove(role.getId());
			config.put("moderatorRoles", moderatorRoles);
			event.replySuccess("Removed moderator role.").ephemeral().finish();
		} else {
			moderatorRoles.add(role.getId());
			config.put("moderatorRoles", moderatorRoles);
			event.replySuccess("Added moderator role.").ephemeral().finish();
		}
	}

	@Command(name = "view-config", help = "View the server's config", permission = "MANAGE_SERVER", userContext = {InteractionContextType.GUILD})
	public static void viewConfig(GenericCommandEvent event) {
		ConfigManager.Config config = event.getConfig();
		EmbedBuilder builder = new EmbedBuilder();

		System.out.println(new Gson().toJson(config));
		event.replyEmbed(builder.setTitle("Server Config")
				.setDescription(new Gson().toJson(config)))
				.ephemeral().finish();
	}

	@Command(name = "rename", help = "Rename the bot", permission = "MANAGE_SERVER", userContext = {InteractionContextType.GUILD})
	public static void rename(GenericCommandEvent event, @Param(description = "The new name") String name) {
		if (name.length()>32) {
			event.replyError("Must not be over 32 characters").finish();
			return;
		}

		event.getGuild().getSelfMember().modifyNickname(name).queue($ -> event.replySuccess("Renamed.").finish(), error -> {
			UUID uuid = UUID.randomUUID();
			event.replyError("Failed to rename.\nContact <@553652308295155723> with this error code: %s".formatted(uuid)).finish();
			System.out.printf("Error code: %s", uuid);
			System.out.printf("Error: %s", error.getMessage());
			error.printStackTrace();
		});
	}

	@Command(name = "community", help = "Set's the default allowed community for the server", permission = "MANAGE_SERVER")
	public static void community(GenericCommandEvent event,
								 @Param(description = "Set to \"null\" to clear") String community) {
		ConfigManager.Config config = event.getConfig();

		if (community.equals("null")) {
			config.put("community", "");
			event.replySuccess("Cleared community.").ephemeral().finish();
			return;
		}
		if (CacheCollector.getCommunities().stream().noneMatch(s -> s.equals(community))) {
			event.replyError("Invalid community.").ephemeral().finish();
			return;
		}
		config.put("community", community);
		event.replySuccess("Set community to %s".formatted(community)).ephemeral().finish();
	}
}

