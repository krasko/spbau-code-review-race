package ru.spbau.anastasia.race;

import java.nio.ByteBuffer;
import java.util.Random;

public class FileForSent {

    public static final Random RND = new Random();
    private float dx, dy;
    private boolean isJumping;

    public FileForSent(float dx_, float dy_, boolean isJumping_) {
        dx = dx_;
        dy = dy_;
        isJumping = isJumping_;
    }
    public FileForSent(byte [] bytes) {
        byte[] dxBytes = {bytes[0], bytes[1], bytes[2], bytes[3]};
        byte[] dyBytes = {bytes[4], bytes[5], bytes[6], bytes[7]};

        dx = ByteBuffer.wrap(dxBytes).getFloat();
        dy = ByteBuffer.wrap(dyBytes).getFloat();

        isJumping = bytes[8] != 0;
    }

    public byte [] toMsg() {

        byte[] bytes = new byte[9];

        byte[] bytesDX = ByteBuffer.allocate(4).putFloat(dx).array();

        bytes[0] = bytesDX[0];
        bytes[1] = bytesDX[1];
        bytes[2] = bytesDX[2];
        bytes[3] = bytesDX[3];

        byte[] bytesDY = ByteBuffer.allocate(4).putFloat(dy).array();

        bytes[4] = bytesDY[0];
        bytes[5] = bytesDY[1];
        bytes[6] = bytesDY[2];
        bytes[7] = bytesDY[3];

        bytes[8] = (byte) (isJumping ? 1 : 0);

        return bytes;
    }

    public static FileForSent genServer() {
        return new FileForSent(0, 0, false);
    }

    public float getDX(){
        return dx;
    }

    public boolean getIsJumping(){
        return isJumping;
    }

    public float getDY(){
        return dy;
    }
}
