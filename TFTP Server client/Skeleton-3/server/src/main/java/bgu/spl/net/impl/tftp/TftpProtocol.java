package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import bgu.spl.net.api.MessagingProtocol;

public class TftpProtocol implements MessagingProtocol<String> {
    private boolean shouldTerminate;
    int connectionId;
    private ConnectionsImpl connections;

    private String clientMSG;

    private int upcode;

    private FilesStock filesStock;

    protected volatile boolean connected;

    private String client_name;

    private byte[] file;
    private int pointer = 0;
    private byte block_number1 = 1;

    private byte block_number2 = 1;

    private String uploaded_file_name;
    private int id;
    private String downloaded_file_name;

    public void start(int connectionId, ConnectionsImpl connections) {
        this.connections = connections;
        this.connectionId = connectionId;
        this.shouldTerminate = false;
        this.clientMSG = "";
        this.filesStock = new FilesStock();
        connected = false;
        file = null;

    }

    public void process(byte[] message) {

        if (message == null)
            throw new RuntimeException("the message is null");

        upcode = (((int) message[0]) << 8 | (int) (message[1]));

        if (upcode == 1 || upcode == 2 || upcode == 7 || upcode == 8) {
            byte[] client_string = new byte[message.length - 2];
            for (int i = 2; i < message.length; i++)
                client_string[i - 2] = message[i];
            clientMSG = new String(client_string, StandardCharsets.UTF_8);
        }

        //////////////////////////////////////// //LOGIN
        if (upcode == 7) {// old client want to log in
            byte[] msg;
            if (connected) {
                msg = ERROR(7); //
                connections.send(connectionId, msg);
            } else {
                if (connections.name_hash.containsKey(clientMSG)) {
                    msg = ERROR(7); // a new user tries to log in with a logged in name
                    connections.send(connectionId, msg);
                } else {
                    client_name = clientMSG; /// successful log-in
                    connections.name_hash.put(client_name, true);
                    msg = ACK(0);
                    connections.send(connectionId, msg);
                    connected = true;
                }
            }
        }
        ////////////////////////////////////////// // DELETE FILE
        if (upcode == 8) {
            if (!connected)
                connections.send(connectionId, ERROR(6));
            else {
                byte[] msg;
                if (filesStock.contains(clientMSG)) {
                    filesStock.deleteFile(clientMSG);
                    msg = ACK(0);
                    connections.send(connectionId, msg);
                    connections.sendAll(BCAST(0, clientMSG));
                } else {
                    msg = ERROR(1);
                    connections.send(connectionId, msg);
                }
            }
        }
        /////////////////////////////////////////////////////// disconnect

        if (upcode == 10) {
            if (!connected)
                connections.send(connectionId, ERROR(6));
            else {
                byte[] msg = ACK(0);
                connections.send(connectionId, msg);
                connections.disconnect(connectionId);
                connections.name_hash.remove(client_name);
                shouldTerminate = true;
            }
        }
        /////////////////////////////////////////////// RRQ - upload to client

        if (upcode == 1) {
            if (!connected)
                connections.send(connectionId, ERROR(6));
            else {
                uploaded_file_name = clientMSG;
                if (!filesStock.contains(uploaded_file_name)) {
                    connections.send(connectionId, ERROR(1));
                } else {
                    file = filesStock.getFileData(uploaded_file_name);
                    pointer = 0;
                    upload_step();
                }
            }
        }

        ///////////////////////////////////////////////// ACK
        if (upcode == 4) {
            if (!connected)
                connections.send(connectionId, ERROR(7));
            else {
                byte[] convert = new byte[2];
                convert[0] = message[2];
                convert[1] = message[3];
                System.out.println("ack:" + TwoBytesToShort(convert));
                upload_step();
            }
        }

        ////////////////////////////////////////// DIRQ
        if (upcode == 6) {
            if (!connected)
                connections.send(connectionId, ERROR(6));
            else {
                byte[] files_name = filesStock.getFilesNames();
                byte[] ans = new byte[files_name.length + 6];
                ans[0] = 0;
                ans[1] = 3;
                ans[2] = (shortToTwoBytes((short) files_name.length))[0];
                ans[3] = (shortToTwoBytes((short) files_name.length))[1];
                ans[4] = 0;
                ans[5] = 1;
                for (int i = 0; i < files_name.length; i++)
                    ans[i + 6] = files_name[i];

                connections.send(connectionId, ans);
            }
        }
        ////////////////////////////// WRQ - download
        if (upcode == 2) {
            if (!connected)
                connections.send(connectionId, ERROR(7));
            else {
                byte[] file_bytes = new byte[message.length - 2]; 
                for (int i = 0; i < file_bytes.length; i++)
                    file_bytes[i] = message[i + 2];
                downloaded_file_name = new String(file_bytes, StandardCharsets.UTF_8);

                if (filesStock.contains(downloaded_file_name))
                    connections.send(connectionId, ERROR(5));
                else {
                    filesStock.createFile(downloaded_file_name);
                    connections.send(connectionId, ACK(0));
                }
            }
        }
        ////////////////////////////////////// data
        if (upcode == 3) {
            if (!connected)
                connections.send(connectionId, ERROR(7));

            else {
                download_step(message);
                if (message.length < 512) {
                    connections.sendAll(BCAST(1, downloaded_file_name));
                    downloaded_file_name = null;
                }
            }
        }
        ///////////////////////////////////////// invalid upcode
        if (upcode > 10 || upcode < 1)
            connections.send(connectionId, ERROR(4));
    }

