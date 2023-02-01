package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class CONNECT extends Frame {
    float version=-1;
    String host;
    String login;
    String passcode;
    String message;
    public CONNECT(String[] split) {
        //the headers can be in a permutation of any order
        for (int i = 1; i <5 ; i++) {
            if(split[i].contains("version:")){
                version = Float.parseFloat(split[i].substring(15));
            }
            else if(split[i].contains("host:")){
                host = split[i].substring(5);
            }
            else if(split[i].contains("login:")){
                login = split[i].substring(6);            }
            else if(split[i].contains("passcode:")){
                passcode = split[i].substring(9);
            }
        }
        for (int i = 5; i <split.length ; i++) {
            message+=split[i];
        }
        //checks every header is filled
        if(version==-1|| host==null || login==null || passcode==null){
            throw new IllegalArgumentException("ERROR: Invalid message");
        }
    }

    public Frame execute(ConnectionHandler handler) {
        if (handler.userIsConnected(login)) {
            return new ERROR("User already logged in", message);
        }
        else if(handler.clientIsLoggedIn()){
            return new ERROR("The client is already logged in, log out before trying again", message);
        }
        else {
            boolean success = handler.connectUser(login, passcode);
            if (success) {
                return new CONNECTED(version);
            } else {//need to make sure the string sent is correct
                return new ERROR("Wrong password", message);
            }

        }
    }
}
