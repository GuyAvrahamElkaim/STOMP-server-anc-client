package bgu.spl.net.srv;

import bgu.spl.net.impl.frames.Frame;
import bgu.spl.net.impl.frames.MESSAGE;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnectionsImpl<T> implements Connections<T> {

    //chanel list
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> channels = new ConcurrentHashMap<>();
    //user list by name that show passwords
    public ConcurrentHashMap<String, String> clientsPasswords = new ConcurrentHashMap<>();
    //user list by name that show id
    public ConcurrentHashMap<String, Integer> clientsId = new ConcurrentHashMap<>();
    //user list by id that show if user already logged in
    public ConcurrentHashMap<Integer, Boolean> clientsLoggedIn = new ConcurrentHashMap<>();
    //user list by name for connection handler
    public ConcurrentHashMap<Integer, ConnectionHandler<T>> clientsConnectionHandler = new ConcurrentHashMap<>();
    //id genrator
    public AtomicInteger id = new AtomicInteger(1);
    //messageId genrator
    public AtomicInteger messageId = new AtomicInteger(1);


    @Override
    public boolean send(String channel, T msg, String sender) { // for each client in the channel, send him the msg.
        ConcurrentLinkedQueue<String> queue = channels.get(channel);
        boolean res=true;
        if (queue == null) {
            res = false;
        } else {
            res = true;
            for (String subscriber : queue) {
                if (!subscriber.equals(sender)) {
                    ConnectionHandler<T> handler = clientsConnectionHandler.get(clientsId.get(subscriber));
                    int subId = handler.getSubscriptionIdByChannel(channel);
                    Frame forward = new MESSAGE(subId, channel,(String) msg);
                    forward.execute(handler);


                }
            }
        }
        return res;
    }

    @Override
    public void disconnect(int connectionId) {
        clientsLoggedIn.remove(connectionId);
        clientsLoggedIn.put(connectionId, false);   //set user logged in to false
        //removing connection handler from list
        clientsConnectionHandler.remove(clientsConnectionHandler.get(connectionId));
    }

    public Boolean connectUser (String userName, String password, ConnectionHandler connectionHandler) {
       Boolean result = false;
        if (clientsPasswords.containsKey(userName)) {
            if (clientsPasswords.get(userName).equals(password)&&!clientsLoggedIn.get(clientsId.get(userName))) {
                clientsLoggedIn.remove(clientsId.get(userName));
                clientsLoggedIn.put(clientsId.get(userName), true);
                clientsConnectionHandler.put(getConnectionId(userName), connectionHandler);
                result = true;
            }
            else{//client exist but already logged in
                result = false;
            }
        }
       // need to create a new user
        else{
            clientsPasswords.put(userName,password);
            //need to create a new userId
            clientsId.put(userName,id.getAndIncrement());
            //need to add to the logged in list
            clientsLoggedIn.put(clientsId.get(userName),true);
            //need to add to the connection handler list
            clientsConnectionHandler.put(clientsId.get(userName),connectionHandler);
            result = true;
        }
        return result;
    }

    public int getMessagesId(){
        return messageId.getAndIncrement();
    }

    public boolean subscribe(String channel, String userName){
       boolean result = false;
       if(!channels.containsKey(channel)){
           addChannel(channel);
           }
       if(!channels.get(channel).contains(userName)){
                channels.get(channel).add(userName);
                //adding to the subs list
                result = true;
       }
        //there is no such channel
        else{
            result=false;
        }
        return result;
    }

    public boolean unsubscribe(String channel, String userName){
        boolean result = false;
        if(channels.containsKey(channel)){
            if(channels.get(channel).contains(userName)){
                channels.get(channel).remove(userName);
                //removing from the subs list
                result = true;
            }
            else{
                //ERROR not subscribed
            }
        }
        //there is no such channel
        else{
            result=false;
        }
        return result;
    }

    public int getAndIncrementMessageId(){
        return messageId.getAndIncrement();
    }

    public int getConnectionId(String name){
        return clientsId.get(name);
    }

    public boolean channelExist(String channel){
        return channels.containsKey(channel);
    }

    public void addChannel(String channel){
        channels.put(channel, new ConcurrentLinkedQueue<>());
    }
}


