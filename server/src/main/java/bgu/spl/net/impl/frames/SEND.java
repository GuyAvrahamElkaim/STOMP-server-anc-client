package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class SEND extends Frame {
    String destination;
    String body;

    public SEND(String[] split) {
        destination = split[1].substring(12);
        body="";
        for(int i = 2; i < split.length; i++){
            body +=split[i]+'\n';
        }
    }
    public Object execute(ConnectionHandler handler){
        //send gets the subs and create a frame that will send all of them the message
        handler.send(destination, body);
        return null;
    }


}
