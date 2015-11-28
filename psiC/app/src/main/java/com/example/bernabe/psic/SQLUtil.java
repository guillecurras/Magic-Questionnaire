package com.example.bernabe.psic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.experiment.InstanceQuery;

/**
 * This is a class that contains several methods to interact with a sqlite database using Weka
 *
 * @author  David Antolin Alvarez
 * @version 0.1
 */

public class SQLUtil {

    /**
     * The user we insert when we don't want to specify any user
     */
    public static final String NO_USER = "nobody";
    /**
     * The password we insert when we don't want to specify any user
     */
    public static final String NO_USER_PWD = "";
    /**
     * The JDBC class used to connect to SQLite Database
     */
    public static final String SQLITE_CLASS = "org.sqlite.JDBC";

    /**
     * The item key
     */
    public static final String ITEM = "item";
    /**
     * Query sentence for find all items
     */
    public static final String Q_ALL_ITEM = "SELECT * FROM item";
    /**
     * Query sentence for find all questions
     */
    public static final String Q_ALL_QUESTION = "SELECT * FROM question";

    /**
     * It gives a query string, returns the InstanceQuery object asociate to that query.
     * @param sQuery The
     * @return jdbcConnection
     */

    public InstanceQuery getUnidentifiedInstanceQuery (String sQuery) throws Exception {
        InstanceQuery query = new InstanceQuery();

        query.setUsername(NO_USER);
        query.setPassword(NO_USER_PWD);
        query.setQuery(sQuery);

        return query;
    }

    /**
     * It gives a database path for a sqlite database, returns a connection for that database.
     * @param databasePath The path of the sqlite's database path
     * @return jdbcConnection The jdbc connection to the specified sqlite database
     */

    public Connection getSqliteConnection (String databasePath) throws Exception {

        Connection jdbcConnection = null;

        Class.forName(SQLITE_CLASS);
        jdbcConnection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

        return jdbcConnection;

    }

    /**
     * It generates a well-formed weka input from the data obtained from the database
     * @param roundNumber The roundNumber to generate the input
     * @return sWekaInput A string with the weka input
     * @throws Exception
     */
    public String generateWekaInput (int roundNumber) throws Exception {
        String sWekaInput = new String();

        // Create the connection to the database
        Connection dbConnection = null;
        PreparedStatement sqlStatement = null;

        // Query sentence for find all the pairs answer-question
        String answersQuery = "SELECT roundAnswers.answerDescription, item.item "
                + "FROM question LEFT OUTER JOIN "
                + "(SELECT * FROM answer WHERE roundNumber = ?) AS roundAnswers ON question.questionId = roundAnswers.questionId LEFT OUTER JOIN "
                + "item ON roundAnswers.itemId = item.itemId";


        try {
            dbConnection = getSqliteConnection("BDD/MAQ");
            sqlStatement = dbConnection.prepareStatement(answersQuery); // Prepare the query
            sqlStatement.setInt(1, roundNumber); // Set the query filter
            ResultSet answersQueryResult = sqlStatement.executeQuery();
            String newItem = null;
            while (answersQueryResult.next()) {
                if (newItem == null) // Obtain the item found in that round
                    newItem = answersQueryResult.getString("item");
                String newAnswer = answersQueryResult.getString("answerDescription");
                if (newAnswer == null) // Creating the output with the answers
                    sWekaInput += ",";
                else if (!newAnswer.contains(" "))
                    sWekaInput += newAnswer + ",";
                else
                    sWekaInput += "\"" + newAnswer + "\"," ;
            }
            if (newItem.contains(" ")) // Adding the item obtained
                sWekaInput += "\"" + newItem + "\"" ;
            else
                sWekaInput += newItem;
        } catch (Exception e){
            throw new Exception("M_ERROR_GENERATING_WEKA_INPUT " + e.getMessage());
        } finally {
            try {
                dbConnection.close();
            } catch (Exception e) {
                throw new Exception("M_ERROR_GENERATING_WEKA_INPUT " + e.getMessage());
            }
        }

        return sWekaInput;
    }

