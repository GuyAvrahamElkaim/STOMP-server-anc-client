package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class ERROR extends Frame {
    Integer receiptId;
    String error;
    String message;


    public ERROR(String error, String message) {
        this.error = error;
        this.message=message;
    }

    public ERROR(String error, String message, int receiptId) {
        this.error = error;
        this.message=message;
        this.receiptId=receiptId;
    }

    public String execute(ConnectionHandler handler){
        if(receiptId==null){
            return "ERROR" +'\n'+"receipt-id:" +"no receipt"+'\n'+"message:" + error + "\n" +"the message:"+"\n"+"MESSAGE"+"\n"+message+ "\n"+"\n" + "\u0000";
        }
        else{
            return "ERROR" + "\n" + "receipt-id:" + receiptId + "\n" + "message:" + error + "\n" +"the message:"+"\n"+"MESSAGE"+"\n"+message+ "\n" + "\n" + "\u0000";
        }

    }


}
