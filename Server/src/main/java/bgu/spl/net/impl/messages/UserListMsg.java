package bgu.spl.net.impl.messages;

public class UserListMsg extends Message {
    short opcoder;

    public UserListMsg(){

        this.opcoder = 7;
    }

    public short getOpcode() {
        return opcoder;
    }
}
