package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TftpConnectionHandler<T> implements ConnectionHandler<T> {

    private final TftpProtocol protocol;
    private final TftpEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public TftpConnectionHandler(Socket sock, TftpEncoderDecoder reader, TftpProtocol protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }


    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                byte[] nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void close() throws IOException {
        protocol.connected=false;
        sock.close();
    }

    public void send(T msg) {
        if (msg != null) {
            try {
                out.write(encdec.encode((byte[])msg));
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public TftpProtocol getProtocol() {
        return protocol;
    }
}