    /**
     * It returns a Vector with all rounds stored in database
     * @return vRounds A vector with all rounds stored in the database
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Vector getAllRounds () throws Exception {
        Vector vRounds = new Vector();

        // Query used to obtain all rounds stored in database
        final String Q_ALL_ROUNDS = "SELECT roundNumber FROM answer GROUP BY roundNumber";

        // Create the connection to the database
        Connection dbConnection = null;
        PreparedStatement sqlStatement = null;

        try {
            dbConnection = getSqliteConnection("BDD/MAQ");
            sqlStatement = dbConnection.prepareStatement(Q_ALL_ROUNDS); // Prepare the query
            ResultSet roundsQueryResult = sqlStatement.executeQuery();
            while (roundsQueryResult.next()) { // Inserting each round in the round's vector
                vRounds.add(roundsQueryResult.getInt("roundNumber"));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_ROUNDS " + e.getMessage());
        } finally {
            try {
                dbConnection.close();
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

        // Query used to obtain all rounds stored in database
        final String Q_ALL_ROUNDS = "SELECT MAX(roundNumber) AS roundNumber FROM answer GROUP BY roundNumber";

        // Create the connection to the database
        Connection dbConnection = null;
        PreparedStatement sqlStatement = null;

        int maxRoundNumber = 0;
        try {
            dbConnection = getSqliteConnection("BDD/MAQ");
            sqlStatement = dbConnection.prepareStatement(Q_ALL_ROUNDS); // Prepare the query
            ResultSet roundsQueryResult = sqlStatement.executeQuery();
            while (roundsQueryResult.next()) { // Inserting each round in the round's vector
                maxRoundNumber = roundsQueryResult.getInt("roundNumber");
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_NEW_ROUND " + e.getMessage());
        } finally {
            try {
                dbConnection.close();
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
    @SuppressWarnings("unchecked")
    public Hashtable getAllQuestion () throws Exception {
        Hashtable hQuestion = new Hashtable();

        // Create the connection to the database
        Connection dbConnection = null;
        PreparedStatement sqlStatement = null;

        try {
            dbConnection = getSqliteConnection("BDD/MAQ");
            sqlStatement = dbConnection.prepareStatement(Q_ALL_QUESTION); // Prepare the query
            ResultSet roundsQueryResult = sqlStatement.executeQuery();
            while (roundsQueryResult.next()) { // Inserting each round in the round's vector
                hQuestion.put(roundsQueryResult.getString("question"), roundsQueryResult.getInt("questionId"));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_QUESTIONS " + e.getMessage());
        } finally {
            try {
                dbConnection.close();
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
    @SuppressWarnings("unchecked")
    public Hashtable getAllItem () throws Exception {
        Hashtable hItem = new Hashtable();

        // Create the connection to the database
        Connection dbConnection = null;
        PreparedStatement sqlStatement = null;

        try {
            dbConnection = getSqliteConnection("BDD/MAQ");
            sqlStatement = dbConnection.prepareStatement(Q_ALL_ITEM); // Prepare the query
            ResultSet roundsQueryResult = sqlStatement.executeQuery();
            while (roundsQueryResult.next()) { // Inserting each round in the round's vector
                hItem.put(roundsQueryResult.getString("item"), roundsQueryResult.getInt("itemId"));
            }
        } catch (Exception e){
            throw new Exception("M_ERROR_GETTING_ITEMS " + e.getMessage());
        } finally {
            try {
                dbConnection.close();
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

            // Insert sentence to answer table
            String sInsertSentence = "INSERT INTO answer (roundNumber, itemId, questionId, answerDescription) "
                    + "VALUES (?, ?, ?, ?)";

            // Create the connection to the database
            Connection dbConnection = null;
            PreparedStatement sqlStatement = null;

            try {
                int itemId = (int) hWekaData.get(ITEM); // Get the itemId from the input table
                dbConnection = getSqliteConnection("BDD/MAQ");
                // Init the transaction to add data all together
                dbConnection.setAutoCommit(false);
                sqlStatement = dbConnection.prepareStatement(sInsertSentence); // Prepare the query
                // Setting the insert values
                sqlStatement.setInt(1, newRoundNumber);
                sqlStatement.setInt(2, itemId);

                Enumeration dataKeys = hWekaData.keys();
                while (dataKeys.hasMoreElements()) { // Insert a new row in answer table for each answer in table
                    int questionId = (int) dataKeys.nextElement();
                    String sAnswerDescription = (String) hWekaData.get(questionId);
                    sqlStatement.setInt(3, (int) questionId);
                    sqlStatement.setString(4, sAnswerDescription);
                    sqlStatement.executeQuery();
                }
                dbConnection.commit();
            } catch (Exception e) {
                dbConnection.rollback();
                throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE " + e.getMessage());
            } finally {
                try {
                    dbConnection.close();
                } catch (Exception e) {
                    throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE " + e.getMessage());
                }
            }

        } else {
            throw new Exception("M_ERROR_INSERTING_WEKA_DATA_IN_DATABASE");
        }

    }

}