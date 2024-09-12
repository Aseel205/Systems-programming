package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.LinkedList;

public class TftpEncoderDecoder<T> implements MessageEncoderDecoder<byte[]> {

    private LinkedList<Byte> bytes;
    private int opcode;
    private int data_length = -10;

    public TftpEncoderDecoder() {
        bytes = new LinkedList<>();
        this.opcode = -1;
    }

    @Override
    public byte[] decodeNextByte(byte nextByte) {

        if (bytes.size() < 2) {
            bytes.add(nextByte);
            if (bytes.size() == 2)
                opcode = nextByte;
            if (opcode == 6 || opcode == 10) {
                byte[] ans = new byte[2];
                ans[1] = (byte) opcode;

                reset();
                return ans;

            }
            return null;
        }
        if (opcode == 1 || opcode == 2 || opcode == 7 || opcode == 8) {
            if (!(nextByte == 0))
                bytes.add(nextByte);

            else {
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i++;
                }
                reset();
                return ans;
            }
        }

        if (opcode == 4) {
            bytes.add(nextByte);
            if (bytes.size() == 4) { // ACK backet
                byte[] ans = new byte[bytes.size()];
                int i = 0;
                for (byte b : bytes) {
                    ans[i] = b;
                    i++;
                }
                reset();
                return ans;
            }
        }

        if (opcode == 3) {
            bytes.add(nextByte);
            if (bytes.size() == 4) {
                byte[] covert = new byte[2];
                covert[0] = bytes.get(2);
                covert[1] = bytes.get(3);
                data_length = TwoBytesToShort(covert);
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

    public byte[] encode(byte[] message) {
        return message;
    }

    public void reset() {
        this.opcode = -1;
        this.data_length = -10;
        this.bytes = new LinkedList<>();
    }

    public short TwoBytesToShort(byte[] bytes) {
        return (short) (((short) bytes[0]) << 8 | (short) (bytes[1]));
    }

    public byte[] shortToTwoBytes(short num) {
        return new byte[] { (byte) (num >> 8), (byte) (num & 0xff) };
    }
}
