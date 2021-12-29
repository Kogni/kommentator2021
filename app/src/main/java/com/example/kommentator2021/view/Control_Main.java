package com.example.kommentator2021.view;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

public class Control_Main {

    View_Main class_View_Main;
    Model_Vocabulary class_Model_Vocabulary;
    private static final String logtag = "Control_Main";

    public Control_Main(View_Main view_main) {
        class_View_Main = view_main;
        class_Model_Vocabulary = new Model_Vocabulary(this);
        hentFremErfaring();
    }

    public void lagreAllErfaring(){
        Log.i(logtag, "lagreAllErfaring");
        //hent liste over erfaringer
        String[] erfaring= class_View_Main.readFileToExperience();

        for (int x = 0; x < erfaring.length; x++) {
            String setning = erfaring[x];
            if (setning != null){
                //send hver erfaring til skriving
                //Log.i(logtag, "lagreAllErfaring, sendes til lagring: "+setning);
            class_View_Main.saveInputToFile(setning);
            } else {
                return;
            }
        }

    }

    public void speech_to_text_received(String inputSentence) {

        //lagre input som erfaring for læring
        class_View_Main.saveInputToFile(inputSentence);

        //legg til ny erfaring
        class_Model_Vocabulary.addNewSentenceToVocabulary(inputSentence);

        //lag svar
        if ( class_View_Main.respondToSpeech == false ){
            Log.i(logtag, "speech_to_text_received, 3a, skal ikke respondere ");
            return;
        }

        generate_Answer(inputSentence);
        Log.i(logtag, "speech_to_text_received "+inputSentence+" 5");
    }

