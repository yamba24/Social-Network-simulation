package bgu.spl.net.impl.messages;

public class LogoutMsg extends Message {
    short opcode;

    public LogoutMsg(){
        this.opcode = 3;
    }

    public short getOpcode(){
        return this.opcode;
    }
}
