package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.stomp.StompMessagingProtocolImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final StompMessagingProtocolImpl<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private boolean clientLoggedIn = false;
    //guy added
    //client list with each client subscription and channel
    //will be updated in subscribe/unsubscribe
    public ConcurrentHashMap<Integer, String> subscriptionListBySubId = new ConcurrentHashMap<>();
    //subId-channel list, mapped by channel name
    public ConcurrentHashMap<String, Integer> subscriptionListByChannel = new ConcurrentHashMap<>();
    private String name;//username
    private String password;//user password
    private int connectionId;//user connection id
    private ConnectionsImpl<T> connections ;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader,
                                     StompMessagingProtocolImpl<T> protocol, ConnectionsImpl<T> connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    in.read();
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    //sends connections the message it wants to forward
    public boolean send(String channel, T msg) {
        return connections.send(channel, msg, name);
    }

    //forwards a message received from connections by another subscriber
    public void forward(String msg) {
        try {
            out.write(encdec.encode((T) msg));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName(){
        return name;
    }

    @Override
    public ConcurrentHashMap<Integer, String> getSubscriptionListBySubId() {
        return subscriptionListBySubId;
    }

    public String getPassword(){
        return password;
    }

    public boolean connectUser(String name, String password){
       clientLoggedIn =  connections.connectUser(name, password, this);
       if(clientLoggedIn){
           this.name = name;
           this.password = password;
           this.connectionId = connections.getConnectionId(name);
       }
         return clientLoggedIn;
    }



    public ConnectionsImpl<T> getConnections(){
        return connections;
    }

    public StompMessagingProtocolImpl<T> getProtocol(){
        return protocol;
    }
//checks if the user is already logged in
public boolean userIsConnected(String name){
    if(connections.clientsId.containsKey(name)){
    int id = connections.getConnectionId(name);
    if(connections.clientsLoggedIn.containsKey(id)){
        return connections.clientsLoggedIn.get(id);
    }
}
    return false;
}

    @Override
    public boolean subscribe(String channel, String userName, int id) {
        boolean isSubscribed = connections.subscribe(channel, userName);
        if(isSubscribed){
            subscriptionListBySubId.putIfAbsent(id, channel);
            subscriptionListByChannel.putIfAbsent(channel, id);
        }
        return isSubscribed;
    }

    public boolean unsubscribe(int subscriptionId){
       boolean isUnsubscribed = false;
        if(subscriptionListBySubId.containsKey(subscriptionId)){
            String channel = subscriptionListBySubId.get(subscriptionId);
             isUnsubscribed = connections.unsubscribe(channel, name);
            if(isUnsubscribed){
                String channelName = subscriptionListBySubId.get(subscriptionId);
                subscriptionListBySubId.remove(subscriptionId);
                subscriptionListByChannel.remove(channelName);
            }
        }
        //there is no such subscription id
        else{

        }
        return isUnsubscribed;
    }

    public int getSubscriptionIdByChannel(String channel){
        return subscriptionListByChannel.get(channel);
    }

    @Override
    public boolean deleteSubscriptions() {
        boolean isDeleted = true;
        for(Integer subId : subscriptionListBySubId.keySet()){
           boolean success = unsubscribe(subId);
           if(!success){
               isDeleted = false;
           }
        }
        return isDeleted;
    }

    @Override
    public boolean disconnectUser() {
       connections.disconnect(connectionId);
       deleteSubscriptions();
       connected = false;
         return true;
    }

    @Override
    public boolean clientIsLoggedIn() {
        return clientLoggedIn;
    }

}
