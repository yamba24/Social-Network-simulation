package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.DataBase;
import bgu.spl.net.api.bidi.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.MessagingProtocolImpl;
import bgu.spl.net.srv.Server;

public class TPCMain {

    public static void main(String[] args) {
        DataBase dataBase = new DataBase();
        Server.threadPerClient(Integer.parseInt(args[0]), ()->
        new MessagingProtocolImpl(dataBase), ()->
        new MessageEncoderDecoderImpl()).serve();
    }
}
