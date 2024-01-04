package dev.scyye.thunderstorebot.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.google.gson.GsonBuilder;
import dev.scyye.botcommons.config.ServerConfig;
import dev.scyye.thunderstoreapi.api.entities.community.Community;
import dev.scyye.thunderstorebot.Bot;
import dev.scyye.thunderstorebot.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
@Interaction
public class AdminCommands {
	@SlashCommand(value = "admin disable-channel", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onDisableChannel(CommandEvent event, @Param(value = "channel", name = "channel") GuildChannel channel) {
		if (channel==null)
			channel = event.getTextChannel();
		ServerConfig config = ServerConfig.configs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		if (config.get("disabledChannels", List.class).contains(channel.getId())) {
			event.reply("Channel is already disabled.");
			return;
		}
		config.get("disabledChannels", List.class).add(channel.getId());
		event.reply("Disabled channel.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin enable-channel", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onEnableChannel(CommandEvent event, @Param(value = "channel", name = "channel") @Optional GuildChannel channel) {
		if (channel==null)
			channel = event.getTextChannel();
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		if (!config.disabledChannels.contains(channel.getId())) {
			event.reply("Channel is already enabled.");
			return;
		}
		config.disabledChannels.remove(channel.getId());
		event.reply("Enabled channel.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin block-user", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onBlockUser(CommandEvent event, @Param(value = "user", name = "user") User user) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		if (config.disabledUsers.contains(user.getId())) {
			event.reply("User is already blocked.");
			return;
		}
		config.disabledUsers.add(user.getId());
		event.reply("Blocked user.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin unblock-user", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onUnblockUser(CommandEvent event, @Param(value = "user", name = "user") User user) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		config.disabledUsers.remove(user.getId());
		event.reply("Unblocked user.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin add-moderator-role", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onAddModeratorRole(CommandEvent event, @Param(value = "role", name = "role") Role role) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		config.moderatorRoles.add(role.getId());
		event.reply("Added moderator role.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin remove-moderator-role", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onRemoveModeratorRole(CommandEvent event, @Param(value = "role", name = "role") Role role) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		config.moderatorRoles.remove(role.getId());
		event.reply("Removed moderator role.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin view-config", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onViewConfig(CommandEvent event, @Optional @Param(value = "File Name", name = "file-name") String file) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				!Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		if (file!=null) {
			event.reply(MessageCreateData.fromFiles(FileUpload.fromData(
					new File("thunderstorebot-assets/server-configs/" + event.getGuild().getId() + ".json")).setName(file+".json")));
			return;
		}

		StringBuilder builder = new StringBuilder();

		builder.append("Disabled channels: ");
		for (String s : config.disabledChannels) {
			builder.append("<#").append(s).append("> ");
		}
		builder.append("\nDisabled users: ");
		for (String s : config.disabledUsers) {
			builder.append("<@").append(s).append("> ");
		}
		builder.append("\nModerator roles: ");
		for (String s : config.moderatorRoles) {
			builder.append("<@&").append(s).append("> ");
		}
		builder.append("\nCommunity: ").append(config.community);
		event.reply(builder.toString());
	}

	@SlashCommand(value = "admin set-config", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onSetConfig(CommandEvent event, @Param(value = "file", name = "file") Message.Attachment file) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}

		event.reply("This command doesnt work ATM, annoy the fuck out of <@553652308295155723>.");

		if (file!=null)
			return;

		try {
			Config.serverConfigs.put(event.getGuild().getId(),
					new GsonBuilder().setPrettyPrinting().create().fromJson
							(Files.readString(file.getProxy().downloadToPath().get()), Config.class));
			Files.writeString(Path.of("thunderstorebot-assets/server-configs/" + event.getGuild().getId() + ".json"),
					new GsonBuilder().setPrettyPrinting().create().toJson(Config.serverConfigs.get(event.getGuild().getId()), Config.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		event.reply("Set config.");
		saveConfig(event.getGuild());
	}

	@SlashCommand(value = "admin rename", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onRename(CommandEvent event, @Param(value = "name", name = "name") String name) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (name.length()>32) {
			event.reply("Must not be over 32 characters");
			return;
		}
		try {
			if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
					Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
				event.reply("You do not have permission to use this command. " +
						"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			event.reply("problem uwu");
		}

		event.getGuild().getSelfMember().modifyNickname(name).queue(unused -> event.reply("Renamed."), error -> {
			UUID uuid = UUID.randomUUID();
			event.reply("Failed to rename.\nContact <@553652308295155723> with this error code: " + uuid);
			System.out.println("Error code: " + uuid);
			System.out.println("Error: " + error.getMessage());
			error.printStackTrace();
		});
	}

	@SlashCommand(value = "admin community", desc = "Set's the default allowed community for the server", ephemeral = true, enabledFor = Permission.MANAGE_SERVER)
	public void onAdminCommunity(CommandEvent event, @Param(name = "community", value = "Set to \"null\" to clear") @Optional String community) {
		Config config = Config.serverConfigs.get(event.getGuild().getId());
		if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) &&
				Utils.containsOne(event.getMember().getRoles().stream().map(ISnowflake::getId).toArray(), config.moderatorRoles.toArray())) {
			event.reply("You do not have permission to use this command. " +
					"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
			return;
		}
		if (community==null) {
			event.reply("Current community: " + config.community);
			return;
		}
		if (community.equals("null")) {
			config.community = "";
			event.reply("Cleared community.");
			saveConfig(event.getGuild());
			return;
		}
		if (Arrays.stream(Bot.bot.tsja.getCommunities()).map(Community::getIdentifier).noneMatch(s -> s.equals(community))) {
			event.reply("Invalid community.");
			return;
		}
		config.community = community;
		event.reply("Set community to " + community);
		saveConfig(event.getGuild());
	}



	public static void saveConfig(Guild guild) {
		Config config = Config.serverConfigs.get(guild.getId());
		if (config==null) return;
		try {
			Files.writeString(Path.of("thunderstorebot-assets/server-configs/" + guild.getId() + ".json"),
					new GsonBuilder().setPrettyPrinting().create().toJson(config, Config.class));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
 */
