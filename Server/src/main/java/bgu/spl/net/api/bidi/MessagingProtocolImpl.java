package bgu.spl.net.api.bidi;

import bgu.spl.net.impl.messages.*;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MessagingProtocolImpl implements BidiMessagingProtocol<Message> {
    boolean shouldTerminate = false;
    private DataBase dataBase;
    private User user;
    private Connections con;
    private int connectionId;


    public MessagingProtocolImpl(DataBase dataBase){
        this.dataBase = dataBase;
        this.user = new User (null, null);
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.con = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(Message message) {
        if(message instanceof RegisterMsg){
            RegisterProcess((RegisterMsg) message);
        }
        else if(message instanceof LoginMsg){
            LoginProcess((LoginMsg) message);
        }
        else if(message instanceof LogoutMsg){
            LogoutProcess((LogoutMsg) message);
        }
        else if(message instanceof FollowMsg){
            FollowProcess((FollowMsg) message);
        }
        else if(message instanceof PostMsg){
            PostProcess((PostMsg) message);
        }
        else if(message instanceof pmMsg){
            PMProcess((pmMsg) message);
        }
        else if(message instanceof UserListMsg){
            UserListProcess((UserListMsg) message);
        }
        else if(message instanceof StatMsg){
            StatProcess((StatMsg) message);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void RegisterProcess (RegisterMsg msg) {
        synchronized (dataBase.getRegisteredClients()) {
            if (dataBase.getRegisteredClients().containsKey(msg.getUsername())) {
                ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
                con.send(connectionId, errorMsg);
            } else {
                dataBase.getRegisteredClients().putIfAbsent(msg.getUsername(), msg.getPassword()); // using put if absent because its thread safe
                dataBase.getUserNameList().addElement(msg.getUsername());
                // if the user is registered than he can accept and send messages
                dataBase.getPmPerUser().putIfAbsent(msg.getUsername(), new ConcurrentLinkedQueue<>());
                dataBase.getPostPerUser().putIfAbsent(msg.getUsername(), new ConcurrentLinkedQueue<>());
                // if the user is registered than he can follow and be followed
                dataBase.getFollowers().putIfAbsent(msg.getUsername(), new Vector<>());
                dataBase.getFollowing().putIfAbsent(msg.getUsername(), new Vector<>());
                dataBase.getNotificationForNonLoggIn().putIfAbsent(msg.getUsername(), new ConcurrentLinkedQueue<>());
                AckMsg ack = new AckMsg(msg.getOpcode());
                con.send(connectionId, ack);
            }
        }
    }
    public void LoginProcess(LoginMsg msg) {
        synchronized (dataBase.getLoggedInMap()) {
            if (user.getUsername() != null || dataBase.isLogged(msg.getUsername()) || !dataBase.getRegisteredClients().containsKey(msg.getUsername()) || !dataBase.getRegisteredClients().get(msg.getUsername()).equals(msg.getPassword())) {
                ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
                con.send(connectionId, errorMsg);
            } else {
                    AckMsg acklogin = new AckMsg(msg.getOpcode());
                    con.send(connectionId, acklogin);
                    user.setPassword(msg.getPassword());
                    user.setUsername(msg.getUsername());
                    dataBase.getLoggedInMap().putIfAbsent(msg.getUsername(), connectionId);
                    while (!dataBase.getNotificationForNonLoggIn().get(msg.getUsername()).isEmpty()) {
                        Message notification = dataBase.getNotificationForNonLoggIn().get(msg.getUsername()).poll();
                        con.send(connectionId, notification);
                    }

            }
        }
    }
    public void LogoutProcess(LogoutMsg msg){
            if (user.getUsername() == null) {
                ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
                con.send(connectionId, errorMsg);
            } else {
                synchronized (dataBase.getLoggedInMap().get(user.getUsername())) {
                    dataBase.getLoggedInMap().remove(user.getUsername(), connectionId);
                    AckMsg ackMsg = new AckMsg(msg.getOpcode());
                    user.setUsername(null);
                    user.setPassword(null);
                    con.send(connectionId, ackMsg);
                    con.disconnect(connectionId);
                    this.shouldTerminate = true;
                }
            }
    }

    public void FollowProcess (FollowMsg msg){
        if(user.getUsername() == null){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else{
            Vector<String> retVector = new Vector<>();
            if(msg.getFolloworUn() == true){
                for(String s : msg.getUserNameList()){
                    if(!dataBase.getFollowing().get(user.getUsername()).contains(s)&& dataBase.getRegisteredClients().containsKey(s)){
                        dataBase.getFollowing().get(user.getUsername()).addElement(s);
                        dataBase.getFollowers().get(s).addElement(user.getUsername());
                        retVector.addElement(s);
                    }
                }
            }
            else{
                for(String s : msg.getUserNameList()) {
                    if (dataBase.getFollowing().get(user.getUsername()).contains(s) && dataBase.getRegisteredClients().containsKey(s)) {
                        dataBase.getFollowing().get(user.getUsername()).remove(s);
                        dataBase.getFollowers().get(s).remove(user.getUsername());
                        retVector.addElement(s);
                    }
                }
            }
            if(retVector.isEmpty()) {
                ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
                con.send(connectionId, errorMsg);
            }
            else {
                AckFollowMessage ackFollowMessage = new AckFollowMessage(retVector,msg.getFolloworUn());
                con.send(connectionId, ackFollowMessage);
            }
        }
    }

    public void PostProcess (PostMsg msg){
        if(user.getUsername()==null){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else{
                 String[] arr = msg.getContent().split(" ");
                 Vector<String> allTaggedUser = new Vector<>();
                 for(int i = 0; i<arr.length; i++){
                     if(arr[i].charAt(0) == '@'){
                         String userName = arr[i].substring(1);
                         if(!allTaggedUser.contains(userName))
                         allTaggedUser.addElement(userName);
                     }
                 }
                 // add the post the user posted
                 dataBase.getPostPerUser().get(user.getUsername()).add(msg.getContent());
                 for(int i = 0; i<allTaggedUser.size(); i++) {
                     if (dataBase.getRegisteredClients().containsKey(allTaggedUser.get(i))) {
                         dataBase.getPmPerUser().get(allTaggedUser.get(i)).add(msg.getContent());
                         synchronized (dataBase.getLoggedInMap()) {
                         if (!dataBase.isLogged(allTaggedUser.get(i))){
                             dataBase.getNotificationForNonLoggIn().get(allTaggedUser.get(i)).add(new NotificationMsg(('1'), user.getUsername(), msg.getContent()));
                         } else {
                                 int conId = dataBase.getLoggedInMap().get(allTaggedUser.get(i));
                                 con.send(conId, new NotificationMsg(('1'), user.getUsername(), msg.getContent()));
                             }
                         }
                     }
                 }
                 for(int i = 0; i<dataBase.getFollowers().get(user.getUsername()).size(); i++){
                     if(!dataBase.isLogged(dataBase.getFollowers().get(user.getUsername()).get(i)) && !allTaggedUser.contains(dataBase.getFollowers().get(user.getUsername()).get(i))){
                         dataBase.getNotificationForNonLoggIn().get(dataBase.getFollowers().get(user.getUsername()).get(i)).add(new NotificationMsg(('1'), user.getUsername(), msg.getContent()));
                     }
                     else if(dataBase.isLogged(dataBase.getFollowers().get(user.getUsername()).get(i)) && !allTaggedUser.contains(dataBase.getFollowers().get(user.getUsername()).get(i))){
                         int conId = dataBase.getLoggedInMap().get(dataBase.getFollowers().get(user.getUsername()).get(i));
                         con.send(conId, new NotificationMsg(('1'), user.getUsername(), msg.getContent()));
                     }
                 }
                 AckMsg ackMsg = new AckMsg(msg.getOpcode());
                 con.send(connectionId, ackMsg);
        }
    }

    public void PMProcess (pmMsg msg){
        if(user.getUsername()==null || !dataBase.getRegisteredClients().containsKey(msg.getUsername())){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else{
            if(dataBase.isLogged(msg.getUsername())){
                dataBase.getPmPerUser().get(msg.getUsername()).add(msg.getContent());
                int conId = dataBase.getLoggedInMap().get(msg.getUsername());
                con.send(conId, new NotificationMsg('0', user.getUsername(), msg.getContent()));
            }
            else{
                dataBase.getPmPerUser().get(msg.getUsername()).add(msg.getContent());
                dataBase.getNotificationForNonLoggIn().get(msg.getUsername()).add(new NotificationMsg('0',user.getUsername(), msg.getContent()));
            }
            AckMsg ackMsg = new AckMsg(msg.getOpcode());
            con.send(connectionId, ackMsg);
        }
    }

    public void UserListProcess (UserListMsg msg) {
        if(user.getUsername()==null){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else{
            AckUserListMessage ackUserListMessage = new AckUserListMessage(dataBase.getUserNameList());
            con.send(connectionId, ackUserListMessage);
        }
    }

    public void StatProcess (StatMsg msg){
        if(user.getUsername()==null){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else if(!dataBase.getRegisteredClients().containsKey(msg.getUsername())){
            ErrorMsg errorMsg = new ErrorMsg(msg.getOpcode());
            con.send(connectionId, errorMsg);
        }
        else{
            short numOfPost = (short) dataBase.getPostPerUser().get(msg.getUsername()).size();
            short numOfFollowers = (short) dataBase.getFollowers().get(msg.getUsername()).size();
            short numOfFollowing = (short) dataBase.getFollowing().get(msg.getUsername()).size();
            AckStatMessage ack = new AckStatMessage(numOfPost, numOfFollowers, numOfFollowing);
            con.send(connectionId, ack);
        }
    }
}
