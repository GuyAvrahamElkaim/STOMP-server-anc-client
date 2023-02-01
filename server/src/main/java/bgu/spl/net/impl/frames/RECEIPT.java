package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class RECEIPT extends Frame {
    int receiptId;

    public RECEIPT(int receiptId) {
        this.receiptId = receiptId;
    }

    public String execute(ConnectionHandler handler){
        String receipt = "RECEIPT" + "\n" + "receipt-id:" + receiptId + "\n" + "\n";
        return receipt;
    }

}
