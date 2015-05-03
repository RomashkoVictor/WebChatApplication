package bsu.fpmi.chat.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class MessageUtil {
	public static final String TOKEN = "token";
	public static final String ID = "id";

	private MessageUtil() {
	}

	public static String getToken(int index) {
		return String.valueOf(index);
	}

	public static int getIndex(String token) {
		return Integer.valueOf(token);
	}

	public static JSONObject stringToJson(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data.trim());
	}

}
