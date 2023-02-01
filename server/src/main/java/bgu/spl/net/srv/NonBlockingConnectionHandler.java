package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.stomp.StompMessagingProtocolImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockingConnectionHandler<T> implements ConnectionHandler<T> {

    private static final int BUFFER_ALLOCATION_SIZE = 1 << 13; //8k
    private static final ConcurrentLinkedQueue<ByteBuffer> BUFFER_POOL = new ConcurrentLinkedQueue<>();

    private final StompMessagingProtocolImpl<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
    private final SocketChannel chan;
    private final Reactor reactor;
    //guy added
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

    public NonBlockingConnectionHandler(
            MessageEncoderDecoder<T> reader,
            StompMessagingProtocolImpl<T> protocol,
            SocketChannel chan,
            Reactor reactor,
            ConnectionsImpl<T> connections) {
        this.chan = chan;
        this.encdec = reader;
        this.protocol = protocol;
        this.reactor = reactor;
        this.connections = connections;
    }

    public Runnable continueRead() {
        ByteBuffer buf = leaseBuffer();

        boolean success = false;
        try {
            success = chan.read(buf) != -1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (success) {
            buf.flip();
            return () -> {
                try {
                    while (buf.hasRemaining()) {
                        T nextMessage = encdec.decodeNextByte(buf.get());
                        if (nextMessage != null) {
                            protocol.process(nextMessage);
                        }
                    }
                } finally {
                    releaseBuffer(buf);
                }
            };
        } else {
            releaseBuffer(buf);
            close();
            return null;
        }

    }

    public void close() {
        try {
            chan.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClosed() {
        return !chan.isOpen();
    }

    public void continueWrite() {
        while (!writeQueue.isEmpty()) {
            try {
                ByteBuffer top = writeQueue.peek();
                chan.write(top);
                if (top.hasRemaining()) {
                    return;
                } else {
                    writeQueue.remove();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                close();
            }
        }

        if (writeQueue.isEmpty()) {
            if (protocol.shouldTerminate()) close();
            else reactor.updateInterestedOps(chan, SelectionKey.OP_READ);
        }
    }

    private static ByteBuffer leaseBuffer() {
        ByteBuffer buff = BUFFER_POOL.poll();
        if (buff == null) {
            return ByteBuffer.allocateDirect(BUFFER_ALLOCATION_SIZE);
        }

        buff.clear();
        return buff;
    }

    private static void releaseBuffer(ByteBuffer buff) {
        BUFFER_POOL.add(buff);
    }

    @Override
    public boolean send(String channel, T msg) {
        return connections.send(channel, msg, name);
    }

    @Override
    public void forward(String msg) {
        writeQueue.add(ByteBuffer.wrap(encdec.encode((T)msg)));
        reactor.updateInterestedOps(chan, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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
        clientLoggedIn =  connections.connectUser(name, password,this);
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
        return true;
    }

    @Override
    public boolean clientIsLoggedIn() {
        return clientLoggedIn;
    }

}
