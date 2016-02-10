package ru.spbau.anastasia.race;

import java.nio.ByteBuffer;
import java.util.Random;

import ru.spbau.anastasia.race.util.SystemUiHider;

public class FileForSent {

    private float dx, dy;
    private boolean isJumping;

    public FileForSent(float dx, float dy, boolean isJumping_) {
        this.dx = dx;
        this.dy = dy;
        isJumping = isJumping_;
    }

    public FileForSent(byte[] bytes) {
        byte[] dxBytes = new byte[ConnectionGame.LENGTH_OF_RECEIVED_BLOCK];
        System.arraycopy(bytes, 0, dxBytes, 0, ConnectionGame.LENGTH_OF_RECEIVED_BLOCK);
        byte[] dyBytes = new byte[ConnectionGame.LENGTH_OF_RECEIVED_BLOCK];
        System.arraycopy(bytes, 0, dyBytes, 4, ConnectionGame.LENGTH_OF_RECEIVED_BLOCK);

        dx = ByteBuffer.wrap(dxBytes).getFloat();
        dy = ByteBuffer.wrap(dyBytes).getFloat();

        isJumping = bytes[8] != 0;
    }

    public byte[] toMsg() {

        byte[] bytes = new byte[9];

        byte[] bytesDX = ByteBuffer.allocate(4).putFloat(dx).array();
        System.arraycopy(bytesDX, 0, bytes, 0, ConnectionGame.LENGTH_OF_RECEIVED_BLOCK);

        byte[] bytesDY = ByteBuffer.allocate(4).putFloat(dy).array();
        System.arraycopy(bytesDY, 0, bytes, 4, ConnectionGame.LENGTH_OF_RECEIVED_BLOCK);

        bytes[8] = (byte) (isJumping ? 1 : 0);
        return bytes;
    }

    public static FileForSent genServer() {
        return new FileForSent(0, 0, false);
    }

    public float getDX() {
        return dx;
    }

    public boolean getIsJumping() {
        return isJumping;
    }

    public float getDY() {
        return dy;
    }
}
