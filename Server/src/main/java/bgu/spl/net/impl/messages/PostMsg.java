package bgu.spl.net.impl.messages;

public class PostMsg extends Message {
    short opcode;
    String content;

    public PostMsg(String content){
        this.opcode = 5;
        this.content = content;
    }

    public String getContent(){
        return this.content;
    }

    public short getOpcode(){
        return this.opcode;
    }
}
