package bgu.spl.net.impl.messages;

public class pmMsg extends Message {
    String username;
    String content;
    short opcode;

    public pmMsg(String username, String content){
        this.username = username;
        this.content = content;
        this.opcode = 6;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public short getOpcode() {
        return opcode;
    }

}
