package ru.spbau.anastasia.race;

import java.util.ArrayList;
import java.util.Iterator;

public class mLayer {

    public float frequencyOfAdding = 5;

    protected boolean isDamaged = false;
    protected ArrayList<mBasic> data = new ArrayList<>();
    protected int level;

    private int lastAdding;

    public mLayer(int lev) {
        level = lev;
        lastAdding = (int) (frequencyOfAdding - 1);
    }

    public boolean tryToAdd() {
        lastAdding = (int) ((lastAdding + 1) % frequencyOfAdding);
        return lastAdding == 0;
    }

    public synchronized void add(mBasic item) {
        if (!isDamaged) {
            data.add(item);
        }
    }

    public synchronized void updateExist() {
        Iterator<mBasic> iterator = data.iterator();
        while (iterator.hasNext()) {
            mBasic s = iterator.next();
            if (!s.exists) {
                iterator.remove();
            }
        }
    }

    public synchronized void delete(mBasic barrier) {
        if (barrier != null) {
            data.remove(barrier);
        }
    }

    public void restart() {
        isDamaged = false;
    }

    public synchronized void clear() {
        data.clear();
    }

    public synchronized void update() {
        if (isDamaged) {
            return;
        }
        for (mBasic a : data) {
            if (a != null) {
                a.update();
            }
        }
    }
}