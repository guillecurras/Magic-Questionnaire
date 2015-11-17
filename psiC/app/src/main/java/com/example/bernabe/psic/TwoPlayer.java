package com.example.bernabe.psic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TwoPlayer extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CREATE", "Recibido-two");
        Intent intent = getIntent();
        String message = intent.getStringExtra(Quiz.EXTRA_MESSAGE);
        setContentView(R.layout.activity_two_player);
        Log.d("CREATE", "Recojido-two");
        TextView tv = (TextView) findViewById(R.id.texto_two_2);
        tv.setText(message);
        tv.setTextSize(40);
        Log.d("CREATE", "Listo-two");

    }
    public void yes(View view) {
        Log.d("CREATION", "two-YES-1");
        Intent intent = new Intent(this,TwoPlayer.class);
        String message = "yes";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "two-YES");
    }
    public void no(View view) {
        Log.d("CREATION", "two-NO-1");
        Intent intent = new Intent(this,TwoPlayer.class);
        String message = "no";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "two-No");
    }
    public void maybe(View view) {
        Log.d("CREATION", "two-MAYBE-1");
        Intent intent = new Intent(this,TwoPlayer.class);
        String message = "maybe";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        Log.d("CREATION", "two-MAYBE");
    }
    public void main(View view) {
        Log.d("CREATION", "One-BACK-1");
        Intent intent4 = new Intent(this,OnePlayer.class);
        String message = "maybe";
        intent4.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent4);
        Log.d("CREATION", "One-BACK");
    }
}