    private void generate_Answer(String inputSentence){
        //Log.i(logtag, "generate_Answer Start  "+inputSentence);
        //vurdere viktigte ord i inputSentence
        String focusWord = class_Model_Vocabulary.getFocusWord(inputSentence);
        Log.i(logtag, "generate_Answer 2, inputSentence="+inputSentence+", focusWord="+focusWord);
        if ( focusWord==null){
            return;
        }

        String respons2 = focusWord;
        //let etter ord foran, så langt det går
        String foran2 = class_Model_Vocabulary.getForan(focusWord, inputSentence);
        Log.i(logtag, "generate_Answer 3, inputSentence="+inputSentence+"focusWord="+focusWord+" foran2="+foran2);
        if ( foran2 != null){
            respons2 = foran2+" "+respons2;
            Log.i(logtag, "generate_Answer 4, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);
            String assosiasjonForan2 = class_Model_Vocabulary.getAssosiasjonForan(focusWord, inputSentence);
            Log.i(logtag, "generate_Answer 41, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);
            if ( allowWord(focusWord, foran2, assosiasjonForan2, respons2)){
                respons2 = assosiasjonForan2+" "+respons2;
                Log.i(logtag, "generate_Answer 41a, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);
                while (assosiasjonForan2 != null){
                    //assosiasjonForan2 = class_Model_Vocabulary.getForan(focusWord, inputSentence);
                    String assosiasjonForan2b = class_Model_Vocabulary.getAssosiasjonForan(assosiasjonForan2, inputSentence);
                    Log.i(logtag, "generate_Answer 41a2, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2+" assosiasjonForan2="+assosiasjonForan2+" assosiasjonForan2b="+assosiasjonForan2b);
                    if ( allowWord(focusWord, assosiasjonForan2, assosiasjonForan2b, respons2)){
                        respons2 = assosiasjonForan2b+" "+respons2;
                        assosiasjonForan2 = assosiasjonForan2b;
                    } else {
                        break;
                    }
                    Log.i(logtag, "generate_Answer 41a3, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);
                }
            }
        }


        //let etter ord bak, så langt det går
        String bak2 = class_Model_Vocabulary.getBak(focusWord, inputSentence);
        Log.i(logtag, "generate_Answer 5, inputSentence="+inputSentence+", focusWord="+focusWord+", bak2="+bak2);
        if ( bak2 != null){
            respons2 = respons2+" "+bak2;
            String assosiasjonBak2 = class_Model_Vocabulary.getAssosiasjonBak(focusWord, inputSentence);
            if ( allowWord(focusWord, bak2, assosiasjonBak2, respons2) ){
                respons2 = respons2+" "+assosiasjonBak2;
                while (assosiasjonBak2 != null){
                    Log.i(logtag, "generate_Answer 6, inputSentence="+inputSentence+", focusWord="+focusWord+", assosiasjonBak2="+assosiasjonBak2);
                    String assosiasjonBak2b = class_Model_Vocabulary.getAssosiasjonBak(assosiasjonBak2, inputSentence);
                    if ( allowWord(focusWord, assosiasjonBak2, assosiasjonBak2b, respons2)){
                        respons2 = assosiasjonBak2b+" "+respons2;
                        assosiasjonBak2 = assosiasjonBak2b;
                    } else {
                        break;
                    }
                }
            }
        }

        Log.i(logtag, "generate_Answer 7, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);

        if ( respons2.equals(inputSentence)){
            Log.i(logtag, "generate_Answer 8, inputSentence="+inputSentence+", focusWord="+focusWord+", respons2="+respons2);
            respons2 = premadeAnswers(inputSentence);
        }

        Log.i(logtag, "generate_Answer End, respons2="+respons2);
        text_for_speech(respons2);

    }

    private boolean allowWord(String focusword, String previousWord, String newWord, String existingResponse){
        Log.i(logtag, "allowWord focusword="+focusword+", previousWord="+previousWord+", newWord="+newWord+", existingResponse="+existingResponse);
        //if ( assosiasjonBak2b != null && !assosiasjonBak2b.equals(assosiasjonBak2) && !assosiasjonBak2b.equals(focusWord))
        //if ( assosiasjonForan2 != null && !assosiasjonForan2.equals(foran2) && !assosiasjonForan2.equals(focusWord)){
        if ( newWord== null){
            return false;
        } else if (newWord.equals(focusword)){
            return false;
        } else if (newWord.equals(previousWord)){
            return false;
        } else if ( existingResponse.indexOf(newWord) > 0){
            //tell instances
            String[] setningArray = existingResponse.split(" ");
            int instances = 0;
            for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
                String ordA = setningArray[ordIDA];
                if ( ordA.equals(newWord)){
                    instances++;
                    if ( instances >= 2){
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private String premadeAnswers(String triggerText){
        Log.i(logtag, "premadeAnswers triggerText="+triggerText);
        if ( triggerText.equals("hello") || triggerText.equals("hi") ){
            return "Hello, how are you?";
        }
        return triggerText;
    }

    private void hentFremErfaring(){ //husk at denne må gjøres kun 1 gang, ikke lagre assosiasjoner osv på gamle erfaringer hver gang det kommer input
        String[] erfaring= class_View_Main.readFileToExperience();
        String[] erfaring2= class_View_Main.readRaw();
        class_Model_Vocabulary.buildVocabulary(erfaring);
    }

    private void text_for_speech(String toString){
        class_View_Main.text_to_speech(toString);
    }

    void writeToSDFile(String inputSentence){
        Log.i(logtag, "writeToSDFile 1 start, inputSentence="+inputSentence);

        File root = android.os.Environment.getExternalStorageDirectory();
        class_View_Main.setProgressText("External file system root: "+root);

        File dir = new File (root.getAbsolutePath() + "/Kommentator2021");
        dir.mkdirs();
        File file = new File(dir, "KommentatorErfaring2.txt");
        Log.i(logtag, "writeToSDFile 2 file="+file.getAbsolutePath());

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(inputSentence);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(logtag, "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        class_View_Main.setProgressText("Data written to "+file);
        //Log.i(logtag, "writeToSDFile 2 end");
    }

    private void readRaw(){

        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/Kommentator2021/"+"KommentatorErfaring.txt");
            class_View_Main.setProgressText("Reading data from "+file.getAbsolutePath());
            InputStream is = new FileInputStream(file);

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size

            try {
                String test;
                String allText="";
                while (true){
                    test = br.readLine();
                    if(test == null) break;
                    allText=allText+"\n"+"    "+test;
                    //tv.setText("\n"+"    "+test);
                    class_View_Main.setProgressText(allText);
                }
                isr.close();
                is.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                class_View_Main.setProgressText("Failed at reading from file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
