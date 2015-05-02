package bsu.fpmi.chat.xml;

import bsu.fpmi.chat.model.Message;
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
import java.util.ArrayList;
import java.util.List;

public final class XMLHistoryUtil {
	private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml"; // history.xml will be located in the home directory
	public static final String MESSAGES = "messages";
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

	public static synchronized void addData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();

		Element root = document.getDocumentElement(); // Root <tasks> element

		Element messageElement = document.createElement(MESSAGE);
		root.appendChild(messageElement);

		messageElement.setAttribute(ID, message.getId());

		Element username = document.createElement(USERNAME);
		username.appendChild(document.createTextNode(message.getUsername()));
		messageElement.appendChild(username);

		Element text = document.createElement(TEXT);
		text.appendChild(document.createTextNode(message.getText()));
		messageElement.appendChild(text);

		Element time = document.createElement(TIME);
		time.appendChild(document.createTextNode(message.getTime()));
		messageElement.appendChild(time);

		Element edited = document.createElement(EDITED);
		edited.appendChild(document.createTextNode(String.valueOf(message.isEdited())));
		messageElement.appendChild(edited);

		Element deleted = document.createElement(DELETED);
		deleted.appendChild(document.createTextNode(String.valueOf(message.isDeleted())));
		messageElement.appendChild(deleted);

		DOMSource source = new DOMSource(document);

		Transformer transformer = getTransformer();

		StreamResult result = new StreamResult(STORAGE_LOCATION);
		transformer.transform(source, result);
	}

	public static synchronized void updateData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Node messageToUpdate = getNodeById(document, message.getId());

		if (messageToUpdate != null) {

			NodeList childNodes = messageToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if (TEXT.equals(node.getNodeName())) {
					node.setTextContent(message.getText());
				}

				if (EDITED.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(message.isEdited()));
				}

				if (DELETED.equals(node.getNodeName())) {
					node.setTextContent(String.valueOf(message.isDeleted()));
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

	public static synchronized boolean doesStorageExist() {
		File file = new File(STORAGE_LOCATION);
		return file.exists();
	}

	public static synchronized List<Message> getMessages() throws SAXException, IOException, ParserConfigurationException {
		List<Message> messages = new ArrayList<>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element
		NodeList messageList = root.getElementsByTagName(MESSAGE);
		for (int i = 0; i < messageList.getLength(); i++) {
			Element messageElement = (Element) messageList.item(i);
			String id = messageElement.getAttribute(ID);
			String username = messageElement.getElementsByTagName(USERNAME).item(0).getTextContent();
			String text = messageElement.getElementsByTagName(TEXT).item(0).getTextContent();
			String time = messageElement.getElementsByTagName(TIME).item(0).getTextContent();
			boolean edited = Boolean.valueOf(messageElement.getElementsByTagName(EDITED).item(0).getTextContent());
			boolean deleted = Boolean.valueOf(messageElement.getElementsByTagName(DELETED).item(0).getTextContent());
			messages.add(new Message(username, text, time, id, edited, deleted));
		}
		return messages;
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
