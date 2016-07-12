package com.example.agatino.registrostudenti;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GestioneDB {

    static final String KEY_RIGAID = "_id";
    static final String KEY_COGNOME = "cognome";
    static final String KEY_NOME = "nome";
    static final String KEY_MATRICOLA = "matricola";
    static final String TAG = "GestioneDB";
    static final String DATABASE_NOME = "DBstudenti";
    static final String DATABASE_TABELLA = "studenti";
    static final int DATABASE_VERSIONE = 1;

    static final String DATABASE_CREAZIONE = "create table studenti (_id integer primary key autoincrement, "
            + "cognome text not null, nome text not null, matricola text not null);";

    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;


    public GestioneDB(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    /*
    Estendo la classe SQLiteOpenHelper che si occupa
    della gestione delle connessioni e della creazione del DB
    */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            // invoco il costruttore della classe madre
            super(context, DATABASE_NOME, null, DATABASE_VERSIONE);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREAZIONE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } // end mathod on create

        /*
         *  metodo astratto lasciato opportunamente senza implementazione in quanto, l'upgrade
         *  non è richiesto nel progetto
         */
        public void onUpgrade(SQLiteDatabase s ,int i, int j)
        {

        }

    } // end class Databasehelper

    /*
    apre la connessione al database sia in lettura che in scritta
     */
    public GestioneDB open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    /*
     * chiude la connsessione al database.
     */
    public void close() {
        DBHelper.close();
    }

    /*
     * questo metodo effettua una query al database:
     * la query restituisce tutti i record presenti nel db
     */
    public Cursor listaStudenti() {
        return db.query(DATABASE_TABELLA, new String[]{KEY_RIGAID, KEY_COGNOME, KEY_NOME, KEY_MATRICOLA}, null, null, null, null, null, null);
    }

    /*
    Inserimento di un nuovo studente nella tabella
    */
    public long inserisciStudente(String cognome, String nome, String matricola) {
        // creo una mappa di valori
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_COGNOME, cognome);
        initialValues.put(KEY_NOME, nome);
        initialValues.put(KEY_MATRICOLA, matricola);
        return db.insert(DATABASE_TABELLA, null, initialValues);
    }


}// end class GestioneDB
