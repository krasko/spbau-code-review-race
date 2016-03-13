package ru.spbau.anastasia.race.util;

import java.nio.ByteBuffer;

public class FileForSent {

    public final boolean isDied;
    public final int numOfBarrier;

    private final float x, y;
    private final boolean isJumping;

    public FileForSent(float x, float y, boolean isJumping, boolean isDied, int numOfBarrier) {
        this.x = x;
        this.y = y;
        this.isJumping = isJumping;
        this.isDied = isDied;
        this.numOfBarrier = numOfBarrier;
    }

    public FileForSent(byte[] bytes, int count) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, count);
        buffer.get();
        x = buffer.getFloat();
        y = buffer.getFloat();
        isJumping = buffer.get() != 0;
        isDied = buffer.get() != 0;
        numOfBarrier = buffer.getInt();
    }

    public byte[] toMsg() {
        return ByteBuffer.allocate(15).put((byte) 0).putFloat(x).putFloat(y).put((byte) (isJumping ? 1 : 0)).put((byte) (isDied ? 1 : 0)).putInt(numOfBarrier).array();
    }

    public boolean getIsJumping() {
        return isJumping;
    }

    public float getX() {
        return x * mSettings.CurrentXRes;
    }

    public float getY() {
        return y * mSettings.CurrentYRes;
    }
}
