package bsu.fpmi.chat.dao;

import java.io.IOException;
import java.util.List;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


public interface MessageDao {
	void add(JSONObject message);

	void update(JSONObject message);

	void delete(String id);

	JSONObject getMesseges(int index)throws ParserConfigurationException, SAXException, IOException, TransformerException;
}
