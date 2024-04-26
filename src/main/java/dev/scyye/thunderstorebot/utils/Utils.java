package dev.scyye.thunderstorebot.utils;

public class Utils {
	public static boolean containsOne(Object[] arr, Object[] arr2) {
		for (var s : arr) {
			for (var s2 : arr2) {
				if (s.equals(s2)) return true;
			}
		}
		return false;
	}

	public static boolean looseEquals(String s1, String s2) {
		// Simply test if s1 contains the characters of s2 in order, whether they are separated by other characters or not
		if (s1.length() < s2.length()) return false;
		int i = 0;
		for (var c : s1.toCharArray()) {
			if (c == s2.charAt(i)) i++;
			if (i == s2.length()) return true;
		}
		return false;
	}
}
