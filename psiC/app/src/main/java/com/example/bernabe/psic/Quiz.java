package com.example.bernabe.psic;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class Quiz extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Log.d("CREATION", "On create");
    }
    public void onePlayer(View view) {
        Log.d("CREATION", "Start one");
        Intent intent = new Intent(this,OnePlayer.class);
        String message = "Primera pregunta";
        intent.putExtra(EXTRA_MESSAGE, message);
        finish();
        startActivity(intent);
        Log.d("CREATION", "Start One - 1");
    }

    public void twoPlayer(View view) {
        Log.d("CREATION", "Start two");
        Intent intent2 = new Intent(this,TwoPlayer.class);
        String message = "Segunda pregunta";
        intent2.putExtra(EXTRA_MESSAGE, message);
        Log.d("CREATION", "Start two - 2");
        startActivity(intent2);
        Log.d("CREATION", "Start two - 1");
    }

    public void help(View view) {
        Log.d("CREATION", "Help");
        Intent intent3 = new Intent(this,Help.class);
        String message = "Help";
        intent3.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent3);
        Log.d("CREATION", "Help -1");
    }
}
