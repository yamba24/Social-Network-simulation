package bgu.spl.net.impl.messages;

public class RegisterMsg extends Message {
    String username;
    String password;
    short opcode;

    public RegisterMsg(String username, String password){
        this.username = username;
        this.password = password;
        this.opcode = 1;
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
