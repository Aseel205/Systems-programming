package bgu.spl.net.impl.tftp;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;


public class TftpServer {


    private final int port;
    private ServerSocket sock;
    private final TftpProtocol protocol;
    private final TftpEncoderDecoder encdec;
    private  ConnectionsImpl connections;
    int id ;
    public static void main(String[] args) throws UnsupportedEncodingException {

        TftpServer server = new TftpServer(7777, new TftpProtocol(), new TftpEncoderDecoder<>(), new ConnectionsImpl());
        server.serve();
    }


    public static byte intToHexByte(int number) {
        return (byte) (number & 0xFF); 
    }
    public TftpServer(int port, TftpProtocol protocol, TftpEncoderDecoder encdec , ConnectionsImpl connections) {
        this.port = port;
        this.protocol =  protocol;
        this.encdec=encdec;
        this.sock = null;
        this.connections= connections;
        this.id = 1;


    }
    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");
            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSock = serverSock.accept();
                System.out.println(" the server accepted client");
                TftpConnectionHandler handler = new TftpConnectionHandler(clientSock, new TftpEncoderDecoder(), new TftpProtocol());
                handler.getProtocol().start(id, connections);
                connections.connect(id, handler);
                id=id+1;
                execute(handler);
            }
        } catch (IOException ex) {
        }


        System.out.println("server closed!!!");
    }

    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    protected void execute(TftpConnectionHandler handler) {
        Thread t1 = new Thread(handler::run);
        t1.start();
    }

    public static class GlobalLock {
        public static final Object lock = new Object();
    }

}
