package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    boolean send(String receiver, T msg, String sender);

    void disconnect(int connectionId);
}
