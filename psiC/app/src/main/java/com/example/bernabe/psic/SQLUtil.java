package com.example.bernabe.psic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import static android.database.sqlite.SQLiteDatabase.openDatabase;

/**
 * This is a class that contains several methods to interact with a sqlite database using Weka
 *
 * @author  David Antolin Alvarez
 * @version 0.3.1
 */

public class SQLUtil extends PreloadedDatabaseHelper {

    private static final String ANDROID_PREDEFINED_ID = "_id";

    /* Table question variables */
    private static final String TABLE_QUESTION = "question";

    private static final String COLUMN_QUESTION_ID = "questionId";

    private static final String COLUMN_QUESTION = "question";

    /* Table item variables */

    private static final String TABLE_ITEM = "item";

    private static final String COLUMN_ITEM_ID = "itemId";

    private static final String COLUMN_ITEM = "item";

    /* Table answer variables */
    private static final String TABLE_ANSWER = "answer";

    private static final String COLUMN_ROUND_NUMBER = "roundNumber";

    private static final String COLUMN_ANSWER_ID = "answerId";

    private static final String COLUMN_ANSWER_DESCRIPTION = "answerDescription";

    /**
     * SQLite database object to realize class operations
     */
    private static SQLiteDatabase sqliteDatabase = null;


    public SQLUtil (Context context, String DB_PATH, String DB_NAME) throws Exception{
        super (context, DB_PATH, DB_NAME);
        this.DB_PATH = DB_PATH;
        this.DB_NAME = DB_NAME;
        super.createDataBase();
    }

    /**
     * Given a database path for a sqlite database, returns a connection for that database.
     * @param databaseQualifiedName The path of the sqlite's database path
     * @return jdbcConnection The jdbc connection to the specified sqlite database
     */

    private SQLiteDatabase openSQLiteDatabase(String databaseQualifiedName, int openMode) throws Exception {
        SQLiteDatabase sqLiteDatabase;

        if (openMode == SQLiteDatabase.OPEN_READONLY) {
            sqLiteDatabase = openDatabase(databaseQualifiedName, null, SQLiteDatabase.OPEN_READONLY);
        } else if (openMode == SQLiteDatabase.OPEN_READWRITE)
            sqLiteDatabase = openDatabase(databaseQualifiedName, null, SQLiteDatabase.OPEN_READWRITE);
        else
            sqLiteDatabase = openDatabase(databaseQualifiedName, null, SQLiteDatabase.OPEN_READONLY);

        return sqLiteDatabase;

    }

