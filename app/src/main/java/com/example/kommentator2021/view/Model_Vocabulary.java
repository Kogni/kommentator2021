package com.example.kommentator2021.view;

import android.util.Log;

import java.util.HashMap;

public class Model_Vocabulary {
    Control_Main class_Control_Main;

    HashMap<String, object_Word> vocabulary;

    String focusWord;
    private static final String logtag = "Model_Vocabulary";

    public Model_Vocabulary(Control_Main control_main) {
        //Log.i(logtag, "Model_Vocabulary");
        vocabulary = new HashMap<String, object_Word>();

    }

    public String getFocusWord(String inputSentence) {
        //Log.i(logtag, "getFocusWord start "+inputSentence);
        String focusWord = null;

        HashMap<String, Integer> brukISetning = new HashMap<String, Integer>();
        HashMap<String, Integer> brukOverall = new HashMap<String, Integer>();

        //gå igjennom setning, sjekk hyppigheter
        String[] setningArray = inputSentence.split(" ");
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            //sjekk hyppighet for hvert ord i input, både overall og i den enkelte setning
            String ordA = setningArray[ordIDA];
            object_Word ordAObject = (object_Word) vocabulary.get(ordA);
            Integer tellerOverall = ordAObject.getBruksTeller();
            if ( brukISetning.get(ordA)==null){
                brukISetning.put(ordA,1);
            } else {
                Integer teller = brukISetning.get(ordA);
                brukISetning.replace(ordA, teller++);
            }
            if ( brukOverall.get(ordA)==null){
                brukOverall.put(ordA,tellerOverall);
            }
            //Log.i(logtag, "getFocusWord "+ordA+" setningsteller="+brukISetning.get(ordA)+" overallTeller="+brukOverall.get(ordA));
        }
        //hent ord(ene) med lavest hyppighet
        Integer lavest_Setning = 99;
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            if( brukISetning.get(ordA)<lavest_Setning) lavest_Setning=brukISetning.get(ordA);
        }
        //Log.i(logtag, "getFocusWord lavest_Setning="+lavest_Setning);

        //av ordene med lavest hyppighet i setning, finn det med lavest hyppighet overall
        Integer lavest_Overall = 99;
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            if( brukISetning.get(ordA)==lavest_Setning) {
                if( brukOverall.get(ordA)<lavest_Overall) {
                    lavest_Overall = brukOverall.get(ordA);
                }
            }
        }
        //Log.i(logtag, "getFocusWord lavest_Overall="+lavest_Overall);

        //hent ordet med lavest hyppighet
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            if( brukOverall.get(ordA)==lavest_Overall) {
                focusWord = ordA;
                //Log.i(logtag, "getFocusWord focusWord=" + focusWord);
            }
        }

        if ( focusWord==null){
            Log.i(logtag, "getFocusWord focusWord ble null. inputSentence=" + inputSentence);
        }
        return focusWord;
    }

    public void buildVocabulary(String[] erfaring) {
        //Log.i(logtag, "buildVocabulary 1 start "+erfaring);

        //gå igjennom hver lagrede setning
        for ( int setningID = 0 ; setningID < erfaring.length ;setningID++ ) {
            String setning = erfaring[setningID];
            if(setning==null){
                setningID=erfaring.length;
                //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+" 2a tomt for setninger");
            } else {
                addNewSentenceToVocabulary(setning);
            }
        }
        //Log.i(logtag, "buildVocabulary end "+erfaring.toString());
    }

    public void addNewSentenceToVocabulary(String nyErfaring) {
        String[] setningArray = nyErfaring.split(" ");
        //Log.i(logtag, "addNewSentenceToVocabulary setning="+nyErfaring+" 2b setning=" + setningArray);

        //gå igjennom hvert ord i setning, for å lagre data på det ordet
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+" 2ba1 ord A=" + ordA);

            object_Word ordAObject = (object_Word) vocabulary.get(ordA);

            //sjekk om ord allerede er laget
            if (ordAObject == null) {
                //lag ord hvis det ikke finnes
                ordAObject = new object_Word(ordA);
                vocabulary.put(ordA, ordAObject);
                //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+" 2ba2 lager ordobjekt: " + vocabulary.get(ordA));
            }
            ordAObject.bruktISetning();
            //oppdater ord

            //gå igjennom resten av ordene i setningen, for å lagre kobling til ordA
            for (int ordIDB = 0; ordIDB < setningArray.length; ordIDB++) {
                String ordB = setningArray[ordIDB];
                //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 3 ord B=" + ordB);

                if (ordIDA != ordIDB) {
                    //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 4a, sjekker "+ordB+" vs "+ordA);
                    if (ordIDB > (ordIDA - 1)) {
                        ordAObject.leggTilAssosiasjonForan(ordB);
                    } else if (ordIDB == (ordIDA - 1)) {
                        //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 4aa "+ordB+" er foran "+ordA);
                        ordAObject.foran(ordB);
                    } else if (ordIDB == (ordIDA + 1)) {
                        //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 4ab "+ordB+" er bak "+ordA);
                        ordAObject.bak(ordB);
                    }  else if (ordIDB < (ordIDA + 1)) {
                        //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 4ab "+ordB+" er bak "+ordA);
                        ordAObject.leggTilAssosiasjonBak(ordB);
                    } else {
                        //Log.i(logtag, "buildVocabulary setning="+nyErfaring+", ordA="+ordA+", ordB="+ordB+" Finner ikke plass i setning");

                    }
                } else {
                    //Log.i(logtag, "buildVocabulary "+erfaring.toString()+", setning="+setning+", ordA="+ordA+", ordB="+ordB+" 4b ord B skippes, fordi det er likt ord A");
                }
            }
        }
    }

    public String getForan(String focusWord, String inputSentence) {
        //.i(logtag, "getForan focusWord="+focusWord+" inputSentence=" + inputSentence);
        object_Word fokusordet = vocabulary.get(focusWord);
        //Log.i(logtag, "getForan 2 focusWord="+focusWord+" fokusordet=" + fokusordet);
        return fokusordet.getLikelyFrontWord(inputSentence);
    }

    public String getBak(String focusWord, String inputSentence) {
        //Log.i(logtag, "getBak focusWord="+focusWord+" inputSentence=" + inputSentence);
        object_Word fokusordet = vocabulary.get(focusWord);
        //Log.i(logtag, "getBak 2 focusWord="+focusWord+" fokusordet=" + fokusordet);
        String retur = fokusordet.getLikelyBackWord(inputSentence);
        //Log.i(logtag, "getBak focusWord="+focusWord+" inputSentence=" + inputSentence+" retur="+retur);
        return retur;
    }

    public String getAssosiasjonForan(String focusWord, String inputSentence) {
        //Log.i(logtag, "getAssosiasjonForan focusWord="+focusWord+" inputSentence=" + inputSentence);
        object_Word fokusordet = vocabulary.get(focusWord);
        //Log.i(logtag, "getAssosiasjonForan 2 focusWord="+focusWord+" fokusordet=" + fokusordet);
        return fokusordet.getLikelyFrontAssosiaction(inputSentence);
    }

    public String getAssosiasjonBak(String focusWord, String inputSentence) {
        //Log.i(logtag, "getAssosiasjonBak focusWord="+focusWord+" inputSentence=" + inputSentence);
        object_Word fokusordet = vocabulary.get(focusWord);
        //Log.i(logtag, "getAssosiasjonBak 2 focusWord="+focusWord+" fokusordet=" + fokusordet);
        return fokusordet.getLikelyBackAssosiaction(inputSentence);
    }
}
