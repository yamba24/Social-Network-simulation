package bgu.spl.net.impl.messages;

public class NotificationMsg extends Message{
    private short opcode;
    private char type;
    private String PostingUser;
    private String content;

    public NotificationMsg(char type, String PostingUser, String content){
        this.opcode = 9;
        this.type = type;
        this.PostingUser = PostingUser;
        this.content = content;
    }

    public short getOpcode() {
        return opcode;
    }

    public char getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getPostingUser(){
        return this.PostingUser;
    }

}
