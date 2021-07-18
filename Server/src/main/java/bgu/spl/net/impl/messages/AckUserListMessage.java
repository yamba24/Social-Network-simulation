package bgu.spl.net.impl.messages;

import java.util.Vector;

public class AckUserListMessage extends UserListMsg {

    private Vector<String> userNameList;
    private short numOfUsers;

    public AckUserListMessage (Vector<String> userNameList){
        this.userNameList = userNameList;
        this.numOfUsers = (short) userNameList.size();
    }

    public Vector<String> getUserNameList(){
        return this.userNameList;
    }

    public short getNumOfUsers(){
        return this.numOfUsers;
    }
}
