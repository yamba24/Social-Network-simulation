package bgu.spl.net.impl.messages;

public class AckMsg extends Message{

    private short opcode = 10;
    private short msgOpcode;

    public AckMsg(){} // empty constructor

    public AckMsg(short msgOpcode){

        this.msgOpcode = msgOpcode;
    }

    public short getMsgOpcode() {
        return msgOpcode;
    }

    public short getOpcode(){
        return 10;
     }
}
