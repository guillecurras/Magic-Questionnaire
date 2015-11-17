package com.example.bernabe.psic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class OnePlayer extends ActionBarActivity {

    ArrayList weka = new ArrayList();
    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CREATE", "Recibido-one");
        Intent intent = getIntent();
        String message = intent.getStringExtra(Quiz.EXTRA_MESSAGE);
        setContentView(R.layout.activity_one_player);
        Log.d("CREATE", "Recojido-One");
        TextView tv = (TextView) findViewById(R.id.texto_one_2);
        tv.setText(message);
        tv.setTextSize(40);
        Log.d("CREATE", "Listo-One");

    }
    public void yes(View view) {
        Log.d("CREATION", "One-YES-1");
        Intent intent = new Intent(this,OnePlayer.class);
        String message = "yes";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "One-YES");
    }
    public void no(View view) {
        Log.d("CREATION", "One-NO-1");
        Intent intent = new Intent(this,OnePlayer.class);
        String message = "no";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "One-NO");
    }
    public void maybe(View view) {
        Log.d("CREATION", "One-MAYBE-1");
        Intent intent = new Intent(this,OnePlayer.class);
        String message = "maybe";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "One-MAYBE");
    }
    public void main(View view) {
        Log.d("CREATION", "One-BACK-1");
        Intent intent4 = new Intent(this,Quiz.class);
        String message = "maybe";
        intent4.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent4);
        Log.d("CREATION", "One-BACK");
    }
}