    /**
     * It generates a well-formed weka input from the data obtained from the database
     * @param roundNumber The roundNumber to generate the input
     * @return sWekaInput A string with the weka input
     * @throws Exception
     */
    public Hashtable generateWekaRoundInput(int roundNumber) throws Exception {
        Hashtable hWekaInput = new Hashtable();

        // Query sentence for find all the pairs answer-question
        String answersQuery = "SELECT roundAnswers." + COLUMN_ANSWER_DESCRIPTION + ", " + TABLE_ITEM + "."
                + ANDROID_PREDEFINED_ID + " AS " + COLUMN_ITEM_ID + ", " + TABLE_QUESTION + "." + ANDROID_PREDEFINED_ID + " AS " + COLUMN_QUESTION_ID
                + " FROM " + TABLE_QUESTION + " LEFT OUTER JOIN "
                + "(SELECT * FROM " + TABLE_ANSWER + " WHERE " + COLUMN_ROUND_NUMBER + " = ?) AS roundAnswers ON "
                + TABLE_QUESTION + "." + ANDROID_PREDEFINED_ID + " = roundAnswers." + COLUMN_QUESTION_ID + " LEFT OUTER JOIN "
                + TABLE_ITEM + " ON roundAnswers." + COLUMN_ITEM_ID + " = " + TABLE_ITEM + "." + ANDROID_PREDEFINED_ID;

        if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen()){
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READWRITE);
        } else if (this.sqliteDatabase.isReadOnly()) {
            this.sqliteDatabase.close();
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READWRITE);
        }

        String [] queryArgs = new String [1];
        try {
            queryArgs[0] = String.valueOf(roundNumber); // Set the query filter
            Cursor queryResult = this.sqliteDatabase.rawQuery(answersQuery, queryArgs);
            Integer newItem = null;
            for (int i = 0; i< queryResult.getCount(); i++) {
                String newAnswer;
                int questionId;
                queryResult.moveToPosition(i);
                if (newItem == null) // Obtain the item found in that round
                    newItem = queryResult.getInt(queryResult.getColumnIndex(COLUMN_ITEM_ID));
                if (queryResult.getString(queryResult.getColumnIndex(COLUMN_ANSWER_DESCRIPTION)) != null &&
                        queryResult.getString(queryResult.getColumnIndex(COLUMN_QUESTION_ID)) != null) {
                    newAnswer = queryResult.getString(queryResult.getColumnIndex(COLUMN_ANSWER_DESCRIPTION));
                    questionId = queryResult.getInt(queryResult.getColumnIndex(COLUMN_QUESTION_ID));
                    hWekaInput.put(questionId, newAnswer);
                }
            }
            if (newItem != null) {
                String sItem = String.valueOf(newItem);
                hWekaInput.put("item", sItem);
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GENERATING_WEKA_INPUT " + e.getMessage());
        } finally {
            try {
                this.sqliteDatabase.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GENERATING_WEKA_INPUT " + e.getMessage());
            }
        }

        return hWekaInput;
    }

    /**
     * It returns a Vector with all rounds stored in database
     * @return vRounds A vector with all rounds stored in the database
     * @throws Exception
     */

    public Vector getAllRounds () throws Exception {
        Vector vRounds = new Vector();

        // Query used to obtain all rounds stored in database
        String sQueryAllRounds = "SELECT " + COLUMN_ROUND_NUMBER + " FROM " + TABLE_ANSWER + " GROUP BY "  + COLUMN_ROUND_NUMBER;

        if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen())
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READONLY);

        try {
            Cursor queryResult = this.sqliteDatabase.rawQuery(sQueryAllRounds, null);
            for (int i = 0; i< queryResult.getCount(); i++) { // Inserting each round in the round's vector
                queryResult.moveToPosition(i);
                vRounds.add(queryResult.getInt(queryResult.getColumnIndex(COLUMN_ROUND_NUMBER)));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_ROUNDS " + e.getMessage());
        } finally {
            try {
                this.sqliteDatabase.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GETTING_ROUNDS " + e.getMessage());
            }
        }

        return vRounds;
    }

    /**
     * It's return a new round number that it's equal to the actual maximum roundNumber + 1
     * @return newRoundNumber
     * @throws Exception
     */
    public int getNewRoundNumber () throws Exception {

        int newRoundNumber = -1;

        // Query used to obtain the maximum roundNumber stored in DB
        String sQueryMaxRound = "SELECT MAX(" + COLUMN_ROUND_NUMBER + ") AS " + COLUMN_ROUND_NUMBER
                + " FROM " + TABLE_ANSWER + " GROUP BY "  + COLUMN_ROUND_NUMBER;

        if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen())
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READONLY);

        int maxRoundNumber = 0;
        try {
            Cursor queryResult = this.sqliteDatabase.rawQuery(sQueryMaxRound, null);
            queryResult.moveToFirst();
            maxRoundNumber = queryResult.getInt(queryResult.getColumnIndex(COLUMN_ROUND_NUMBER));
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_NEW_ROUND " + e.getMessage());
        } finally {
            try {
                this.sqliteDatabase.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GETTING_NEW_ROUND " + e.getMessage());
            }
        }

        newRoundNumber = maxRoundNumber + 1;

        return newRoundNumber;

    }


    /**
     * Its returns a Hashtable with the contents of the table question from database
     * @return hQuestions where key = question, value = questionId
     * @throws Exception
     */

    public Hashtable getAllQuestion () throws Exception {
        Hashtable hQuestion = new Hashtable();

        String sQueryAllItem = "SELECT * FROM " + TABLE_QUESTION;

        if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen())
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READONLY);

        try {
            Cursor queryResult = this.sqliteDatabase.rawQuery(sQueryAllItem, null);
            for (int i = 0; i< queryResult.getCount(); i++) { // Inserting each round in the question's vector
                queryResult.moveToPosition(i);
                hQuestion.put(queryResult.getInt(queryResult.getColumnIndex(ANDROID_PREDEFINED_ID)), queryResult.getString(queryResult.getColumnIndex(COLUMN_QUESTION)));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_QUESTIONS " + e.getMessage());
        } finally {
            try {
                this.sqliteDatabase.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GETTING_QUESTIONS " + e.getMessage());
            }
        }

        return hQuestion;
    }

    /**
     * Its returns a Hashtable with the contents of the table question from database
     * @return hQuestions where key = question, value = questionId
     * @throws Exception
     */

    public Hashtable getAllItem () throws Exception {
        Hashtable hItem = new Hashtable();

        String sQueryAllItem = "SELECT * FROM " + TABLE_ITEM;

        if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen())
            this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READONLY);

        try {
            Cursor queryResult = this.sqliteDatabase.rawQuery(sQueryAllItem, null);
            for (int i = 0; i< queryResult.getCount(); i++) { // Inserting each item in the item's vector
                queryResult.moveToPosition(i);
                hItem.put(queryResult.getInt(queryResult.getColumnIndex(ANDROID_PREDEFINED_ID)), queryResult.getString(queryResult.getColumnIndex(COLUMN_ITEM)));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_ITEMS " + e.getMessage());
        } finally {
            try {
                this.sqliteDatabase.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GETTING_ITEMS " + e.getMessage());
            }
        }

        return hItem;
    }

    /**
     * Giving a table with query-answer-item data, it's insert in answer table a row for each answer in the table with a new generated roundNumber
     * @param hWekaData A table with the shape questionId1=answerDescription1, ..., "item"=itemId
     * @throws Exception
     */
    public void insertNewWekaData (Hashtable hWekaData) throws Exception {

        int newRoundNumber = this.getNewRoundNumber();
        if (newRoundNumber > 0) {

            if (this.sqliteDatabase == null || !this.sqliteDatabase.isOpen()){
                this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READWRITE);
            } else if (this.sqliteDatabase.isReadOnly()) {
                this.sqliteDatabase.close();
                this.sqliteDatabase = this.openSQLiteDatabase((this.DB_PATH) + (this.DB_NAME), SQLiteDatabase.OPEN_READWRITE);
            }

            try {
                int itemId = (int) hWekaData.get(COLUMN_ITEM); // Get the itemId from the input table
                // Init the transaction to add data all together
                this.sqliteDatabase.beginTransaction();
                Enumeration dataKeys = hWekaData.keys();
                ContentValues cValuesInsert;
                while (dataKeys.hasMoreElements()) { // Insert a new row in answer table for each answer in table
                    int questionId = (int) dataKeys.nextElement();
                    String sAnswerDescription = (String) hWekaData.get(questionId);
                    cValuesInsert = new ContentValues();
                    cValuesInsert.put(COLUMN_ROUND_NUMBER, newRoundNumber);
                    cValuesInsert.put(COLUMN_ITEM_ID, itemId);
                    cValuesInsert.put(COLUMN_QUESTION_ID, questionId);
                    cValuesInsert.put(COLUMN_ANSWER_DESCRIPTION, sAnswerDescription);
                    this.sqliteDatabase.insert(TABLE_ANSWER, null, cValuesInsert);
                }
                this.sqliteDatabase.setTransactionSuccessful();
                this.sqliteDatabase.endTransaction();
            } catch (Exception e) {
                this.sqliteDatabase.endTransaction();
                throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE " + e.getMessage());
            } finally {
                try {
                    this.sqliteDatabase.close();
                } catch (Exception e) {
                    throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE " + e.getMessage());
                }
            }

        } else {
            throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE");
        }

    }

    public ArrayList getWekaAttributes () throws Exception {
        ArrayList vWekaAttributes = new ArrayList();

        // Setting up the question attributes
        Hashtable hQuestions = this.getAllQuestion();
        Enumeration enQuestionId = hQuestions.keys();
        while (enQuestionId.hasMoreElements()) {
            Object questionId = enQuestionId.nextElement();
            vWekaAttributes.add(new Attribute(questionId.toString()));
        }

        // Setting up the item (class) attribute
        Attribute classAttribute = getWekaClassAttribute();
        vWekaAttributes.add(classAttribute);

        return vWekaAttributes;
    }

    public Attribute getWekaClassAttribute () throws Exception {
        Attribute classAttribute = null;

        Hashtable hItem = this.getAllItem();
        ArrayList vClassValues = new ArrayList();
        Enumeration enItemId = hItem.keys();
        while (enItemId.hasMoreElements()) {
            Object itemId = enItemId.nextElement();
            vClassValues.add(itemId.toString());
        }
        classAttribute = new Attribute("item", vClassValues);

        return classAttribute;
    }

    public Instances generateWekaInstances () throws Exception {
        Instances wekaInstances = null;

        Vector vRounds = this.getAllRounds();
        ArrayList vWekaAttributes = this.getWekaAttributes();
        Attribute classAttribute = this.getWekaClassAttribute();

        int classIndex = vWekaAttributes.size() - 1;

        if (vRounds != null && !vRounds.isEmpty()) {
            Instance roundInstance = null;
            wekaInstances = new Instances("Rel", vWekaAttributes, vRounds.size());
            wekaInstances.setClassIndex(classIndex);
            for (int i = 0; i < vRounds.size(); i++) {
                Hashtable hWekaData = this.generateWekaRoundInput((int)vRounds.get(i));
                if (hWekaData != null && !hWekaData.isEmpty()) {
                    roundInstance = new SparseInstance(vWekaAttributes.size());
                    roundInstance.setDataset(wekaInstances);
                    Enumeration enumQuestions = hWekaData.keys();
                    while (enumQuestions.hasMoreElements()) {
                        Object key = enumQuestions.nextElement();
                        if (key instanceof Number) { // Hay que setear los atributos de la instancia previamente. Lo tal seria meterle todas las questions al crear la instancia
                            Attribute attr = new Attribute(key.toString());
                            int wekaAttrIndex = vWekaAttributes.indexOf(attr);
                            if (wekaAttrIndex != -1) {
                                double numericAttrValue = -1;
                                if (hWekaData.get(key).toString().equalsIgnoreCase("NO"))
                                    numericAttrValue = 0;
                                else if (hWekaData.get(key).toString().equalsIgnoreCase("MAYBE"))
                                    numericAttrValue = 0.5;
                                else if (hWekaData.get(key).toString().equalsIgnoreCase("YES"))
                                    numericAttrValue = 1;
                                if (numericAttrValue != -1)
                                    roundInstance.setValue(wekaAttrIndex, numericAttrValue);
                            }
                        }else if (key instanceof String && key.toString().equalsIgnoreCase("item")) {
                            roundInstance.setValue(classIndex, hWekaData.get(key).toString());
                        }
                    }
                    if (roundInstance != null)
                        wekaInstances.add(roundInstance);
                }
            }
        }

        return wekaInstances;
    }
}