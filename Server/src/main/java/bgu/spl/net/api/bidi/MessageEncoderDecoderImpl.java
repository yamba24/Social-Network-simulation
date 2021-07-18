package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.messages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {

    private Vector<String> parameters = new Vector<>(); // vector that saves all the parameters
    private boolean follow = false;
    private short numOfUsers = 0;
    private short numOfUsers2 = 0;
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    private byte[] bytesArray = new byte[2]; // array for converting the opcode
    private int counter = 0;
    private int opcount = 0; // counter that will count the number of zeros
    private Vector<Byte> allBytes = new Vector<>(); // a vector that used for convert message to bytes
    private Vector<Byte> followBytes = new Vector<>();

    @Override
    public Message decodeNextByte(byte nextByte) {
        // convert the two bytes into a string

        if (counter < 2) {
            bytesArray[counter] = nextByte;
            counter++;
            if(counter ==2 && bytesToShort(bytesArray) == 3){
                LogoutMsg logoutMsg = new LogoutMsg();
                counter = 0;
                bytesArray = new byte[2];
                return logoutMsg;
            }
            else if(counter == 2 && bytesToShort(bytesArray) == 7){
                counter = 0;
                bytesArray = new byte[2];
                return new UserListMsg();
            }
            else return null;
        }

        short opc = bytesToShort(bytesArray);
        if (opc == 1) {
            if (nextByte == '\0') {
                String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                parameters.addElement(parameter);
                clearBytes();
                if (parameters.size() == 2) {
                    RegisterMsg registerMsg = new RegisterMsg(parameters.get(0), parameters.get(1));
                    clearBytes();
                    counter=0;
                    bytesArray=new byte[2];
                    parameters.clear();
                    return registerMsg;
                }
                return null;
            }
            else {
                pushByte(nextByte);
                return null;
            }
        }
        else if (opc == 2) {
            if (nextByte == '\0') {
                String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                parameters.addElement(parameter);
                clearBytes();
                if (parameters.size() == 2) {
                    LoginMsg loginMsg = new LoginMsg(parameters.get(0), parameters.get(1));
                    clearBytes();
                    counter=0;
                    bytesArray=new byte[2];
                    parameters.clear();
                    return loginMsg;
                }
                return null;
            }
            else {
                pushByte(nextByte);
                return null;
            }
        }
          else if (opc == 4) {
            if (nextByte == '\0' && opcount == 0) {
                follow = true;
                opcount++;
                return null;
            } else if (nextByte == '\1' && opcount == 0) {
                follow = false;
                opcount++;

                return null;
            }
            if (opcount == 1) {
                followBytes.addElement(nextByte);
                if (followBytes.size() == 2) {
                    byte [] replace=new byte[2];
                    replace[0]=followBytes.get(0);
                    replace[1]=followBytes.get(1);
                    numOfUsers = bytesToShort(replace);
                    numOfUsers2 = numOfUsers;
                    opcount++;
                }
                return null;
            }
            if (opcount == 2) {
                if (nextByte == '\0') {
                    String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                    parameters.addElement(parameter);
                    clearBytes();
                    numOfUsers2--;
                    if (numOfUsers2 == 0) {
                        counter=0;
                        opcount=0;
                        bytesArray=new byte[2];
                        Vector<String> copyString=new Vector<>();
                        for(String s : parameters) copyString.addElement(s);
                        parameters.clear();
                        followBytes.clear();
                        boolean just=follow;
                        follow=false;
                        return new FollowMsg(copyString, just, numOfUsers);
                    }
                    return null;
                } else {
                    pushByte(nextByte);
                    return null;
                }
            }
        }
            else if (opc == 5) {
                if (nextByte == '\0') {
                    String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                    PostMsg postMsg = new PostMsg(parameter);
                    counter=0;
                    bytesArray=new byte[2];
                    clearBytes();
                        return postMsg;
                    }
                 else {
                    pushByte(nextByte);
                    return null;
                }
            }
            else if (opc == 6) {
                if (nextByte == '\0') {
                    String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                    parameters.addElement(parameter);
                    clearBytes();
                    if (parameters.size() == 2) {
                        pmMsg PM = new pmMsg(parameters.get(0), parameters.get(1));
                        parameters.clear();
                        counter=0;
                        bytesArray=new byte[2];
                        clearBytes();
                        return PM;
                    }
                    return null;
                }
                else {
                    pushByte(nextByte);
                    return null;
                }
            }
            else if (opc == 8) {
                if (nextByte == '\0') {
                    String parameter = new String(bytes, 1, len, StandardCharsets.UTF_8);
                    StatMsg statMsg = new StatMsg(parameter);
                    counter=0;
                    bytesArray= new byte[2];
                    clearBytes();
                    return statMsg;
                }
                else {
                    pushByte(nextByte);
                    return null;
                }
            }
            return null;
        }

        @Override
        public byte[] encode (Message message){
        if(message instanceof NotificationMsg){
            // push the opcode to byte
            byte[] bytes = shortToBytes(((NotificationMsg) message).getOpcode());
            allBytes.addElement(bytes[0]);
            allBytes.addElement(bytes[1]);

            // push the delimiter 0 or 1
            if(((NotificationMsg) message).getType() == '0'){
                byte byteToPush = '\0';
                allBytes.addElement(byteToPush);
            }
            else if(((NotificationMsg) message).getType() == '1'){
                byte byteToPush = '\1';
                allBytes.addElement(byteToPush);
            }

            // push the postingUser to byte
            byte[] bytes1 = ((NotificationMsg) message).getPostingUser().getBytes();
            for(int i=0;i<bytes1.length;i++){
                allBytes.addElement(bytes1[i]);
            }

            // push the delimiter 0
            byte byteTopush = '\0';
            allBytes.addElement(byteTopush);

            // push the content
            byte[] bytes2 = ((NotificationMsg) message).getContent().getBytes();
            for(int i=0;i<bytes2.length;i++){
                allBytes.addElement(bytes2[i]);
            }

            // push the delimiter 0
            allBytes.addElement(byteTopush);

            // convert the vector to array
            byte[] encodeByte = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                encodeByte[i] = allBytes.get(i);
            }
            allBytes.clear();
            return encodeByte;
        }

        if(message instanceof AckMsg && !(message instanceof AckFollowMessage || message instanceof AckStatMessage || message instanceof AckUserListMessage) ){
            byte[] bytes = shortToBytes(((AckMsg) message).getOpcode());
            allBytes.addElement(bytes[0]);
            allBytes.addElement(bytes[1]);
            byte[] bytes1 = shortToBytes(((AckMsg) message).getMsgOpcode());
            allBytes.addElement(bytes1[0]);
            allBytes.addElement(bytes1[1]);

            byte[] encodeByte = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                encodeByte[i] = allBytes.get(i);
            }
            allBytes.clear();
            return encodeByte;
        }
        else if(message instanceof AckFollowMessage){
            // encode the ack opcode
            byte[] bytes = shortToBytes((short)10);
            allBytes.addElement(bytes[0]);
            allBytes.addElement(bytes[1]);

            // encode the follow opcode
            byte[] opcodefollow = shortToBytes((short)4);
            allBytes.addElement(opcodefollow[0]);
            allBytes.addElement(opcodefollow[1]);

            // encode num of user
            byte[] numOfUserAck = shortToBytes(((AckFollowMessage) message).getNumOfUsers());
            allBytes.addElement(numOfUserAck[0]);
            allBytes.addElement(numOfUserAck[1]);

            // encode the list of string
            for(int i = 0; i<((AckFollowMessage) message).getUsernameList().size(); i++){
               byte[] bytes123 =  ((AckFollowMessage) message).getUsernameList().get(i).getBytes();
               for(int j = 0; j<bytes123.length; j++){
                   allBytes.addElement(bytes123[j]);
               }
               byte bytetopush = '\0';
               allBytes.addElement(bytetopush);
            }
            byte[] arraytoret = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                arraytoret[i] = allBytes.get(i);
            }
            allBytes.clear();
            return arraytoret;
        }
        else if(message instanceof AckUserListMessage){
            // encode the ack opcode
            byte[] opcodearray = shortToBytes((short)10);
            allBytes.addElement(opcodearray[0]);
            allBytes.addElement(opcodearray[1]);

            // encode the message opcode
            byte[] userlistopcode = shortToBytes((short)7);
            allBytes.addElement(userlistopcode[0]);
            allBytes.addElement(userlistopcode[1]);

            //encode the num of user
            byte[] numofuser = shortToBytes(((AckUserListMessage) message).getNumOfUsers());
            allBytes.addElement(numofuser[0]);
            allBytes.addElement(numofuser[1]);

            for(int i = 0; i<((AckUserListMessage) message).getUserNameList().size(); i++){
                byte[] byte123 = ((AckUserListMessage) message).getUserNameList().get(i).getBytes();
                for(int j = 0; j<byte123.length; j++){
                    allBytes.addElement(byte123[j]);
                }
                byte bytetopush = '\0';
                allBytes.addElement(bytetopush);
            }
            byte[] arraytoret = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                arraytoret[i] = allBytes.get(i);
            }
            allBytes.clear();
            return arraytoret;
        }

        else if(message instanceof AckStatMessage){
            byte[] ackopcode = shortToBytes((short)10);
            allBytes.addElement(ackopcode[0]);
            allBytes.addElement(ackopcode[1]);
            byte[] statopcode = shortToBytes((short)8);
            allBytes.addElement(statopcode[0]);
            allBytes.addElement(statopcode[1]);
            byte[] numofpost = shortToBytes(((AckStatMessage)message).getPostCount());
            allBytes.addElement(numofpost[0]);
            allBytes.addElement(numofpost[1]);
            byte[] followers = shortToBytes(((AckStatMessage)message).getNumOfFollowers());
            allBytes.addElement(followers[0]);
            allBytes.addElement(followers[1]);
            byte[] following = shortToBytes(((AckStatMessage)message).getNumOfFollowing());
            allBytes.addElement(following[0]);
            allBytes.addElement(following[1]);

            byte[] arraytoret = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                arraytoret[i] = allBytes.get(i);
            }
            allBytes.clear();
            return arraytoret;

        }
        if(message instanceof ErrorMsg){
            byte[] bytes = shortToBytes(((ErrorMsg) message).getOpcode());
            allBytes.addElement(bytes[0]);
            allBytes.addElement(bytes[1]);
            byte[] bytes1 = shortToBytes(((ErrorMsg) message).getMsgOpcode());
            allBytes.addElement(bytes1[0]);
            allBytes.addElement(bytes1[1]);

            byte[] encodeByte = new byte[allBytes.size()];
            for(int i = 0; i<allBytes.size(); i++){
                encodeByte[i] = allBytes.get(i);
            }
            allBytes.clear();
            return encodeByte;
        }
        return null;
        }

        // convert byte to short
        public short bytesToShort ( byte[] byteArr){
            short result = (short) ((byteArr[0] & 0xff) << 8);
            result += (short) (byteArr[1] & 0xff);
            return result;

        }

    // convert short to byte array
        public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

        // increase the array
        private void pushByte ( byte nextByte){
            if (len >= bytes.length) {
                bytes = Arrays.copyOf(bytes, len * 2);
            }
            len++;
            bytes[len] = nextByte;
        }
        private void clearBytes () {
            bytes = new byte[1 << 10];
            len = 0;
        }
        // a function that push the bytes into a vector of bytes
        public void pushBytes(byte[] someBytes){
        for(int i = 0; i<someBytes.length; i++){
            allBytes.addElement(someBytes[i]);
        }
    }




}