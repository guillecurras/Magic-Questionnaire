package com.example.bernabe.psic;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.openDatabase;

public class OnePlayer extends ActionBarActivity {

    /**
     * Database path
     */
    private static final String DATABASE_PATH = "/data/data/com.example.bernabe.psic/databases/MAQ";

    /**
     * @variable hQuestion All question cache
     */
    private static Hashtable <String, Integer> hQuestion;

    /**
     * @variable hItem All item cache
     */
    private static Hashtable <String, Integer> hItem;

    /**
     * @variable roundNumber The round number of the actual game
     */
    private int roundNumber;

    /**
     * @variable hAnswer Table with each round's answers
     */
    private Hashtable hAnswer;

    /**
     * @variable Object to access to database utilities
     */
    private SQLUtil sqlUtil;

    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";

    String nextQuestion = "";

    File tree = null;
    static Parser parser = null;
    TextView questionTextView;

    ArrayList<Button> botones = new ArrayList<Button>();

    protected void onCreate(Bundle savedInstanceState) {

        // Generating question and item cache
        try {
            sqlUtil = new SQLUtil(DATABASE_PATH);
            hQuestion = sqlUtil.getAllQuestion();
            hItem = sqlUtil.getAllItem();
        } catch (Exception e) {
            Log.e("DB_ERROR", "ERROR_GENERATING_CACHE");
            e.printStackTrace();
        }

        // Init answer's table
        hAnswer = new Hashtable();

        super.onCreate(savedInstanceState);
        Log.d("CREATE", "Recibido-one");
        Intent intent = getIntent();
        setContentView(R.layout.activity_one_player);
        Log.d("CREATE", "Recojido-One");

        tree = obtenerArbol();
        if (parser == null)
            parser = new Parser(tree);

        questionTextView = (TextView) findViewById(R.id.texto_one_2);
        nextQuestion = parser.getSiguientePregunta("");
        questionTextView.setText(nextQuestion);
        questionTextView.setTextSize(40);
        Log.d("CREATE", "Listo-One");

        Button boton1 = (Button) findViewById(R.id.boton_yes);
        Button boton2 = (Button) findViewById(R.id.boton_not);
        Button boton3 = (Button) findViewById(R.id.boton_maybe);

        botones.add(boton1);
        botones.add(boton2);
        botones.add(boton3);

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
        String actualQuestion = nextQuestion;
        nextQuestion = parser.getSiguientePregunta(boton.getText().toString());

        if (!nextQuestion.startsWith("#")) {
            questionTextView.setText(nextQuestion);
            if (hQuestion != null && hQuestion.containsKey(actualQuestion))
                hAnswer.put(hQuestion.get(actualQuestion), boton.getText());
        } else {
            questionTextView.setText("Estabas pensando en un: " + nextQuestion.substring(1));
            if (hQuestion != null && hItem != null && hQuestion.containsKey(actualQuestion)
                    && hItem.containsKey(nextQuestion.substring(1))) {
                hAnswer.put(hQuestion.get(actualQuestion), boton.getText());
                hAnswer.put("item", hItem.get(nextQuestion.substring(1)));

                try {
                    this.sqlUtil.insertNewWekaData(hAnswer);
                } catch (Exception e) {
                    Log.e("ERROR_INSERT_ROUND_DATA", e.getMessage());
                    e.printStackTrace();
                }
            }
            for (Button boton2 : botones)
            {
                boton2.setVisibility(View.GONE);
            }
        }
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
