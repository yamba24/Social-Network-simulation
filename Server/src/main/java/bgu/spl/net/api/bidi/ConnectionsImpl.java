package bgu.spl.net.api.bidi;


import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl <T> implements Connections<T>{

    private ConcurrentHashMap <Integer, ConnectionHandler> idcmap;

    public ConnectionsImpl (){

        this.idcmap = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized boolean send(int connectionId, T msg) {
        if(idcmap.containsKey(connectionId)){
            idcmap.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        // if the map is not null, than send the message to all the client
        if(idcmap != null){
            for(int id : idcmap.keySet()){
                idcmap.get(id).send(msg);
            }
        }
    }

    @Override
    // remove active client from the map
    // note to myself - should send logout msg?
    public void disconnect(int connectionId) {
        if(idcmap.containsKey(connectionId)){
            idcmap.remove(connectionId);
        }
    }

    public ConcurrentHashMap<Integer, ConnectionHandler> getIdcmap() {
        return idcmap;
    }
}
