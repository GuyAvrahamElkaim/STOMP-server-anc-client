package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public class CONNECTED extends Frame {
    float version;

    public CONNECTED(float version) {
        this.version = version;

    }


    @Override
    public String execute(ConnectionHandler handler) {
        String s = "CONNECTED" + "\n" + "version:" + version + "\n";
        return s;
    }
}
