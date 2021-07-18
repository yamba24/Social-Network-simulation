package bgu.spl.net.impl.messages;

import java.util.Vector;

public class FollowMsg extends Message {
    private boolean followOrUn;
    private short numOfUsers;
    private Vector<String> UserNameList;
    short opcode;

    public FollowMsg(Vector<String> UserNameList, boolean getFolloworUn, short numOfUsers){

        this.UserNameList = UserNameList;
        this.followOrUn = getFolloworUn;
        this.numOfUsers = numOfUsers;
        this.opcode = 4;
    }

    public short getOpcode() {
        return opcode;
    }

    public Vector<String> getUserNameList() {return this.UserNameList;}

    public boolean getFolloworUn(){ return this.followOrUn;}
}
