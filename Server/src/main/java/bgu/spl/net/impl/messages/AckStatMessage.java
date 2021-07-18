package bgu.spl.net.impl.messages;

public class AckStatMessage extends AckMsg {

    private short postCount;
    private short numOfFollowers;
    private short numOfFollowing;

    public AckStatMessage(short postCount, short numOfFollowers, short numOfFollowing) {
        this.numOfFollowers = numOfFollowers;
        this.postCount = postCount;
        this.numOfFollowing = numOfFollowing;
    }

    public short getPostCount() {
        return postCount;
    }

    public short getNumOfFollowers() {
        return numOfFollowers;
    }

    public short getNumOfFollowing() {
        return numOfFollowing;
    }
}
