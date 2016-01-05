package com.example.bernabe.psic;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.inra.qualscape.wekatexttoxml.WekaTextfileToXMLTextfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import weka.classifiers.trees.J48;
import weka.core.Instances;

public class OnePlayer extends ActionBarActivity {

    /**
     * Database path
     */
    private static final String DB_PATH = "/data/data/com.example.bernabe.psic/databases/";

    /**
     * Database path
     */
    private static final String DB_NAME = "MAQ";

    /**
     * @variable hQuestion All question cache
     */
    private Hashtable hQuestion;

    /**
     * @variable hItem All item cache
     */
    private Hashtable hItem;

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

    J48 treeClassifier;

    public final static String EXTRA_MESSAGE = "com.bernabe.psic.MESSAGE";

    String nextQuestion = "";

    File tree = null;
    Parser parser = null;
    TextView questionTextView;
    boolean answerFound = false;

    ArrayList<Button> botones = new ArrayList<Button>();

    private static final HashMap<String, Double> answerToDouble = new  HashMap<String, Double>(3);
    static
    {
        answerToDouble.put("YES", 1.0);
        answerToDouble.put("MAYBE", 0.5);
        answerToDouble.put("NO", 0.0);


    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initCache();


        if (hQuestion != null){
            Enumeration keys = hQuestion.keys();
            while (keys.hasMoreElements()) {
                Log.e("Clave", keys.nextElement().toString());
            }
        }

        /* Init answer's table */
        hAnswer = new Hashtable();
        try {
            // Prueba del arbol
            Instances wekaInput = sqlUtil.generateWekaInstances();
            Log.d("Instances", wekaInput.toString().replace("\n\n", "\n"));
            treeClassifier = new J48();
            String[] options = weka.core.Utils.splitOptions("-M 1");
            treeClassifier.setOptions(options);
            treeClassifier.buildClassifier(wekaInput);

            Log.d("Arbol", treeClassifier.toString().replace("\n\n", "\n"));
        } catch (Exception e){
            e.printStackTrace();
        }

        Log.d("CREATE", "Recibido-one");
        Intent intent = getIntent();
        setContentView(R.layout.activity_one_player);
        Log.d("CREATE", "Recojido-One");

        tree = obtenerArbol();
        if (parser == null)
            parser = new Parser(tree);

        questionTextView = (TextView) findViewById(R.id.texto_one_2);
        nextQuestion = parser.getSiguientePregunta(-1);
        questionTextView.setText(hQuestion.get(Integer.parseInt(nextQuestion)).toString());
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

        if (answerFound == false) {
            String actualQuestion = nextQuestion;
            nextQuestion = parser.getSiguientePregunta(answerToDouble.get(boton.getText().toString()));
            if (!nextQuestion.startsWith("#")) {
                String sNewQuestion = hQuestion.get(Integer.parseInt(nextQuestion)).toString();
                questionTextView.setText(sNewQuestion);
                if (hQuestion != null && hQuestion.containsKey(actualQuestion))
                    hAnswer.put(hQuestion.get(actualQuestion), boton.getText());
            } else {
                questionTextView.setText("Were you thinking of " + hItem.get(Integer.parseInt(nextQuestion.substring(1))) + "?");
                if (hQuestion != null && hItem != null && hQuestion.containsKey(actualQuestion)
                        && hItem.containsKey(Integer.parseInt(nextQuestion.substring(1)))) {
                    hAnswer.put(nextQuestion, boton.getText());
                    hAnswer.put("item", nextQuestion.substring(1));

                    try {
                        this.sqlUtil.insertNewWekaData(hAnswer);
                    } catch (Exception e) {
                        Log.e("ERROR_INSERT_ROUND_DATA", e.getMessage());
                        e.printStackTrace();
                    }
                }
                for (Button boton2 : botones) {
                    if (boton2.getText().toString().trim().equals("MAYBE"))
                        boton2.setVisibility(View.GONE);
                }

                answerFound = true;
            }
        }
        else            // We have a guess
        {
            if (boton.getText().toString().equals("YES"))
            {
                // TODO: Meter el resultado en la base de datos
            }
            else
            {
                for (Button boton2 : botones) {
                    if (boton2.getText().toString().trim().equals("MAYBE"))
                        boton2.setVisibility(View.GONE);
                }

                Button sendButton = (Button) findViewById(R.id.boton_send);
                final EditText answerText = (EditText) findViewById(R.id.edittext_answer);

                for (Button boton2 : botones) {
                    boton2.setVisibility(View.GONE);
                }

                questionTextView.setText("What were you thinking of?");
                sendButton.setVisibility(View.VISIBLE);
                answerText.setVisibility(View.VISIBLE);

                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String answer = answerText.getText().toString();

                        // TODO: Añadir la respuesta a la base de datos.
                    }
                });


            }
        }
    }

    private static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    private static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    private static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }

    public File obtenerArbol()  // De momento cargo un árbol que hay en la carpeta Assets.
                                // Habrá que cambiar este método para que llame a weka y calcule el árbol.
    {
/*
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;

        File wekaTree = null;
        try {
            in = assetManager.open("tree.xml");
            wekaTree = new File(getFilesDir(), "tree2.xml");
            out = new FileOutputStream(wekaTree);
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

        return wekaTree;*/

        AssetManager assetManager = getAssets();
        OutputStream out = null;

        File treeTextFile = null;
        try {
            treeTextFile = new File(getFilesDir(), "tree.txt");
            out = new FileOutputStream(treeTextFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
            outputStreamWriter.write(treeClassifier.toString().split("\n\n")[1]);
            outputStreamWriter.close();
        }
        catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + "tree.txt", e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }

        File treeXMLFile = new File(getFilesDir(), "tree.xml");

        Log.d("File", readFromFile(treeTextFile));

        // TODO: Remove
        // J48 pruned tree
        // ------------------

        new WekaToXML(treeTextFile, treeXMLFile, true, false).writeXmlFromWekaText();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(treeXMLFile), "UTF-8"))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                Log.d("XML", line);
            }
        }
        catch (Exception e) {
        }

        return treeXMLFile;
      /*  AssetManager assetManager = getAssets();
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

        return treeXMLFile;*/

    }

    private String readFromFile(File file) {

        String ret = "";

        try {
            InputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append('\n');

                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


    public void initCache () {
        // Generating question and item cache
        try {
            sqlUtil = new SQLUtil(this, this.DB_PATH, this.DB_NAME);
            hQuestion = sqlUtil.getAllQuestion();
            hItem = sqlUtil.getAllItem();
        } catch (Exception e) {
            Log.e("DB_ERROR", e.getMessage());
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, Quiz.class));

    }


}
