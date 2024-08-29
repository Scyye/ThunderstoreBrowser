package dev.scyye.thunderstorebot.utils;

import java.util.Arrays;
import java.util.List;

public class Utils {
	public static boolean loseEquals(String string, String search) {
		List<String> stringList = Arrays.asList(string.split(""));
		String regex = "^" + stringList.stream().map(s -> s+"?").reduce("", String::concat) + "$";
		return search.matches(regex);
	}
}
