package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.util.ServletUtil;
import bsu.fpmi.chat.xml.XMLHistoryUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;

import static bsu.fpmi.chat.util.MessageUtil.*;

@WebServlet("/Chat")
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MessageServlet.class.getName());

    @Override
    public void init() throws ServletException {
        try {
            loadHistory();
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            logger.error(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter(TOKEN);
        try {
            if (token != null && !"".equals(token)) {
                int index = getIndex(token);
                logger.info("Index " + index);
                String messages = XMLHistoryUtil.getMessages(index);
                response.setContentType(ServletUtil.APPLICATION_JSON);
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.print(messages);
                out.flush();
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPost");
        String data = ServletUtil.getMessageBody(request);
        logger.info(data);
        try {
            JSONObject message = stringToJson(data);
            //Message message = jsonToMessage(json);
            // MessageStorage.addMessage(message);
            XMLHistoryUtil.addData(message);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPut");
        String data = ServletUtil.getMessageBody(request);
        logger.info(data);
        try {
            JSONObject message = stringToJson(data);
            if (message != null) {
                XMLHistoryUtil.updateData(message);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task does not exist");
            }
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doDelete");
        String data = ServletUtil.getMessageBody(request);
        logger.info(data);
        String id = request.getParameter(ID);
        try {
            JSONObject message = stringToJson(data);
            if (message != null) {
                XMLHistoryUtil.updateData(message);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task does not exist");
            }
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void loadHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException {
        if (!XMLHistoryUtil.doesStorageExist()) {
            XMLHistoryUtil.createStorage();
            //addStubData();
        }
    }
}
