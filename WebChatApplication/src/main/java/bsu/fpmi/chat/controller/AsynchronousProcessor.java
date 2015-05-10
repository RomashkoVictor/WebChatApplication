package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.xml.XMLHistoryUtil;
import bsu.fpmi.chat.util.ServletUtil;
import bsu.fpmi.chat.xml.XMLRequestHistoryUtil;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static bsu.fpmi.chat.util.MessageUtil.TOKEN;
import static bsu.fpmi.chat.util.MessageUtil.getIndex;

public final class AsynchronousProcessor {
    private final static Queue<AsyncContext> storage = new ConcurrentLinkedQueue<AsyncContext>();

    public static void notifyAllClients() {
        for (AsyncContext asyncContext : storage) {
            getMessages(asyncContext);
            asyncContext.complete();
            storage.remove(asyncContext);
        }
    }

    public static void getMessages(AsyncContext asyncContext) {
        String token = asyncContext.getRequest().getParameter(TOKEN);

        try {
            if (token != null && !"".equals(token)) {
                int index = getIndex(token);
                String messages = XMLHistoryUtil.getMessages(index);
                asyncContext.getResponse().setContentType(ServletUtil.APPLICATION_JSON);
                asyncContext.getResponse().setCharacterEncoding("utf-8");
                PrintWriter out = asyncContext.getResponse().getWriter();
                out.print(messages);
                out.flush();
            } else {
                ((HttpServletResponse) asyncContext.getResponse()).sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
            }
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {

        }
    }

    public static void addAsyncContext(final AsyncContext context) {
        context.addListener(new AsyncListener() {

            public void onTimeout(AsyncEvent event) throws IOException {
                removeAsyncContext(context);
            }

            public void onStartAsync(AsyncEvent event) throws IOException {
            }

            public void onError(AsyncEvent event) throws IOException {
                removeAsyncContext(context);
            }

            public void onComplete(AsyncEvent event) throws IOException {
                removeAsyncContext(context);
            }
        });
        int token = Integer.parseInt(context.getRequest().getParameter(TOKEN));
        try {
            if (token == XMLRequestHistoryUtil.getStorageRequestSize())
                storage.add(context);
            else {
                getMessages(context);
                context.complete();
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {


        }
    }

    private static void removeAsyncContext(final AsyncContext context) {
        storage.remove(context);
    }

}
