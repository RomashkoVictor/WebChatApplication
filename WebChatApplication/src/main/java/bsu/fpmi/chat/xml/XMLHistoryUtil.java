package bsu.fpmi.chat.xml;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static bsu.fpmi.chat.xml.XMLRequestHistoryUtil.getRequests;
import static bsu.fpmi.chat.xml.XMLRequestHistoryUtil.getStorageRequestSize;

public final class XMLHistoryUtil {
	private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml"; // history.xml will be located in the home directory
	public static final String MESSAGES = "messages";
	public static final String TOKEN = "token";
	public static final String MESSAGE = "message";
	public static final String USERNAME = "username";
	private static final String TEXT = "text";
	private static final String TIME = "time";
	private static final String ID = "id";
	private static final String EDITED = "edited";
	private static final String DELETED = "deleted";

	private XMLHistoryUtil() {
	}

	public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(MESSAGES);
		doc.appendChild(rootElement);

		Transformer transformer = getTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
		transformer.transform(source, result);
	}

	public static synchronized void addData(JSONObject message) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Element root = document.getDocumentElement(); // Root <tasks> element

		Element messageElement = document.createElement(MESSAGE);
		root.appendChild(messageElement);

		String id = UUID.randomUUID().toString();
		XMLRequestHistoryUtil.addRequest(id);
		messageElement.setAttribute(ID, id);

		Element username = document.createElement(USERNAME);
		username.appendChild(document.createTextNode((String)message.get(USERNAME)));
		messageElement.appendChild(username);

		Element text = document.createElement(TEXT);
		text.appendChild(document.createTextNode((String)message.get(TEXT)));
		messageElement.appendChild(text);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		Element time = document.createElement(TIME);
		time.appendChild(document.createTextNode((dateFormat.format(date)).toString()+"<br>"+(timeFormat.format(date)).toString()));
		messageElement.appendChild(time);

		Element edited = document.createElement(EDITED);
		edited.appendChild(document.createTextNode(String.valueOf(message.get(EDITED))));
		messageElement.appendChild(edited);

		Element deleted = document.createElement(DELETED);
		deleted.appendChild(document.createTextNode(String.valueOf(message.get(DELETED))));
		messageElement.appendChild(deleted);

		DOMSource source = new DOMSource(document);

		Transformer transformer = getTransformer();

		StreamResult result = new StreamResult(STORAGE_LOCATION);
		transformer.transform(source, result);
	}

	public static synchronized void updateData(JSONObject message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		XMLRequestHistoryUtil.addRequest((String)message.get(ID));
		Node messageToUpdate = getNodeById(document, (String)message.get(ID));

		if (messageToUpdate != null) {

			NodeList childNodes = messageToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (TEXT.equals(node.getNodeName())) {
					node.setTextContent((String)message.get(TEXT));
				}

				if (EDITED.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(message.get(EDITED)));
				}

				if (DELETED.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(message.get(DELETED)));
				}
			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized boolean deleteData(String id) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		XMLRequestHistoryUtil.addRequest(id);
		Node messageToUpdate = getNodeById(document, id);

		if (messageToUpdate != null) {

			NodeList childNodes = messageToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (TEXT.equals(node.getNodeName())) {
					node.setTextContent(" ");
				}

				if (EDITED.equals(node.getNodeName())) {
					node.setTextContent("false");
				}

				if (DELETED.equals(node.getNodeName())) {
					node.setTextContent("true");
				}
			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			return false;
		}
		return true;
	}

	public static synchronized boolean doesStorageExist() {
		File file = new File(STORAGE_LOCATION);
		return file.exists();
	}

	public static synchronized String getMessages(int index) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		JSONObject message = new JSONObject();
		JSONArray messages= new JSONArray();
		ArrayList<String> requestId = getRequests(index);
		for (String id:requestId) {
			NodeList childNodes = getNodeById(document, id).getChildNodes();
			message.put(ID, id);
			for (int j = 0; j < childNodes.getLength(); j++) {

				Node node = childNodes.item(j);

				if (USERNAME.equals(node.getNodeName())) {
					message.put(USERNAME, node.getTextContent());
				}

				if (TEXT.equals(node.getNodeName())) {
					message.put(TEXT, node.getTextContent());
				}

				if (TIME.equals(node.getNodeName())) {
					message.put(TIME, node.getTextContent());
				}

				if (EDITED.equals(node.getNodeName())) {
					message.put(EDITED, Boolean.valueOf(node.getTextContent()));
				}

				if (DELETED.equals(node.getNodeName())) {
					message.put(DELETED, Boolean.valueOf(node.getTextContent()));
				}
			}
			messages.put(message);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES, messages);
		jsonObject.put(TOKEN, getStorageRequestSize());
		return jsonObject.toJSONString();
	}

	public static synchronized int getStorageSize() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element
		return root.getElementsByTagName(MESSAGE).getLength();
	}

	private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//" + MESSAGE + "[@id='" + id + "']");
		return (Node) expr.evaluate(doc, XPathConstants.NODE);
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// Formatting XML properly
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		return transformer;
	}

}
