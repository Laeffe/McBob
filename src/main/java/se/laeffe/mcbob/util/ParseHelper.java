package se.laeffe.mcbob.util;

public class ParseHelper {
	public static Integer getInt(String[] args, int i) {
		if(args.length > i) {
			try {
				return Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {}
		}
		return null;
	}
}
