package com.example.bernabe.psic;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class OnePlayer extends ActionBarActivity {

    ArrayList weka = new ArrayList();
    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";
    String siguientePregunta = "";
    File arbol = null;
    static Parser parser = null;

    @TargetApi(Build.VERSION_CODES.KITKAT)
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


        arbol = obtenerArbol();

        if (parser == null)
            parser = new Parser(arbol);


    }


//        try {
//            new WekaTextfileToXMLTextfile(getAssets().open("treeInText.txt"), getAssets().open("treeInXML.xml"));
//        }
//        catch (Exception e)
//        {
//
//        }

    public void handlerBotones(View view)
    {
        Button boton = (Button) view;
        Intent intent = new Intent(this,OnePlayer.class);
        siguientePregunta = parser.getSiguientePregunta(boton.getText().toString());
        intent.putExtra(EXTRA_MESSAGE, siguientePregunta);
        startActivity(intent);
    }


    public File obtenerArbol()  // De momento cargo un árbol que hay en la carpeta Assets.
                                // Habrá que cambiar este método para que llame a weka y calcule el árbol.
    {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;

        File treeTextFile = null;
        try {
            in = assetManager.open("weatherBonito.txt");
            treeTextFile = new File(getFilesDir(), "tree.txt");
            out = new FileOutputStream(treeTextFile);
            copyFile(in, out);
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + "tree.txt", e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }

        File treeXMLFile = new File(getFilesDir(), "tree.xml");

        new WekaToXML(treeTextFile, treeXMLFile, true, false).writeXmlFromWekaText();

//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(treeXMLFile), "UTF-8"))) {
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                Log.d("XML", line);
//            }
//        }
//        catch (Exception e) {
//        }

        return treeXMLFile;

    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}