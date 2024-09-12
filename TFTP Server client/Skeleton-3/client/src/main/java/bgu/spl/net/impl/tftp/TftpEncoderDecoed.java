package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.util.LinkedList;

public class TftpEncoderDecoed implements MessageEncoderDecoder<byte[]> {
    private LinkedList<Byte> bytes = new LinkedList<>();
    private int opcode;
    private int data_length = -10;
    protected byte upcode;
    protected byte blockNumber = 0;

    public byte[] decodeNextByte(byte nextByte) {
        if (bytes.size() < 2) {
            bytes.add(nextByte);
            if (bytes.size() == 2)
                opcode = nextByte;
            return null;
        } else if (opcode == 9) { // Bcast without 0
            if (nextByte == 0 && bytes.size() > 3) {
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i++;
                }
                reset();
                return ans;
            } else
                bytes.add(nextByte);
        } else if (opcode == 5) { // here the error packet we sent the whole packet
            if (bytes.size() > 4 && nextByte == 0) {
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i++;
                }
                reset();
                return ans;

            } else
                bytes.add(nextByte);

        } else if (opcode == 4) { // ACK backet
            bytes.add(nextByte);
            if (bytes.size() == 4) {
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i++;
                }
                reset();
                return ans;
            }
        } else if (opcode == 3) { // data packet
            bytes.add(nextByte);
            if (bytes.size() == 4) {
                byte[] convert = new byte[2];
                convert[0] = bytes.get(2);
                convert[1] = bytes.get(3);
                data_length = TwoBytesToShort(convert);
            } else if (bytes.size() == data_length + 6) {
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i = i + 1;
                }
                reset();
                return ans;
            }
        }

        return null;

    }

    @Override
    public byte[] encode(byte[] message) { // the opcode is in the begging

        if (upcode == 1) { // RRQ
            byte[] output = new byte[message.length + 3];
            output[0] = 0;
            output[1] = upcode;
            for (int i = 0; i < message.length; i++)
                output[i + 2] = message[i];
            output[output.length - 1] = 0;
            return output;
        }

        if (upcode == 2) { // WRQ
            byte[] output = new byte[message.length + 3];
            output[0] = 0;
            output[1] = upcode;
            for (int i = 0; i < message.length; i++)
                output[i + 2] = message[i];
            output[output.length - 1] = 0;
            return output;

        }

        if (upcode == 3) { // DATA
            byte[] output = new byte[message.length + 6];
            output[0] = 0;
            output[1] = upcode;
            output[2] = (shortToTwoBytes((short) message.length))[0];
            output[3] = (shortToTwoBytes((short) message.length))[1];
            output[4] = (shortToTwoBytes(blockNumber))[0];
            output[5] = (shortToTwoBytes(blockNumber))[1];
            for (int i = 0; i < message.length; i++)
                output[i + 6] = message[i];
            return output;

        }
        if (upcode == 4) { // ACK
            byte[] output = new byte[4];
            output[0] = 0;
            output[1] = upcode;
            output[2] = (shortToTwoBytes(blockNumber))[0];
            output[3] = (shortToTwoBytes(blockNumber))[1];
            return output;
        }

        if (upcode == 6) { // DIRQ
            byte[] output = new byte[2];
            output[0] = 0;
            output[1] = 6;
            return output;

        }
        if (upcode == 7) { // LOGRQ
            byte[] output = new byte[message.length + 3];
            output[0] = 0;
            output[1] = upcode;
            for (int i = 0; i < message.length; i++)
                output[i + 2] = message[i];
            output[output.length - 1] = 0;
            return output;

        }
        if (upcode == 8) { // DELRQ
            byte[] output = new byte[message.length + 3];
            output[0] = 0;
            output[1] = upcode;
            for (int i = 0; i < message.length; i++)
                output[i + 2] = message[i];
            output[output.length - 1] = 0;
            return output;

        }

        if (upcode == 10) { // DISC
            byte[] output = new byte[2];
            output[0] = (shortToTwoBytes((short) 10))[0];
            output[1] = (shortToTwoBytes((short) 10))[1];
            return output;
        }
        return null;
    }

    public void reset() {
        this.opcode = -1;
        this.data_length = -10;
        this.bytes = new LinkedList<>();
        blockNumber = 0;
        upcode = -1;
    }

    public short TwoBytesToShort(byte[] bytes) {
        return (short) (((short) bytes[0]) << 8 | (short) (bytes[1]));
    }

    public byte[] shortToTwoBytes(short num) {
        return new byte[] { (byte) (num >> 8), (byte) (num & 0xff) };
    }
}