package ru.spbau.anastasia.race;

import java.util.ArrayList;
import java.util.Iterator;

public class mLayer {

    public int numOfTheme = 0;
    public float frequencyOfAdding = 5;

    private int lastAdding;

    protected boolean isDamaged = false;
    protected ArrayList<mBasic> data = new ArrayList<>();
    protected int level;

    public mLayer(int lev, int numOfTheme_) {
        level = lev;
        lastAdding = (int) (frequencyOfAdding - 1);
        numOfTheme = numOfTheme_;
    }

    public boolean tryToAdd() {
        lastAdding = (int)((lastAdding + 1) % frequencyOfAdding);
        return lastAdding == 0;
    }

    public synchronized void add(mBasic item) {
        if (!isDamaged) {
            data.add(item);
        }
    }

    public synchronized void updateExist(){
        Iterator<mBasic> iter = data.iterator();
        while (iter.hasNext()) {
            mBasic s = iter.next();
            if (!s.exist) {
                iter.remove();
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