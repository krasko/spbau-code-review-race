package ru.spbau.anastasia.race.util;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class DataBaseHelper {
    private static Firebase bestScoreDataBase;
    private static long bestScore = 0;

    public static void initializeDataBase(Context context) {
        Firebase.setAndroidContext(context);
        bestScoreDataBase = new Firebase("https://radiant-inferno-5405.firebaseio.com/");

        bestScoreDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                bestScore = (long) snapshot.getValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    public static long getBestScore() {
        return bestScore;
    }

    public static void setBestScore(long score) {
        bestScoreDataBase.setValue(score);
    }
}
