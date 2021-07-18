package bgu.spl.net.impl.messages;

import java.util.Vector;

public class AckFollowMessage extends AckMsg {
    private Vector<String> usernameList;
    private short numOfUsers;
    private boolean follow;

    public AckFollowMessage(Vector<String> userNameList,boolean follow) {
        this.usernameList = userNameList;
        this.numOfUsers = (short)usernameList.size();
        this.follow=follow;
    }

    public Vector<String> getUsernameList() {
        return usernameList;
    }

    public short getNumOfUsers(){return this.numOfUsers;}

    public boolean isFollow() {
        return follow;
    }
}
