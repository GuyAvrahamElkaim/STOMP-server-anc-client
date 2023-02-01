package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this


    //    Server.threadPerClient(
    //            7777, //port
    //            () -> new StompMessagingProtocolImpl<>(), //protocol factory
    //            MessageEncoderDecoderStomp::new,//message encoder decoder factory
    //            new ConnectionsImpl<>()//connections
    //    ).serve();

         Server.reactor(
                 Runtime.getRuntime().availableProcessors(),
                 7777, //port
                 () ->  new StompMessagingProtocolImpl<>(), //protocol factory
                 MessageEncoderDecoderStomp::new,//message encoder decoder factory
                 new ConnectionsImpl<>()//connections
         ).serve();
    }
}
