package ru.spbau.anastasia.race.Other;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class DataBaseHelper {
    public static Firebase bestScoreDataBase;
    public static long bestScore = 0;

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

    public static void setNewBestScore(long score) {
        bestScoreDataBase.setValue(score);
    }
}
