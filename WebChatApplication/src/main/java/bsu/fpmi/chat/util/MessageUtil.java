package bsu.fpmi.chat.util;

import bsu.fpmi.chat.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class MessageUtil {
	public static final String TOKEN = "token";
	public static final String MESSAGES = "messages";
	public static final String USERNAME = "username";
	private static final String TEXT = "text";
	private static final String TIME = "time";
	private static final String ID = "id";
	private static final String EDITED = "edited";
	private static final String DELETED = "deleted";

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

	public static Message jsonToMessage(JSONObject json) {
		Object username = json.get(USERNAME);
		Object text = json.get(TEXT);
		Object time = json.get(TIME);
		Object edited = json.get(EDITED);
		Object deleted = json.get(DELETED);
		Object id = json.get(ID);

		if (id != null && deleted != null && edited != null && time != null && text != null && username != null) {
			return new Message((String)username, (String)text, (String)time, (String)id, (Boolean)edited, (Boolean)deleted);
		}
		return null;
	}
}
