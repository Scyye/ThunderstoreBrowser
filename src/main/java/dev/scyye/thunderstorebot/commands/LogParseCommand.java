package dev.scyye.thunderstorebot.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import dev.scyye.botcommons.command.slash.SlashCommandEvent;
import dev.scyye.botcommons.menu.PaginatedMenuHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Interaction
public class LogParseCommand {

	@SlashCommand(value = "logparse", desc = "Parse a log file")
	public void logParse(CommandEvent event, @Param(name="log", value = "The log file to parse") String log) {
		// Get all files from the message
		if (log==null) {
			event.reply("Please attach a log file");
			return;
		}
		// Get the log file from the included link in "log"
		event.getInteraction().getMessageChannel().retrieveMessageById(log).queue(message -> {
			var logFile = message.getAttachments().get(0);
			File file = logFile.getProxy().downloadToFile(new File("logs\\" + UUID.randomUUID() + ".log")).join();

			if (file.getName().endsWith(".log")) {
				FileInfo fileInfo = getFileInfo(file);

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setTitle(fileInfo.gameName + " - BepInEx v" + fileInfo.bepInVersion)
						.setAuthor("Unity " + fileInfo.unityVersion)
						.addField("Plugin Count", String.valueOf(fileInfo.plugins.size()), true)
						.addField("Error Count", fileInfo.errors.size() + " (" + fileInfo.uniqueErrors.size() + " unique errors)", true);

				for (var e : fileInfo.uniqueErrors) {
					embedBuilder.addField("Error", e, false);
				}
				embedBuilder.setDescription("Use /pluginlist to see all plugins");

				event.reply(embedBuilder);
			}
		});
	}

	@SlashCommand(value = "pluginlist", desc = "List all plugins in a log file")
	public void onPluginList(CommandEvent event, @Param(name = "log", value = "the log file") String id) {
		SlashCommandEvent slashEvent = SlashCommandEvent.from(event);

		List<MessageCreateData> messages = new ArrayList<>();

		event.getMessageChannel().retrieveMessageById(id).queue(msg -> {
			var logFile = msg.getAttachments().get(0);
			File file = logFile.getProxy().downloadToFile(new File("logs\\" + UUID.randomUUID() + ".log")).join();

			if (file.getName().endsWith(".log")) {
				FileInfo fileInfo = getFileInfo(file);

				StringBuilder builder = new StringBuilder();
				for (var plugin : fileInfo.plugins) {
					builder.append(plugin).append("\n");
					if (builder.length() > 1500) {
						messages.add(MessageCreateData.fromContent(builder.toString()));
						builder = new StringBuilder();
					}
				}
			}
		});

		slashEvent.replyMenu(messages.toArray(MessageCreateData[]::new));
	}

	FileInfo getFileInfo(File file) {
		FileInfo result = new FileInfo();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}


		result.bepInVersion = lines.get(0).split(" ")[5];
		result.gameName = lines.get(0).split("-")[1].split(" ")[1];

		result.unityVersion = lines.get(1).split(" ")[10];
		int i = lines.indexOf("[Message:   BepInEx] Chainloader started");

		result.plugins = lines.subList(i - 1, lines.size() - 1)
				.stream()
				.filter(s -> s.startsWith("[Info   :   BepInEx] Loading"))
				.map(s -> String.join(" ", Arrays.copyOfRange(s.split(" "), 8, s.split(" ").length)))
				.toList();

		result.errors = lines.stream().filter(s -> s.startsWith("[Error"))
				.map(s -> String.join(" ", Arrays.copyOfRange(s.split(" "), 5, s.split(" ").length)))
				.toList();

		for (String error : result.errors) {
			if (result.uniqueErrors == null)
				result.uniqueErrors = new ArrayList<>();

			if (!result.uniqueErrors.contains(error))
				result.uniqueErrors.add(error);

		}

		return result;
	}

	static class FileInfo {
		String bepInVersion;
		String gameName;
		String unityVersion;
		List<String> plugins;
		List<String> errors;
		List<String> uniqueErrors;
	}

}
