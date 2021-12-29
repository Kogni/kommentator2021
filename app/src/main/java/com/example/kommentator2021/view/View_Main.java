package com.example.kommentator2021.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kommentator2021.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class View_Main extends AppCompatActivity {

    private static final String logtag = "View_Main";
    Control_Main class_Control_Main;
    private EditText progressText;
    private ImageView buttonListenRespond;
    private ImageView buttonContinueListen;
    //String filnavn = "/storage/emulated/0/Inputsfile.txt";

    //settings
    String listenerMode="";
    boolean respondToSpeech = true;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_WRITE_STORAGE = 112;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public int MY_DATA_CHECK_CODE = 0; //trengs egentlig denne?
    public static final Integer RecordAudioRequestCode = 1; //trengs egentlig denne?
    public static final Integer WriteFilesRequestCode = 1; //trengs egentlig denne?

    //speech to text/speech recognition
    boolean lytter = false;
    Intent speechRecognizerIntent;
    private SpeechRecognizer speechRecognizer;

    //text to speech
    TextToSpeech textToSpeech;
    String mostRecentUtteranceID;
    Integer mostRecentUtteranceWordCount;
    boolean doneSpeaking=true;

    boolean doneLoading = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(logtag, "onCreate start -------------------------------------------------------------");

        setContentView(R.layout.activity_speech);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        ActivityCompat.requestPermissions(View_Main.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},00);


        buttonListenRespond = findViewById(R.id.buttonListenRespond);
        buttonContinueListen = findViewById(R.id.buttonContinueListen);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        Bundle extra = speechRecognizerIntent.getExtras();
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        extra = speechRecognizerIntent.getExtras();
        for (String key : extra.keySet()) {
            //saveKeyValueInPrefs(key, extra.get(key).toString()); //To Implement
            Log.i(logtag, "onReadyForSpeech bundle 1 content: key="+key+" content="+extra.get(key).toString());
        }
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-GB");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-CA");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-AU");
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-ie");
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-nz");
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "no-NO");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, -1000);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 0);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000);

        extra = speechRecognizerIntent.getExtras();
        for (String key : extra.keySet()) {
            //saveKeyValueInPrefs(key, extra.get(key).toString()); //To Implement
            Log.i(logtag, "onReadyForSpeech bundle 2 content: key="+key+" content="+extra.get(key).toString());
        }

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {
                //progressText.setHint("onReadyForSpeech");
                //setProgressText("onReadyForSpeech");
                //Log.i(logtag, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                //progressText.setHint("Listening...");
                setProgressText("Listening");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }
            @Override
            public void onEndOfSpeech() {

                //progressText.setHint("onEndOfSpeech");
                setProgressText("End Of Speech");
                //Log.i(logtag, "onEndOfSpeech listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
            }

            @Override
            public void onError(int i) {
                //progressText.setHint("onError "+i);
                //Log.i(logtag, "onError listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                setProgressText("Error "+i);
                //Log.i(logtag, "onError: "+i);
                lytter = false;
                startLytting("onError");

            }

            @Override
            public void onResults(Bundle bundle) {
                //Log.i(logtag, "onResults start, listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                //progressText.setHint("onResults");
                setProgressText("Got Results");
                //Log.i(logtag, "onResults 1, start ");
                stopLytting();

                ArrayList<String> speechToText_inputSentence = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                //Log.i(logtag, "onResults 2, speechToText_inputSentence="+speechToText_inputSentence);
                for (int i = 0; i < speechToText_inputSentence.size(); i++) {
                    Log.i(logtag, "onResults 2, speechToText_inputSentence, #"+i+"="+speechToText_inputSentence.get(i));
                }

                bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
                //Log.i(logtag, "onResults 3, bestResult="+bestResult);

                float[] scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                Log.i(logtag, "onResults 4, scores="+scores.toString());
                for (int i = 0; i < scores.length; i++) {
                    //Log.i(logtag, "onResults 4, scores, #"+i+"="+scores[i]);
                }

                //progressText.setText(speechToText_inputSentence.get(0));

                double prob = scores[0]*100;
                prob = Math.floor(prob);
                setInputText(speechToText_inputSentence.get(0)+" ("+prob+"% prob)");

                speech_to_text_received(speechToText_inputSentence);
                //Log.i(logtag, "onResults 2, ferdig");
                startLytting("onResults 5");
                //Log.i(logtag, "onResults end, listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                //Log.i(logtag, "onPartialResults: ");
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                //Log.i(logtag, "onEvent: ");
            }
        });

        buttonListenRespond.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                    setModeText("stopped");
                    lytter = false;
                    listenerMode="-";
                    //Log.i(logtag, "onTouch 1a listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    listenerMode="Respond";
                    respondToSpeech = true;
                    setModeText("Responding");
                    //Log.i(logtag, "buttonListenRespond, respond to speech ");
                    //Log.i(logtag, "onTouch 1b listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                    speechRecognizer.startListening(speechRecognizerIntent);
              }
                return false;
            }
        });

        buttonContinueListen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                    setModeText("stopped");
                    lytter = false;
                    listenerMode="-";
                    //Log.i(logtag, "onTouch 2a listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    listenerMode="Continuous";
                    respondToSpeech = false;
                    setModeText("Continuous listening");
                    //Log.i(logtag, "buttonContinueListen, just listen");
                    //Log.i(logtag, "onTouch 2b listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                    speechRecognizer.startListening(speechRecognizerIntent);
               }
                return false;
            }
        });

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //Log.i("View_Main", "TextToSpeech.onInit 1");
                //Log.i(logtag, "TextToSpeech listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
                if(textToSpeech.getEngines().size() == 0){
                    //Toast.makeText(View_Main.this,"No Engines Installed",Toast.LENGTH_LONG).show();
                }else{
                    if (status == TextToSpeech.SUCCESS){
                        //Log.i("View_Main", "TextToSpeech.onInit 2");

                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                //Log.i("View_Main", "ttsInitialized.onStart, "+utteranceId);
                            }
                            @Override
                            public void onDone(String utteranceId) {
                                //Log.i("View_Main", "ttsInitialized.onDone, utteranceId="+utteranceId);
                                //Log.i(logtag, "ttsInitialized.onDone listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
                                doneSpeaking = true;
                                setProgressText("Done responding");
                                startLytting("ttsInitialized.onDone");
                                if (!utteranceId.equals(mostRecentUtteranceID)) {
                                    //Log.i("View_Main", "ttsInitialized.onDone() blocked: utterance ID mismatch (got "+utteranceId+", last spoken was "+mostRecentUtteranceID);
                                    return;
                                }
                                boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
                                //Log.i("View_Main", "ttsInitialized.onDone. called on a background thread? : " + wasCalledFromBackgroundThread);
                                //Log.i("View_Main", "ttsInitialized.onDone working.");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("View_Main", "ttsInitialized.run"); //hva brukes denne til??
                                        //Toast.makeText(View_Main.this,"onDone working.",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.i("View_Main", "ttsInitialized.onError, "+utteranceId);
                            }
                        });
                    }
                }
                //Log.i("View_Main TextToSpeech", "onInit 3 end");
            }
        });


        boolean returnValue = Settings.System.canWrite(this);
        //Log.i(logtag, "onCreate: canWrite="+returnValue);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Log.i(logtag, "onCreate: canWrite 2");
            if (returnValue == false) {
                //Log.i(logtag, "onCreate: canWrite false. Context can write="+Settings.System.canWrite(getApplicationContext()));
                if (!Settings.System.canWrite(getApplicationContext())) {

                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    //Toast.makeText(getApplicationContext(), "Please allow system settings for writing ", Toast.LENGTH_LONG).show();
                    startActivityForResult(intent, 200);
                }
            }else {
                //Log.i(logtag, "onCreate: canWrite true");
                //Toast.makeText(getApplicationContext(), "You are allowed to write ", Toast.LENGTH_LONG).show();
            }
        }

        verifyStoragePermissions(this);
        requestPermission(this);
        checkExternalMedia();

        class_Control_Main = new Control_Main(this);

        //readFileToExperience();
        readRaw();

        doneLoading = true;
        class_Control_Main.lagreAllErfaring();

        Log.i(logtag, "onCreate end -------------------------------------------------------------");
    }

    private void slettFil() {
        Log.i(logtag, "slettFil 1");

        try {
            Log.i(logtag, "slettFil 6");
            if (Environment.getExternalStorageDirectory().isDirectory()) {
                Log.i(logtag, "slettFil 6-1");
                for (File file : Environment.getExternalStorageDirectory().listFiles()) {
                    Log.i(logtag, "slettFil 6-2");
                    if (file.getName().contains("Inputsfile.txt")) {
                        Log.i(logtag, "slettFil 6-3");
                        if (file.isFile()) {
                            Log.i(logtag, "slettFil 6-4");
                            if (file.exists()) {
                                Log.i(logtag, "slettFil 6-5, deleting");
                                file.delete();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
                Log.i(logtag, "slettFil 6, sletting feilet "+e.getCause());
                Log.i(logtag, "slettFil 6, sletting feilet "+e.toString());
                Log.i(logtag, "slettFil 6, sletting feilet "+e.getStackTrace());
        }

        readFileToExperience();
        Log.i(logtag, "slettFil 6");
    }

    private boolean getRespondTOSpeech() {
        boolean respondToSpeech = this.respondToSpeech;
        return respondToSpeech;
    }

    public void stopLytting(){
        //Log.i(logtag, "stopLytting listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
        //Log.i(logtag, "stopLytting ");
        lytter = false;
        speechRecognizer.stopListening();
    }

    public void startLytting(String source){
        //Log.i(logtag, "startLytting listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
        //Log.i(logtag, "startLytting "+source); //gir mye spam i logg
       // Log.i(logtag, "startLytting lytter="+lytter+" respondToSpeech="+respondToSpeech); //gir mye spam i logg
        if ( doneSpeaking == true) {
            if (lytter == false) {
                //Log.i(logtag, "startLytting 3a"); //gir mye spam i logg
                if (source.contains("onError")) { //skjønte ikke tale
                    //Log.i(logtag, "startLytting onError lytter="+lytter+" respondToSpeech="+respondToSpeech); //gir mye spam i logg
                    if (this.respondToSpeech) { //skjønte ikke hva som skal svares på
                        startlytting();
                    } else { //skal bare lytte kontinuerlig
                        startlytting();
                    }
                } else if (source.contains("onResults")) { //plukket opp tale.
                    Log.i(logtag, "startLytting onResults lytter=" + lytter + " respondToSpeech=" + respondToSpeech);
                    if (this.respondToSpeech) { //ferdig å svare. Lytte mer?
                        startlytting();
                    } else { //skal bare lytte kontinuerlig
                        startlytting();
                    }
                } else if (source.contains("text_to_speech")) { //ferdig å svare. Lytte mer?
                    Log.i(logtag, "startLytting text_to_speech lytter=" + lytter + " respondToSpeech=" + respondToSpeech);
                    //return; //unngå å lytte til segselv
                    startlytting();
                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        startlytting("startLytting text_to_speech");
                    }
                }, 1500);*/
                } else if (source.contains("ttsInitialized.onDone")) {
                    //Log.i(logtag, "startLytting ferdig å snakke selv");
                    startlytting();
                    takeScreenshot();
                    //captureScreen();
                } else {
                    Log.i(logtag, "startLytting - ukjent source: "+source);
                    startlytting();
                }


            } else {
                Log.i(logtag, "startLytting 3b, lytter allerede ");
            }
        }  //Log.i(logtag, "startLytting 4b, snakker selv");

    }

    private void startlytting(){
        //Log.i(logtag, "startLytting listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking+" source="+source);
        //Log.i(logtag, "startLytting, restarter lytting "+source);
        try {
            //speechRecognizer.startListening(speechRecognizerIntent);

            // get the speech class
            View v = findViewById(R.id.item_OutputText); //fetch a View: any one will do
            v.post(
                    new Runnable(){
                        public void run(){
                            //c.doSomething();
                            speechRecognizer.startListening(speechRecognizerIntent);
                        }
                    }
            );

            setProgressText("Listening...");
            lytter = true;
        } catch (Exception e){
            Log.i(logtag, "startLytting feilet i å restarte lytting: "+e.getCause());
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            e.printStackTrace( printWriter );
            printWriter.flush();

            String stackTrace = writer.toString();
            Log.i(logtag, "startLytting feilet i å restarte lytting, error="+stackTrace);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //hva trenger jeg egentlig her?
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
        } else if (requestCode == WriteFilesRequestCode && grantResults.length > 0 ){
        } else {
            // delete file
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(logtag, "verifyStoragePermissions 2 permission != PackageManager.PERMISSION_GRANTED");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void requestPermission(Activity context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/new_folder";
            File storageDir = new File(path);
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Log.i(logtag, "requestPermission 3, !storageDir.exists() && !storageDir.mkdirs()");
            }
        }
    }

    private void checkExternalMedia(){
        Environment.getExternalStorageState();

        //tv.setText("\n\nExternal Media: readable=" +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
    }

    public void speech_to_text_received(ArrayList<String> speechToText_InputSentence){

        String newInput = speechToText_InputSentence.toString();
        String lineClean = newInput.substring(1);
        lineClean = lineClean.substring(0,lineClean.length()-1);
        class_Control_Main.speech_to_text_received(lineClean);
    }

    public void text_to_speech(String text) {
        stopLytting();
        setOutputText(text);

        snakk(text);

        startLytting("text_to_speech");
    }

    public void snakk(String text_To_Speech){
        //Log.i(logtag, "snakk listenerMode="+listenerMode+" respondToSpeech="+respondToSpeech+" lytter="+lytter+" doneSpeaking="+doneSpeaking);
        if (doneSpeaking) {
            setProgressText("Responding...");
            mostRecentUtteranceID = (new Random().nextInt() % 999) + "";
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mostRecentUtteranceID);
            //tell antall ord i respons, for å vurdere om screenshot skal tas
            mostRecentUtteranceWordCount = 0;
            String[] setningArray = text_To_Speech.split(" ");
            for (String ordA : setningArray) {
                if (ordA != null) {
                    mostRecentUtteranceWordCount++;
                }
            }
            Log.i("View_Main", "snakk, " + text_To_Speech + ", ID=" + mostRecentUtteranceID);
            doneSpeaking = false;
            textToSpeech.speak(text_To_Speech, TextToSpeech.QUEUE_FLUSH, params);
        } else {
            Log.i("View_Main", "snakk, opptatt med å snakke");
        }
    }

    public void setModeText(String mode){
        TextView modeText = findViewById(R.id.Mode);
        modeText.setText(Html.fromHtml("<b>Listening mode:</b> "+mode));
    }

    public void setProgressText(String progress){
        TextView progressText = findViewById(R.id.ProgressText);
        progressText.setText(Html.fromHtml("<b>Last event:</b> "+progress));
    }

    public void setOutputText(String output){
        TextView outputText = findViewById(R.id.item_OutputText);
        outputText.setText(Html.fromHtml("<b>Last response given:</b> "+output));
    }

    public void setInputText(String input){
        TextView inputText = findViewById(R.id.item_InputText);
        inputText.setText(Html.fromHtml("<b>Last speech recognized:</b> "+input));
    }

    private void takeScreenshot() {

        if ( mostRecentUtteranceWordCount <= 2 ) {
            return;
        }
        Date now = new Date();
        String filename = "Kommentator2021 "+(now.getYear()+1900)+"-"+(now.getMonth()+1)+"-"+(now.getDay()+19)+" "+now.getHours()+":"+now.getMinutes()+":"+now.getSeconds();
        try {
            String mPath_3 = "/storage/emulated/0/DCIM/Screenshots/" + filename + ".jpg";

            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath_3);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.i("View_Main", "takeScreenshot, completed successfully: "+imageFile.getAbsolutePath());
            try {
                MediaScannerConnection.scanFile(this,
                        new String[] { imageFile.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                //Log.i("ExternalStorage", "Scanned " + path + ":");
                                //Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String[] readFileToExperience() {
        Log.i(logtag, "readFileToExperience 1 start");
        try {
            FileInputStream in = openFileInput("Inputsfile.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String[] erfaring = new String[9999];
            String line;
            int x = 0;
            while ((line = bufferedReader.readLine()) != null) {
                x++;
                try {
                    String lineClean = line;

                    lineClean = lineClean.replaceAll("\\[\\]", "");

                    if ( lineClean.length()>1){

                        sb.append(lineClean + ", ");
                        erfaring[x - 1] = lineClean;
                    }
                } catch (Exception e){
                }
            }
            Log.i(logtag, "readFileToExperience 8, hentet "+x+" linjer: "+sb.toString());
            Log.i(logtag, "readFileToExperience 9, length="+sb.length());
            inputStreamReader.close();

            return erfaring;
        } catch (IOException e) {
            Log.i(logtag, "readFileToExperience error: "+e.getCause());
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }

    String[]  readRaw(){
        Log.i(logtag, "readRaw 1 start");

        try {

            File root = android.os.Environment.getExternalStorageDirectory();

            File file = new File(root, "/Kommentator2021/"+"KommentatorErfaring1.txt");
            Log.i(logtag, "readRaw 2 file="+file.getAbsolutePath());
            InputStream is = new FileInputStream(file);

            InputStreamReader inputStreamReader = new InputStreamReader(is);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String[] erfaring = new String[99999];
            String line;
            int x = 0;
            while ((line = bufferedReader.readLine()) != null) {
                x++;
                try {
                    String lineClean = line;
                    lineClean = lineClean.replaceAll("\\[\\]", "");
                    //Log.i(logtag, "readFileToExperience "+x+" lineClean 2="+lineClean);

                    if ( lineClean.length()>1){
                        //Log.i(logtag, "readFileToExperience "+x+" will be saved: "+lineClean);
                        sb.append(lineClean + ", ");
                        erfaring[x - 1] = lineClean;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    setProgressText("Failed at reading from file");
                }
            }
            return erfaring;

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(logtag, "readRaw 5 end");
        return null;
    }

    public void saveInputToFile(String inputSentence){
        //Log.i(logtag, "saveInputToFile: 1 "+inputSentence);
        //setProgressText("save Input To File");

        inputSentence = inputSentence.replaceAll("\\[\\]", "");
        //Log.i(logtag, "saveInputToFile: 2 "+inputSentence);

        if (inputSentence.length() <= 1){
            return;
        }
        try {
            //Log.i(logtag, "saveInputToFile: 3 skal lagres: "+inputSentence);
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File (root.getAbsolutePath() + "/Kommentator2021");
            File inputsFile = new File(dir, "KommentatorErfaring1.txt");
            try {
                dir.mkdirs();
            } catch (Exception e){
            }

            FileOutputStream fos = new FileOutputStream (inputsFile.getAbsolutePath(), true); // true will be same as Context.MODE_APPEND

            fos.write(inputSentence.getBytes());
            fos.write(System.getProperty("line.separator").getBytes());
            //filnavn = fos.toString();
            fos.close();

            //readFileToExperience();
        } catch (Exception e) {
            Log.i(logtag, "saveInputToFile 3b inputs failed");
            e.printStackTrace();
        }
        //Log.i(logtag, "saveInputToFile: 4");
    }

}