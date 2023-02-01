package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class UNSUBSCRIBE extends Frame {
    int id;
    int receiptId;
    String message;
    public UNSUBSCRIBE(String[] split) {

        id = Integer.parseInt(split[1].substring(3));
        receiptId = Integer.parseInt(split[2].substring(8));
        for (int i = 1; i <split.length ; i++) {
            message+=split[i];
        }
    }

    public Frame execute(ConnectionHandler handler){
        handler.unsubscribe(id);
        return new RECEIPT(receiptId);


    }


}
