package bgu.spl.net.impl.messages;

public class ErrorMsg extends Message {
    short opcode;
    short msgOpcode;

    public ErrorMsg(short msgOpcode){
        this.opcode = 11;
        this.msgOpcode = msgOpcode;
    }

    public short getOpcode(){
        return this.opcode;
    }

    public short getMsgOpcode() {return this.msgOpcode;}
}
