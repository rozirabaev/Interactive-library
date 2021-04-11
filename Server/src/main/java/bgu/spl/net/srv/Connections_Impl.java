package bgu.spl.net.srv;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connections_Impl implements Connections<String> {
    private ConcurrentHashMap<Integer, ConnectionHandler> connectionsList;
    private ConcurrentHashMap<String, Topic> topics;

    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> subscriptionsById;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> subscriptionsByTopic;

    private ConcurrentHashMap<String, String> usersAndPassword;


    public Connections_Impl() {
        connectionsList = new ConcurrentHashMap<>();
        topics = new ConcurrentHashMap<>();
        subscriptionsById = new ConcurrentHashMap<>();
        subscriptionsByTopic = new ConcurrentHashMap<>();
        usersAndPassword = new ConcurrentHashMap<>();
    }

    public ConnectionHandler getHandler(int id) {
        return connectionsList.get(id);
    }


    public void subscribe(Integer conId, String topic, int id) { //subscribe
        //add to topics
        if (topics.get(topic) == null) {
            Topic topic1 = new Topic();
            topic1.addHandler(conId);
            topics.put(topic, topic1);
        } else {
            topics.get(topic).addHandler(conId);
        }
        //add subscriptions
        if (subscriptionsByTopic.get(conId) == null) {
            ConcurrentHashMap Top = new ConcurrentHashMap();
            Top.put(topic, id);
            subscriptionsByTopic.put(conId, Top);
        } else {
            subscriptionsByTopic.get(conId).put(topic, id);
        }
        if (subscriptionsById.get(conId) == null) {
            ConcurrentHashMap Top = new ConcurrentHashMap();
            Top.put(id, topic);
            subscriptionsById.put(conId, Top);
        } else {
            subscriptionsById.get(conId).put(id, topic);
        }
    }

    public void unsubscribe(Integer conId, int id) {
        //removing from the topic
        if(subscriptionsById.contains(conId)) {
            String top = subscriptionsById.get(conId).get(id);
            Topic topic = topics.get(top);
            topic.removeSubscriber(conId);
            //removing the subscription
            subscriptionsById.get(conId).remove(id);
            subscriptionsByTopic.remove(top);
        }
    }

    @Override
    public boolean send(int connectionId, String msg) {
        ConnectionHandler hand = connectionsList.get(connectionId);
        synchronized (hand) {
            hand.send(msg);
        }
        return true;
    }

    @Override
    public void send(String channel, String msg) {
        Topic topic = topics.get(channel);
        if(topic!=null && topic.gethandlerId()!=null) {
            for (int conId : topic.gethandlerId()) {
                ConnectionHandler hand = connectionsList.get(conId);
                String tokens[] = msg.split("%");
                int id=subscriptionsByTopic.get(conId).get(channel);
                String message = tokens[0] + id + tokens[1];

                synchronized (hand) {
                    hand.send(message);
                }
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler<String> hand = connectionsList.get(connectionId);
        ConcurrentHashMap<Integer, String> map = subscriptionsById.get(connectionId);
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            topics.get(entry.getValue()).removeSubscriber(connectionId);
        }
        connectionsList.remove(connectionId);
    }

    @Override
    public void connect(int connectionId) {

    }


    public ConcurrentHashMap<Integer, ConnectionHandler> getConnectionsList() {
        return connectionsList;
    }

    public ConcurrentHashMap<String, String> getUsersAndPassword() {
        return usersAndPassword;
    }

    public int getSubscriptionId(int conId, String topic) {
        return subscriptionsByTopic.get(conId).get(topic);
    }

    public void addUser(String name, String password) {
        usersAndPassword.put(name, password);
    }

    public void disconnect(String message, int conId){
        send(conId,message);
        disconnect(conId);
    }
}
