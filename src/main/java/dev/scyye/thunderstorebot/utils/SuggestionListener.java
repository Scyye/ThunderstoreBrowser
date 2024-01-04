package dev.scyye.thunderstorebot.utils;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

public class SuggestionListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getChannelType() != ChannelType.PRIVATE) return;
		if (event.getAuthor().isBot()) return;

		if (event.getChannel().getId().equals("1165904082817519656")) {
			event.getJDA().retrieveUserById(event.getMessage().getContentRaw().split(" ")[0]).queue(
					user -> user.openPrivateChannel().flatMap(
							channel -> channel.sendMessage(event.getMessage().getContentRaw().replace(
									event.getMessage().getContentRaw().split(" ")[0] + " ", ""))
					).queue()
			);
			return;
		}

		event.getJDA().retrieveUserById("553652308295155723").queue(
				user -> user.openPrivateChannel().flatMap(
						channel -> channel.sendMessage(event.getAuthor().getName() + " suggested: " + event.getMessage().getContentRaw())
				).queue()
		);



		event.getMessage().addReaction(Emoji.fromUnicode("✔️")).queue();
		event.getMessage().reply("Your suggestion has been sent, \nfeel free to DM any bug reports/suggestions").queue();
	}
}
