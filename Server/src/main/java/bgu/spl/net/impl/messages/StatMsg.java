package bgu.spl.net.impl.messages;

public class StatMsg extends Message{
    short opcode;
    String username;

    public StatMsg(String username){
        this.opcode = 8;
        this.username = username;
    }

    public short getOpcode() {
        return opcode;
    }

    public String getUsername() {
        return username;
    }
}
