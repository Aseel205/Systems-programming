package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.util.Hashtable;

public class ConnectionsImpl<T> implements Connections<T> {

    private Hashtable<Integer, ConnectionHandler<T>> id_hash;
    public Hashtable<String, Boolean> name_hash;

    public ConnectionsImpl() {
        this.id_hash = new Hashtable<>();
        this.name_hash = new Hashtable<>();

    }

    public void connect(int connectionId, ConnectionHandler<T> handler) {
        id_hash.put(connectionId, handler);

    }

    public synchronized boolean send(int connectionId, T msg) {
        if (id_hash.containsKey(connectionId)) {
            TftpConnectionHandler<T> handler = (TftpConnectionHandler<T>) id_hash.get(connectionId);
            handler.send(msg);

            return true;
        }
        return false;

    }

    public synchronized void sendAll(T msg) {
        for (Integer key : id_hash.keySet()) {
            if (((TftpConnectionHandler<T>) id_hash.get(key)).getProtocol().connected)
                send(key, msg);
        }
    }

    public synchronized void disconnect(int connectionId) {
        try {
            (id_hash.get(connectionId)).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        id_hash.remove(connectionId);

    }
}
