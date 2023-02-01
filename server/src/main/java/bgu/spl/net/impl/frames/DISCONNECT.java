package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class DISCONNECT extends Frame {
    int receiptId;

    public DISCONNECT(String[] split) {
        receiptId = Integer.parseInt(split[1].substring(8));
    }
    public Frame execute(ConnectionHandler handler){
        //will delete all of the users subscriptions and then will disconnect
        //delete all of the users subscriptions
        handler.disconnectUser();
        //send receipt
        RECEIPT receipt = new RECEIPT(receiptId);
        return receipt;
    }


}
