package dev.scyye.thunderstorebot.commands;

import ch.qos.logback.core.ConsoleAppender;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import dev.scyye.botcommons.menu.PaginatedMenuHandler;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Predicate;

@Interaction
public class TestCommands {
	@SlashCommand(value = "test")
	public void onTest(CommandEvent event) throws RuntimeException {
		throw new RuntimeException("Test");
	}

	public static boolean checkExecute(CommandEvent event, Predicate<CommandEvent> predicate) {
		if (predicate.test(event))
			return true;
		event.reply("You do not have permission to use this command. " +
				"If you believe this is an error, please contact this server's staff, or <@553652308295155723>");
		return false;
	}
}
