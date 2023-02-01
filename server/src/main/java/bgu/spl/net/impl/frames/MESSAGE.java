package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class MESSAGE extends Frame {
    int subscriptionId;
    int messageId;
    String destination;
    String body;

    //subscription :78
    //message - id :20
    //destination :/ topic / a
    public MESSAGE(int subscriptionId, String destination, String body) {
        this.subscriptionId = subscriptionId;
        this.destination = destination;
        this.body = body;
        //messageId will be defined with handler
    }


    public Object execute(ConnectionHandler connectionHandler){
        //creating unique id for the message
        messageId = connectionHandler.getConnections().getAndIncrementMessageId();
        String message = "MESSAGE" + "\n" + "subscription:" + subscriptionId + "\n" + "message-id:" + messageId + "\n" + "destination:" + destination + "\n" + "\n" + body + "\n" + "\0";
        connectionHandler.forward(message);
        return true;
    }

}
