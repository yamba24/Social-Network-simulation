package bgu.spl.net.api.bidi;

import bgu.spl.net.impl.messages.Message;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataBase {

    private ConcurrentHashMap<String, Integer> loggedInMap = new ConcurrentHashMap<>(); // hold username and connection id
    private ConcurrentHashMap<String, String> registeredClients = new ConcurrentHashMap<>(); // hold username and password
    private ConcurrentHashMap<String, Vector<String>> followers = new ConcurrentHashMap<>(); // hold the username and the user who followes him
    private ConcurrentHashMap<String, Vector<String>> following = new  ConcurrentHashMap<>(); // hold username and the user he follow after
    private ConcurrentHashMap <String, ConcurrentLinkedQueue<String>> postPerUser = new  ConcurrentHashMap<>(); // usernames and the post they posted
    private ConcurrentHashMap <String,  ConcurrentLinkedQueue<String>> pmPerUser = new  ConcurrentHashMap<>(); // username and the message they get
    private ConcurrentHashMap <String, ConcurrentLinkedQueue<Message>> notificationForNonLoggIn = new  ConcurrentHashMap<>();
    private Vector<String> userNameList = new Vector<>();

    public DataBase(){
        for(String s: loggedInMap.keySet()){
            userNameList.addElement(s);
        }
    }

    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> getNotificationForNonLoggIn() {
        return notificationForNonLoggIn;
    }

    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getPostPerUser() {
        return postPerUser;
    }

    public  ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getPmPerUser() {
        return pmPerUser;
    }

    public  ConcurrentHashMap<String, Integer> getLoggedInMap() {

        return loggedInMap;
    }

    public  ConcurrentHashMap<String, String> getRegisteredClients() {
        return registeredClients;
    }

    public  ConcurrentHashMap <String, Vector<String>> getFollowers(){

        return this.followers;
    }

    public  ConcurrentHashMap<String, Vector<String>> getFollowing() {

        return following;
    }

    public Vector<String> getUserNameList() {

        return userNameList;
    }

    public boolean isLogged(String username){
        if(loggedInMap.containsKey(username)){
            return true;
        }
        else return false;
    }

}
