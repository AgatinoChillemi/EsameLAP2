package com.example.agatino.registrostudenti;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private final int SPEECH_RECOGNITION_CODE = 1;
    private int PHOTO_REQUEST_CODE;
    private TextView cognomeOut;
    private TextView nomeOut;
    private TextView matricolaOut;
    private ImageButton btnMicrophone;
    private ImageView imagePhoto;
    private TextView testo1;
    private Toast toast;
    GestioneDB db = new GestioneDB(this);
    private long id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * inizializzazione componenti grafici
         */
        cognomeOut=(TextView) findViewById(R.id.cognomeOut);
        nomeOut=(TextView) findViewById(R.id.nomeOut);
        matricolaOut=(TextView) findViewById(R.id.matricolaOut);
        btnMicrophone=(ImageButton) findViewById(R.id.btn_mic);
        imagePhoto=(ImageView) findViewById(R.id.imagePhoto);
        testo1=(TextView) findViewById(R.id.testo1);


       cognomeOut.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nomeOut.setImeOptions(EditorInfo.IME_ACTION_DONE);
        matricolaOut.setImeOptions(EditorInfo.IME_ACTION_DONE);


        //registrazione listener per btnMicrophone
        btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });


    }// end onCreate method

    /**
     * configurazione impostazioni di speeching
     */
    private void startSpeechToText() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Inserire informazioni studenti");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        }
        catch (ActivityNotFoundException a) {

            Toast.makeText(getApplicationContext(),
                    "Spiacente, funzionalità non supportata su questo Device. :( ",
                    Toast.LENGTH_LONG).show();

        }// end catch

    } // end method startSpeechToText()

    /**
     * Callback per la l'activity di speech recognition
     * Richiamata dopo l'elaborazione dell'input vocale
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);

                    if(text.equals("foto"))
                        // apertura camera
                        camera();

                    else if( text.equals("cancella"))
                        // reset del Form di inserimento
                        reset();

                    else if(text.equals("salva"))
                        // inserimento studente nel db con chiave primaria id, autoincrementata
                        id=insertStudente(id);

                    else if(text.equals("visualizza"))
                        // query al db per visualizzare l'elenco completo
                        visualizzaTutti();


                    else if(text.equals("aiuto"))
                        // visualizza una leggenda sintetica dei comandi
                        help();

                    else
                        // si procede con la compilazione del form
                        valida(text); // se qualcosa va storto, rimetti setForm(text);
                }
            }// end case SPEECH_RECOGNITION_CODE
            break;
        }// end switch

        // scatto della foto andato a buon fine
        if (requestCode==PHOTO_REQUEST_CODE) {
            Bitmap bp = (Bitmap) data.getExtras().get("data");

            // si inserisce la foto nella imageView imagePhoto
            imagePhoto.setImageBitmap(bp);
        } // end if di gestione foto

    }// end onActivityResult(int, int, Intent)

    /*
    * questo medoto estrae dalla stringa che riceve in input i valori relativi
    * alle info inserite dall'utente.
    * La stringa passata in input è il risulato dell'elaborazione dello speech recognotion
     */
    public void setForm(String text){

        int i,j;
        String str1, str2;

        // rilevamento della sottostringa con valore cognome
        if (text.substring(0, 7).equals("cognome")) {
            str1 = text.substring(7, text.length());
            i = str1.indexOf("nome");
            if (i > 1)
                cognomeOut.setText(str1.substring(0, i));
            else
                allerta(text);

            // rilevazione della sottostringa con valore nome
            i=i+ "nome".length(); // il campo nome inizia dalla posizione i
            str2=str1.substring(i,str1.length());

            j=str2.indexOf("matricola");

            if(j>1)
                nomeOut.setText(str2.substring(0,j));
            else
                allerta(text);

            // rilevazione della sottostringa con valore matricola
            j=j+"matricola".length();
            if( j<str2.length())
                matricolaOut.setText(str2.substring(j, str2.length()));
            else
                allerta(text);

            }// end if esterno
        else
            allerta(text);

        //imagePhoto=null;

    }// end setForm()




    /*
     * questo metodo resetta i campi nel Form di inserimento.
     * non cancella la riga dal DB
     */
    public void reset(){
        cognomeOut.setText("");
        nomeOut.setText("");
        matricolaOut.setText("");
        testo1.setVisibility(View.VISIBLE );
        imagePhoto.setImageBitmap(null);

        visualizzaToast("reset avvenuto con successo");
    } // end method reset()

    /*
     * questo metodo apre la camera posteriore del device
     */
    public void camera(){

        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(photoIntent, PHOTO_REQUEST_CODE);

    }// end method camera()



    /*
     * questo metodo estrae i valori degli attributi dal form e li inserisce nel db
     */
    public long insertStudente(long id){

        String cognome=cognomeOut.getText().toString();
        String nome=nomeOut.getText().toString();
        String matricola=matricolaOut.getText().toString();

        // connessione al DB, aperta
        db.open();
        id = db.inserisciStudente(cognome,nome,matricola);  // effettua l'insert nel DB
        db.close();

        // connessione al DB, chiusa
        reset();
       visualizzaToast("Informazioni inserite con successo");

        return id;

    }// end method insertStudente(int)


    /*
     * lancia una finestra di allert con messaggio di attenzione e messaggio passato come parametro
     */
    public void allerta(String s){
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(MainActivity.this);
        miaAlert.setTitle("ATTENZIONE");
        miaAlert.setMessage(s);
        AlertDialog alert = miaAlert.create();
        alert.show();
    }// end method allert(String)

    /*
     * questo metodo, non utilizzato, effettua un match tra la stringa in input
     * e l'espressione regolare che dovrebbe rappresentare
     */
    public static boolean check(String regex, String input){

        if (Pattern.matches(regex, input))
            return true;
        else
            return false;

    }


    /*
     * validazione stringa input attraverso le regex
     */
    public void valida(String text){
        String regex="cognome [a-zA-Z ]{1,20}"+"nome [a-zA-Z ]{1,20}"+"matricola [a-zA-Z0-9 ]{1,8}";
        if(check(regex, text)){
            setForm(text);
        }
        else
            allerta(text);

    } // end method validazione(String)






    /*
     * questo metodo effettua una query al DB, facendosi restituire tutte le righe
     * equivalente in SQL:
     * select *; from Studenti;
     */
    public void visualizzaTutti(){

        db.open();
        Cursor c = db.listaStudenti();
        if (c.moveToFirst()) {
            do {
                toast = Toast.makeText(MainActivity.this,
                        "id: " + c.getString(0) + "\n" + "Cognome: "
                                + c.getString(1) + "\n"
                                + "nome: " + c.getString(2)+ "\n"
                                + "matricola: "+ c.getString(3),
                        Toast.LENGTH_LONG);
                toast.show();
            } while (c.moveToNext());
        }
        db.close();

    }// end method visualizzaTutti()



    public void visualizzaToast(String s){
        toast = Toast.makeText(MainActivity.this, s , Toast.LENGTH_SHORT);
        toast.show();
    }

    public void help(){
        String leggenda="per inserire uno studente: " + "\n"
                        +"nome attributo, valore attributo" + "\n"
                        + "foto, per aprire la camera" + "\n"
                        + "salva, per il salvataggio" + "\n"
                        + "cancella, per il reset" + "\n"
                        + "visualizza, per visualizzare tutto";

        allerta(leggenda);
    }

} //end main activityClass