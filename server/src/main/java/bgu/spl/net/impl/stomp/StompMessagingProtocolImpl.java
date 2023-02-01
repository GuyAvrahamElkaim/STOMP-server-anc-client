package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.frames.ERROR;
import bgu.spl.net.impl.frames.Frame;
import bgu.spl.net.impl.frames.CONNECT;
import bgu.spl.net.impl.frames.DISCONNECT;
import bgu.spl.net.impl.frames.SEND;
import bgu.spl.net.impl.frames.SUBSCRIBE;
import bgu.spl.net.impl.frames.UNSUBSCRIBE;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.ConnectionsImpl;

public class StompMessagingProtocolImpl <T> implements bgu.spl.net.api.StompMessagingProtocol {
    private ConnectionsImpl<T> connections; // the connections object of the server
    private ConnectionHandler<T> connectionHandler;


    @Override
    public void start(ConnectionsImpl connections) {
        this.connections = connections;
    }

    @Override
    //make proccess void and use forward to  send a message
    //only use forward for messages
    public void process(Object msg) {
        if (msg instanceof String) {
            //in case of an error in the message relevant error will be provided through the frames execute method
            Frame receivedFrame = stringToFrame((String) msg);
            if((receivedFrame instanceof SEND)){
                receivedFrame.execute(connectionHandler);
            }
            else if(receivedFrame!=null) {
                Frame answer = (Frame) receivedFrame.execute(connectionHandler);
                String ans = (String) answer.execute(connectionHandler);
                connectionHandler.forward(ans);
            }
        }
    }


    @Override
    public boolean shouldTerminate() {
        //implement
        return false;
    }

    public Frame stringToFrame(Object msg) {
        if (msg instanceof String) {
            String message = (String) msg;
            String[] split = message.split("\n");
            if (split[0].equals("CONNECT")) {
                return new CONNECT(split);
            }
            if (split[0].equals("SUBSCRIBE")) {
                return new SUBSCRIBE(split);
            }
            if (split[0].equals("UNSUBSCRIBE")) {
                return new UNSUBSCRIBE(split);
            }
            if (split[0].equals("DISCONNECT")) {
                return new DISCONNECT(split);
            }
            if (split[0].equals("SEND")) {
                return new SEND(split);
            }
            
        }

        return null;
    }

    public ConnectionsImpl<T> getConnections() {
        return connections;
    }

    public ConnectionHandler<T> getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(ConnectionHandler<T> connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
}
