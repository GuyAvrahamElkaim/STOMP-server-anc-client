package bgu.spl.net.impl.frames;

import bgu.spl.net.srv.ConnectionHandler;

public abstract class Frame {


    /**
     * creates the required message according to the message received
     */
    public abstract Object execute(ConnectionHandler handler);

}
