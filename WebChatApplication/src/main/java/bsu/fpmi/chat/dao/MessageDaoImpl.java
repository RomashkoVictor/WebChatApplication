package bsu.fpmi.chat.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import bsu.fpmi.chat.xml.XMLHistoryUtil;
import bsu.fpmi.chat.xml.XMLHistoryUtil.*;

import bsu.fpmi.chat.db.ConnectionManager;
import bsu.fpmi.chat.xml.XMLRequestHistoryUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static bsu.fpmi.chat.xml.XMLRequestHistoryUtil.getStorageRequestSize;


public class MessageDaoImpl implements MessageDao {
    private static Logger logger = Logger.getLogger(MessageDaoImpl.class.getName());

    @Override
    public void add(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO messages (id, username, text, time, edited, deleted) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, (String) message.get(XMLHistoryUtil.ID));
            preparedStatement.setString(2, (String) message.get(XMLHistoryUtil.USERNAME));
            preparedStatement.setString(3, (String) message.get(XMLHistoryUtil.TEXT));
            preparedStatement.setString(4, (String) message.get(XMLHistoryUtil.TIME));
            preparedStatement.setBoolean(5, (Boolean) message.get(XMLHistoryUtil.EDITED));
            preparedStatement.setBoolean(6, (Boolean) message.get(XMLHistoryUtil.DELETED));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, time = ?, edited = ?, deleted = ? WHERE id = ?");
            preparedStatement.setString(5, (String) message.get(XMLHistoryUtil.ID));
            preparedStatement.setString(1, (String) message.get(XMLHistoryUtil.TEXT));
            preparedStatement.setString(2, (String) message.get(XMLHistoryUtil.TIME));
            preparedStatement.setBoolean(3, (Boolean) message.get(XMLHistoryUtil.EDITED));
            preparedStatement.setBoolean(4, (Boolean) message.get(XMLHistoryUtil.DELETED));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public JSONObject getMesseges(int index) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        JSONArray messages = new JSONArray();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> requestsIds = XMLRequestHistoryUtil.getRequests(index);
        try {
            for (String idRequest : requestsIds) {
                connection = ConnectionManager.getConnection();
                JSONObject message = new JSONObject();
                preparedStatement = connection.prepareStatement("SELECT * FROM messages WHERE id = ?");
                preparedStatement.setString(1, idRequest);
                resultSet = preparedStatement.executeQuery();
                resultSet.next();
                message.put(XMLHistoryUtil.ID, resultSet.getString(XMLHistoryUtil.ID));
                message.put(XMLHistoryUtil.USERNAME, resultSet.getString(XMLHistoryUtil.USERNAME));
                message.put(XMLHistoryUtil.TEXT, resultSet.getString(XMLHistoryUtil.TEXT));
                message.put(XMLHistoryUtil.TIME, resultSet.getString(XMLHistoryUtil.TIME));
                message.put(XMLHistoryUtil.EDITED, resultSet.getBoolean(XMLHistoryUtil.EDITED));
                message.put(XMLHistoryUtil.DELETED, resultSet.getBoolean(XMLHistoryUtil.DELETED));
                messages.add(message);
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(XMLHistoryUtil.MESSAGES, messages);
        jsonObject.put(XMLHistoryUtil.TOKEN, getStorageRequestSize());
        return jsonObject;
    }


    @Override
    public void delete(String id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, edited = ?, deleted = ? WHERE id = ?");
            preparedStatement.setString(1, "");
            preparedStatement.setBoolean(2, false);
            preparedStatement.setBoolean(3, true);
            preparedStatement.setString(4, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

}
