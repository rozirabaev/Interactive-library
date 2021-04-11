package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Connections_Impl;

import java.lang.String;


import java.util.concurrent.atomic.AtomicInteger;

public class STOMP_MessagingProtocol implements StompMessagingProtocol<String> {
    private boolean shouldTerminate = false;
    private static AtomicInteger msgId;
    private int conId;
    private Connections_Impl connections;
    private boolean isLoged;

    public STOMP_MessagingProtocol() {
        msgId = new AtomicInteger(1);
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        shouldTerminate = false;
        this.connections = (Connections_Impl) connections;
        this.conId = connectionId;
        this.isLoged = false;
    }

    @Override
    public void process(String message) {
        String[] tokens = message.split("\n");
        String action = tokens[0];
        if (action.equals("CONNECT")) {
            connect(tokens, conId);
            return;
        }
        if (action.equals("SEND")) {
            send(tokens, conId);
            return;
        }
        if (action.equals("SUBSCRIBE")) {
            subscribe(tokens, conId);
            return;
        }
        if (action.equals("UNSUBSCRIBE")) {
            unsubscribe(tokens, conId);
            return;
        }
        if (action.equals("DISCONNECT")) {
            disconnect(tokens, conId);
            return;
        }
        //should we add connect to process?
       /* else
            sendEror( "Wrong head frame recieved", conId);
*/
    }

    private void connect(String[] tokens, int conId) {
        if (isLoged) {
            sendEror("user already logged!", conId);
            return;
        }
        String version = findHeader("accept-version", tokens);
        if (!(version.equals("1.2"))) {
            return;
        }
        //to check if we should check the host port?
        String login = findHeader("login", tokens);
        String password = findHeader("passcode",tokens);
        if (login.equals("") | password.equals("")) {
            return;
        }
        if (connections.getUsersAndPassword().containsKey(login)) {
            String pass = connections.getUsersAndPassword().get(login);
            if (!pass.equals(password)) {
                sendEror("incorrect password!", conId);
                return;
            }
            isLoged = true;
            sendConnected(conId);
        } else {
            isLoged = true;
            connections.addUser(login, password);
            sendConnected(conId);
        }
        String receipt = findHeader("receipt", tokens);
        if (!receipt.equals(""))
            receipt(conId, receipt, 0);

    }

    private void send(String[] tokens, int conId) {
        String topic = findHeader("destination", tokens);
        if (topic.equals("")) {
            return;
        }
        String message = tokens[tokens.length-1];
        int currentMsgId;
        synchronized (msgId) {
            currentMsgId = msgId.get();
            msgId.set(msgId.get() + 1);
        }
        String MessageFrame = "MESSAGE" + "\n" + "subscription:" + "%" + "\n" + "Message-id:" + currentMsgId + "\n" + "destination:" + topic + "\n" + "\n" + message + "\n";
        connections.send(topic, MessageFrame);
    }

    private void subscribe(String[] msg, int conId) {
        //checks correct destination
        String destinaton = findHeader("destination", msg);
        if (destinaton.equals(""))
            return;
        //checks correct id
        String theId = findHeader("id", msg);
        if (theId.equals("")) {
            return;
        }
        int id = Integer.parseInt(theId);
        //checks cocrrect reciept
        String receipt = findHeader("receipt", msg);
        if (receipt.equals("")) {
            return;
        }
        connections.subscribe(conId, destinaton, id);
        receipt(conId, receipt, 0); // sends receipt
    }

    private void unsubscribe(String[] msg, int conId) {
        String id = findHeader("id", msg);
        if (id.equals("")) {
            return;
        }
        connections.unsubscribe(conId, Integer.parseInt(id));
        String receipt = findHeader("receipt", msg);
        if (!receipt.equals(""))
                receipt(conId, receipt, 0);
        }


    private void disconnect(String[] tokens, int conId) {
        String receipt = findHeader("receipt", tokens);
        if (receipt.equals("")) {
            return;
        }
        receipt(conId, receipt, 1); // sends receipt to disconnect
    }

    //sends receipt frame
    private void receipt(int conId, String msg, int disconnect) {
        String frameReceipt = "RECEIPT" + "\n" + "receipt-id:" + msg + "\n";
        if (disconnect == 0)
            connections.send(conId, frameReceipt);
        else
            connections.disconnect(frameReceipt, conId);
    }

    //send error frame
    private void sendEror(java.lang.String msg, int conId) {
        String frameError = "ERROR" + "\n" + msg + "\n";
        connections.send(conId, frameError);
        this.isLoged = false;
        shouldTerminate = true;
    }

    private void sendConnected(int conId) {
        String connectedFrame = "CONNECTED" + "\n" + "version:1.2" + "\n";
        connections.send(conId, connectedFrame);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    private String findHeader(String toFind, String[] findIn) {
        for (String s : findIn) {
            String[] line = s.split(":");
            if (line[0].equals(toFind))
                return line[1];
        }
        return "";
    }
}


