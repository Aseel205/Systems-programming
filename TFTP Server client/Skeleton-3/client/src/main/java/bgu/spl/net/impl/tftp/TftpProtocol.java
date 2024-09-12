package bgu.spl.net.impl.tftp;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TftpProtocol {

    protected String RRQ_file_name;
    protected String WRQ_file_name;
    protected int pointer = 0;
    protected byte block_number1 = 1;
    protected byte[] file;
    private int upcode;
    private FilesStock filesStock;
    private short ACK_number = 1;
    private BufferedOutputStream out;
    protected volatile boolean listening = false;
    protected final Object lock;
    protected boolean WRQ_boolean = false;

    public TftpProtocol(BufferedOutputStream out) {
        this.upcode = 0;
        this.filesStock = TftpClient.getFilesStock();
        this.out = out;
        this.lock = new Object();
    }

    public void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();

        for (byte b : byteArray)
            if (b == 0) {
                System.out.println(sb.toString());
                sb.setLength(0);
            } else
                sb.append(new String(new byte[] { b }, StandardCharsets.UTF_8));

        if (sb.length() > 0)
            System.out.println(sb.toString());

        synchronized (lock) {
            lock.notifyAll();
        }

    }

    public void process(byte[] message) throws IOException {
        if (message == null)
            throw new RuntimeException("the message is null");

        upcode = (((int) message[0]) << 8 | (int) (message[1]));

        /////////////////////////////////////////////////

        if (upcode == 3) { // data packet

            byte[] convert = new byte[2];
            convert[0] = message[2];
            convert[1] = message[3];
            byte[] data = new byte[TwoBytesToint(convert)];
            for (int i = 0; i < data.length; i++) {
                data[i] = message[i + 6];
            }
            if (RRQ_file_name != null) {
                filesStock.addDataToFile(RRQ_file_name, data);
                ACK();
                if (data.length < 512) {
                    System.out.println("RRQ " + RRQ_file_name + " completed ;)");
                    RRQ_file_name = null;
                    ACK_number = 1;

                    synchronized (lock) {
                        lock.notifyAll();
                    }

                }
            } else { //// DIRQ
                printByteArray(data);
            }
        }

        ////////////////////////////////////////////////

        if (upcode == 4) { // ack packet
            byte[] convert = new byte[2];
            convert[0] = message[2];
            convert[1] = message[3];
            int pr = TwoBytesToint(convert);
            System.out.println("ACK " + pr);
            if (!WRQ_boolean) {
                if (pr != 0) {
                    System.out.println("WRQ " + WRQ_file_name + " complete ;)");
                    WRQ_file_name = null;
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            if (WRQ_file_name != null) {
                if (pointer == file.length) { // we have to return an empty packet
                    byte[] output = new byte[6];
                    output[0] = 0;
                    output[1] = 3;
                    output[2] = 0;
                    output[3] = (byte) 0;
                    output[4] = (shortToTwoBytes(block_number1))[0];
                    output[5] = (shortToTwoBytes(block_number1))[1];
                    block_number1 = 1;
                    file = null;
                    pointer = 0;
                    WRQ_boolean = false;
                    out.write(output);
                    out.flush();

                } else if (file.length < pointer + 512) { ////// if the file has less than 512 bytes
                    byte[] output = new byte[(file.length - pointer) + 6]; 
                    for (int i = 0; i < file.length - pointer; i++)
                        output[i + 6] = file[pointer + i];
                    output[0] = 0;
                    output[1] = 3;
                    output[2] = (shortToTwoBytes((short) (file.length - pointer)))[0];
                    output[3] = (shortToTwoBytes((short) (file.length - pointer)))[1];
                    output[4] = (shortToTwoBytes(block_number1))[0];
                    output[5] = (shortToTwoBytes(block_number1))[1];
                    block_number1 = 1;
                    file = null;
                    pointer = 0;
                    WRQ_boolean = false;
                    out.write(output);
                    out.flush();

                } else {
                    byte[] output = new byte[518];
                    for (int i = 0; i < 512; i++)
                        output[i + 6] = file[pointer + i];

                    output[0] = 0;
                    output[1] = 3;
                    output[2] = (shortToTwoBytes((short) (512)))[0];
                    output[3] = (shortToTwoBytes((short) (512)))[1];
                    output[4] = (shortToTwoBytes(block_number1))[0];
                    output[5] = (shortToTwoBytes(block_number1))[1];
                    block_number1++;

                    pointer = pointer + 512;
                    out.write(output);
                    out.flush();

                }

            }
        }

        ///////////////////////////////////////////////////

        if (upcode == 5) {
            if (message[3] == 0)
                ERROR(0);

            if (message[3] == 1) {
                ERROR(1);
                filesStock.deleteFile(RRQ_file_name); 
                RRQ_file_name = null;
            }
            if (message[3] == 2)
                ERROR(2);

            if (message[3] == 3)
                ERROR(3);

            if (message[3] == 4)
                ERROR(4);

            if (message[3] == 5) {
                ERROR(5);
                WRQ_file_name = null;
            }
            
            if (message[3] == 6)
                ERROR(6);

            if (message[3] == 7)
                ERROR(7);

        }

        if (upcode == 9) { // BCAST
            if (message.length > 3) {
                String str = new String(message, 3, message.length - 3, StandardCharsets.UTF_8);
                if (message[2] == 0)
                    System.out.println("BCAST: " + str + " was deleted :(");
                else
                    System.out.println("BCAST: " + str + " was added ;)");
                synchronized (lock) {
                    lock.notifyAll();
                }

            }
        }
    }

    public void ERROR(int error_num) {
        String errorMsg = "";
        if (error_num == 0)
            errorMsg = "ERROR: Not defined";
        if (error_num == 1)
            errorMsg = "ERROR: File not found";
        if (error_num == 2)
            errorMsg = "ERROR: Access violation";
        if (error_num == 3)
            errorMsg = "ERROR: Disk full or allocation exceeded";
        if (error_num == 4)
            errorMsg = "ERROR: Illegal TFTP operation";
        if (error_num == 5)
            errorMsg = "ERROR: File already exists";
        if (error_num == 6)
            errorMsg = "ERROR: User not logged in";
        if (error_num == 7)
            errorMsg = "ERROR: User already logged in";
        System.out.println(errorMsg);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void ACK() {
        byte[] ans = new byte[4];
        ans[0] = 0;
        ans[1] = 4;
        ans[2] = (shortToTwoBytes(ACK_number))[0];
        ans[3] = (shortToTwoBytes(ACK_number))[1];
        ACK_number++;
        try {
            out.write(ans);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int TwoBytesToint(byte[] bytes) {
        return (int) (((int) bytes[0]) << 8 | (int) (bytes[1]));
    }

    public byte[] shortToTwoBytes(short num) {
        return new byte[] { (byte) (num >> 8), (byte) (num & 0xff) };
    }

}