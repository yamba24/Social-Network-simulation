package bgu.spl.net.impl.messages;

public class LoginMsg extends Message{
    private String username;
    private String password;
    short opcode;

    public LoginMsg(String username, String password){
        this.username = username;
        this.password = password;
        opcode = 2;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public short getOpcode(){
        return this.opcode;
    }
}
