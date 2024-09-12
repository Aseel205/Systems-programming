package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TftpClient {

    private static FilesStock filesStock = new FilesStock();
    private TftpEncoderDecoed encdec;
    private TftpProtocol protocol;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public static FilesStock getFilesStock() {
        return filesStock;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        TftpClient client = new TftpClient();
        String serverAddress = "127.0.0.1";
        Thread keyboard_thread = new Thread(() -> {
            try {
                client.start(serverAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });
        keyboard_thread.start();

    }

    public void start(String args) throws IOException, InterruptedException {

        Socket sock = new Socket(args, 7777);
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
        encdec = new TftpEncoderDecoed();
        protocol = new TftpProtocol(out);
        Thread listeningThread = new Thread(this::read_from_server);
        listeningThread.start();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Enter your command:");
            String input = scanner.nextLine();
            if (input.matches("LOGRQ .+")) {
                String[] tokens = input.split("\\s+", 2);
                String username = tokens[1];
                LOGRQ(username);
            } else if (input.matches("DELRQ .+")) {
                String[] tokens = input.split("\\s+", 2);
                String filename = tokens[1];
                DELRQ(filename);
            } else if (input.matches("RRQ .+")) {
                String[] tokens = input.split("\\s+", 2);
                String filename = tokens[1];
                RRQ(filename);
            } else if (input.matches("WRQ .+")) {
                String[] tokens = input.split("\\s+", 2);
                String filename = tokens[1];
                WRQ(filename);
            } else if (input.equals("DIRQ")) {
                DIRQ();
            } else if (input.equals("DISC")) {
                DISC();
                running = false; // Exit the loop
            } else {
                System.out.println("Invalid input. Please enter a valid command.");
            }

        }
        scanner.close();
    }

    public void LOGRQ(String username) throws IOException {
        byte[] output = username.getBytes();
        this.encdec.upcode = 7;
        byte[] msg = encdec.encode(output);
        out.write(msg);
        out.flush();
        _wait();
    }

    public void DELRQ(String filename) throws IOException {
        byte[] output = filename.getBytes();
        this.encdec.upcode = 8;
        byte[] msg = encdec.encode(output);
        out.write(msg);
        out.flush();
        _wait();

    }

    public void RRQ(String filename) throws IOException {
        if (filesStock.contains(filename))
            System.out.println(" The File Already Exists!!");
        else {
            filesStock.createFile(filename);
            byte[] output = filename.getBytes();
            this.encdec.upcode = 1;
            byte[] msg = encdec.encode(output);
            protocol.RRQ_file_name = filename;
            out.write(msg);
            out.flush();
            _wait();

        }
    }

    public void WRQ(String filename) throws IOException {
        if (!filesStock.contains(filename))
            System.out.println(" The File Doesn't Exist!!");
        else {
            byte[] output = filesStock.getFileData(filename);
            this.encdec.upcode = 2;
            byte[] msg = encdec.encode(filename.getBytes(StandardCharsets.UTF_8));
            protocol.file = output;
            protocol.WRQ_file_name = filename;
            protocol.WRQ_boolean = true;
            protocol.pointer = 0;
            out.write(msg);
            out.flush();
            _wait();
        }
    }

    public void DIRQ() throws IOException {
        this.encdec.upcode = 6;
        byte[] msg = encdec.encode(new byte[0]);
        out.write(msg);
        out.flush();
        _wait();

    }

    public void DISC() throws IOException {
        this.encdec.upcode = 10;
        byte[] msg = encdec.encode(new byte[0]);
        out.write(msg);
        out.flush();

    }

    public void read_from_server() {

        int read = 0;

        while (true) {
            try {
                if (!((read = in.read()) >= 0))
                    break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] nextMessage = encdec.decodeNextByte((byte) read);
            if (nextMessage != null) {
                try {
                    protocol.process(nextMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    public void _wait() {
        synchronized (protocol.lock) {
            try {
                protocol.lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
