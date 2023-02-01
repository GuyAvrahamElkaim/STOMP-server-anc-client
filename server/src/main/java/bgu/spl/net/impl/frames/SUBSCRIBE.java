package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class SUBSCRIBE extends Frame {
    String destination;
    int id;
    int receiptId;
    String message;

    public SUBSCRIBE(String[] split) {
        destination = split[1].substring(12);
        id = Integer.parseInt(split[2].substring(3));
        receiptId = Integer.parseInt(split[3].substring(8));
        for (int i = 1; i <split.length ; i++) {
            message+=split[i];
        }
    }
    public Frame execute(ConnectionHandler handler){
        handler.subscribe(destination, handler.getName(),id);
        return new RECEIPT(receiptId);

    }


}