    @Override
    public String process(String msg) {
        return null;
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public byte[] ERROR(int error_num) {
        String errorMsg = "";
        if (error_num == 0)
            errorMsg = "Not defined";
        if (error_num == 1)
            errorMsg = "File not found";
        if (error_num == 2)
            errorMsg = "Access violation";
        if (error_num == 3)
            errorMsg = "Disk full or allocation exceeded";
        if (error_num == 4)
            errorMsg = "Illegal TFTP operation";
        if (error_num == 5)
            errorMsg = "File already exists";
        if (error_num == 6)
            errorMsg = "User not logged in";
        if (error_num == 7)
            errorMsg = "User already logged in";

        byte[] ans = errorMsg.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[ans.length + 5];
        output[0] = 0;
        output[1] = 5;
        output[2] = 0;
        output[3] = (byte) error_num;
        for (int i = 0; i < ans.length; i++)
            output[i + 4] = ans[i];

        output[output.length - 1] = 0;
        return output;
    }

    public byte[] ACK(int blockNumber) {
        byte[] ans = new byte[4];
        ans[0] = 0;
        ans[1] = 4;
        ans[2] = (shortToTwoBytes((short) blockNumber))[0];
        ans[3] = (shortToTwoBytes((short) blockNumber))[1];

        return ans;
    }

    public byte[] BCAST(int del_Add, String file_name) {
        byte[] ans = file_name.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[ans.length + 4];
        output[0] = 0;
        output[1] = 9;
        output[2] = (byte) del_Add;
        for (int i = 0; i < ans.length; i++)
            output[i + 3] = ans[i];
        output[output.length - 1] = 0;

        return output;
    }

    public void upload_step() {

        if (file != null) {
            if (pointer == file.length) { // we have to return an empty packet
                byte[] messege = new byte[6];
                messege[0] = 0;
                messege[1] = 3;
                messege[2] = 0;
                messege[3] = (byte) 0;
                messege[4] = (shortToTwoBytes((short) block_number1))[0];
                messege[5] = (shortToTwoBytes((short) block_number1))[1];

                block_number1 = 0;
                file = null;
                pointer = 0;
                connections.send(connectionId, messege);

            } else if (file.length < pointer + 512) { ////// if the file has less than 512 bytes
                byte[] messege = new byte[(file.length - pointer) + 6];
                for (int i = 0; i < file.length - pointer; i++)
                    messege[i + 6] = file[pointer + i];
                messege[0] = 0;
                messege[1] = 3;
                messege[2] = (shortToTwoBytes((short) (file.length - pointer)))[0];
                messege[3] = (shortToTwoBytes((short) (file.length - pointer)))[1];
                messege[4] = (shortToTwoBytes(block_number1))[0];
                messege[5] = (shortToTwoBytes(block_number1))[1];

                block_number1 = 0;
                file = null;
                pointer = 0;
                connections.send(connectionId, messege);
            } else {
                byte[] messege = new byte[518];
                for (int i = 0; i < 512; i++)
                    messege[i + 6] = file[pointer + i];

                messege[0] = 0;
                messege[1] = 3;
                messege[2] = (shortToTwoBytes((short) 512))[0];
                messege[3] = (shortToTwoBytes((short) 512))[1];
                messege[4] = (shortToTwoBytes(block_number1))[0];
                messege[5] = (shortToTwoBytes(block_number1))[1];

                block_number1++;
                pointer = pointer + 512;
                connections.send(connectionId, messege);

            }

        }
    }

    public void download_step(byte[] data) {
        byte ans[] = new byte[data.length - 6];
        for (int i = 0; i < ans.length; i++)
            ans[i] = data[i + 6];

        filesStock.addDataToFile(downloaded_file_name, ans);

        connections.send(connectionId, ACK(block_number2));
        block_number2++;
        if (data.length < 518)
            block_number2 = 1;

    }

    public byte[] shortToTwoBytes(short num) {
        short a = num;
        byte[] a_bytes = new byte[] { (byte) (a >> 8), (byte) (a & 0xff) };
        return a_bytes;
    }

    public short TwoBytesToShort(byte[] bytes) {
        return (short) (((short) bytes[0]) << 8 | (short) (bytes[1]));
    }
}