/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.net.srv;

import bgu.spl.net.impl.stomp.StompMessagingProtocolImpl;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ConnectionHandler interface for Message of type T
 */
public interface ConnectionHandler<T> extends Closeable {

    /**
     * Comment the following lines (both send methods) for the existing implementations to work.
     *
     */

    boolean send(String channel, T msg);

    void forward(String msg);

    /**
     * gooes to connections and check if there is a user with compitable name and password
     * **/
//guy added
    boolean connectUser(String name, String password);
    ConnectionsImpl<T> getConnections();
    StompMessagingProtocolImpl<T> getProtocol();
    boolean subscribe(String channel, String userName,int connectionId);
    boolean unsubscribe(int subscriptionId);
    boolean userIsConnected(String name);
    String getName();
    ConcurrentHashMap<Integer, String> getSubscriptionListBySubId();
    int getSubscriptionIdByChannel(String channel);
    boolean deleteSubscriptions();
    boolean disconnectUser();
    boolean clientIsLoggedIn();

}
