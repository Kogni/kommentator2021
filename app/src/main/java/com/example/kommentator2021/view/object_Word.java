package com.example.kommentator2021.view;

import android.util.Log;

import java.util.HashMap;

public class object_Word {
    String word;
    private Integer brukstelling;
    private HashMap<String, Integer> foranCount;
    private HashMap<String, Integer> bakCount;
    private HashMap<String, Integer> assosiasjonForanCount;
    private HashMap<String, Integer> assosiasjonBakCount;

    private static final String logtag = "object_Word";

    public object_Word(String ordA) {
        //Log.i(logtag, "object_Word nytt ord="+ordA);
        word = ordA;
        foranCount = new HashMap<String, Integer>();
        bakCount = new HashMap<String, Integer>();
        assosiasjonForanCount = new HashMap<String, Integer>();
        assosiasjonBakCount = new HashMap<String, Integer>();
        brukstelling=0;
    }

    public void bruktISetning(){
        brukstelling++;
    }

    public void foran(String ordB) {
        //Log.i(logtag, "foran "+word+" "+ordB);
        if (foranCount.get(ordB)==null){
            foranCount.put(ordB,1);
        } else {
            int telling = foranCount.get(ordB);
            //Log.i(logtag, "foran " + word + " " + ordB + " gammel telling=" + foranCount.get(ordB));
            foranCount.replace(ordB, telling + 1);
            //Log.i(logtag, "foran " + word + " " + ordB + " ny telling=" + foranCount.get(ordB));
        }
       //Log.i(logtag, "foran " + word + "+" + ordB +"("+ordB+" "+word+ "), ny telling=" + foranCount.get(ordB)+". Object="+this);
    }

    public void bak(String ordB) {
        //Log.i(logtag, "bak "+word+" "+ordB);
        if (bakCount.get(ordB)==null){
            bakCount.put(ordB,1);
        } else {
            int telling = bakCount.get(ordB);
            //Log.i(logtag, "foran " + word + " " + ordB + " gammel telling=" + foranCount.get(ordB));
            bakCount.replace(ordB, telling + 1);
            //Log.i(logtag, "foran " + word + " " + ordB + " ny telling=" + foranCount.get(ordB));
        }
        Log.i(logtag, "bak " + word + "+" + ordB +"("+word+" "+ordB+ "), ny telling=" + bakCount.get(ordB)+". Object="+this);
    }

    public void leggTilAssosiasjonForan(String ordB) {
        //Log.i(logtag, "object_Word leggTilAssosiasjon "+word+" assosieres med "+ordB);
        if (assosiasjonForanCount.get(ordB)==null){
            assosiasjonForanCount.put(ordB,1);
        } else {
            int telling = assosiasjonForanCount.get(ordB);
            //Log.i(logtag, "foran " + word + " " + ordB + " gammel telling=" + foranCount.get(ordB));
            assosiasjonForanCount.replace(ordB, telling + 1);
            //Log.i(logtag, "foran " + word + " " + ordB + " ny telling=" + foranCount.get(ordB));
        }
    }

    public void leggTilAssosiasjonBak(String ordB) {
        //Log.i(logtag, "object_Word leggTilAssosiasjon "+word+" assosieres med "+ordB);
        if (assosiasjonBakCount.get(ordB)==null){
            assosiasjonBakCount.put(ordB,1);
        } else {
            int telling = assosiasjonBakCount.get(ordB);
            //Log.i(logtag, "foran " + word + " " + ordB + " gammel telling=" + foranCount.get(ordB));
            assosiasjonBakCount.replace(ordB, telling + 1);
            //Log.i(logtag, "foran " + word + " " + ordB + " ny telling=" + foranCount.get(ordB));
        }
    }

    public Integer getBruksTeller() {
        return brukstelling;
    }

    public String getLikelyFrontWord(String inputSentence) {
        //Log.i(logtag, "getLikelyFrontWord, word="+word+", "+inputSentence+". Object="+this);

        HashMap<String, Integer> brukISetning = new HashMap<String, Integer>();

        //gå igjennom setning, sjekk koblingsstyrke
        String[] setningArray = inputSentence.split(" ");
        Integer mestBruktTeller = 0;
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            //Log.i(logtag, "getLikelyFrontWord, word="+word+", sjekker ordA="+ordA);
            if ( foranCount.get(ordA) == null || ordA.equals(word)){

            } else {
                Integer bruktForan = foranCount.get(ordA);
                //Log.i(logtag, "getLikelyFrontWord, word="+word+", ordA="+ordA+", bruktForan="+bruktForan);
                if (bruktForan == null){//aldri brukt foran
                } else {
                    //noterer at ord finnes i inputsetning
                    brukISetning.put(ordA,bruktForan);
                    //vekter sannsynlighet for å legges til respons
                    Integer bruktteller = bruksTellerModifisert(ordA,foranCount.get(ordA));
                    //Log.i(logtag, "getLikelyFrontWord, word=" + word + ", ordA=" + ordA + ", foranCount.get(ordIDA)=" + foranCount.get(ordA));
                    if ( bruktteller > mestBruktTeller) {
                        mestBruktTeller = bruktteller;
                    }
                }
            }
        }
        //Log.i(logtag, "getLikelyFrontWord, word="+word+", mestBruktTeller="+mestBruktTeller);
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            //Log.i(logtag, "getLikelyFrontWord, word="+word+", 2, ordA="+ordA);
            if ( foranCount.get(ordA) == null || ordA.equals(word)){

            } else {
                Log.i(logtag, "getLikelyFrontWord, ordA="+ordA+", foranCount.get(ordA)="+foranCount.get(ordA));
                Integer bruktteller = bruksTellerModifisert(ordA,foranCount.get(ordA));
                if ( bruktteller == mestBruktTeller) {
                    //Log.i(logtag, "getLikelyFrontWord, word="+word+", mestBrukt ord="+ordA);
                    return ordA;
                }
            }
        }

        return null;
    }

    public String getLikelyBackWord(String inputSentence) {
        //Log.i(logtag, "getLikelyBackWord, word="+word+", "+inputSentence+". Object="+this);

        HashMap<String, Integer> brukISetning = new HashMap<String, Integer>();

        //gå igjennom setning, sjekk koblingsstyrke
        String[] setningArray = inputSentence.split(" ");
        Integer mestBruktTeller = 0;
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            //Log.i(logtag, "getLikelyBackWord, word="+word+", sjekker ordA="+ordA);
            if ( bakCount.get(ordA) == null || ordA.equals(word)){

            } else {
                Integer bruktBak = bakCount.get(ordA);
                //Log.i(logtag, "getLikelyBackWord, word="+word+", ordA="+ordA+", bruktBak="+bruktBak);
                if (bruktBak == null){//aldri brukt foran
                } else {
                    //noterer at ord brukes i inputsetning
                    brukISetning.put(ordA,bruktBak);
                    Integer bruktteller = bruksTellerModifisert(ordA,bakCount.get(ordA));
                    //Log.i(logtag, "getLikelyBackWord, word=" + word + ", ordA=" + ordA + ", foranCount.get(ordIDA)=" + foranCount.get(ordA));
                    if ( bruktteller > mestBruktTeller) {
                        mestBruktTeller = bruktteller;
                    }
                }
            }
        }
        //Log.i(logtag, "getLikelyBackWord, word="+word+", mestBruktTeller="+mestBruktTeller);
        for (int ordIDA = 0; ordIDA < setningArray.length; ordIDA++) {
            String ordA = setningArray[ordIDA];
            //Log.i(logtag, "getLikelyBackWord, word="+word+", 2, ordA="+ordA);
            if ( bakCount.get(ordA) == null || ordA.equals(word)){

            } else {
                Integer bruktteller = bruksTellerModifisert(ordA,bakCount.get(ordA));
                if ( bruktteller == mestBruktTeller) {
                    //Log.i(logtag, "getLikelyBackWord, word="+word+", mestBrukt ord="+ordA);
                    return ordA;
                }
            }
        }

        //Log.i(logtag, "getLikelyBackWord, word="+word+", returning null");
        return null;
    }

    public String getLikelyFrontAssosiaction(String inputSentence) {
        HashMap<String, Integer> brukISetning = new HashMap<String, Integer>();

        String[] setningArray = inputSentence.split(" ");
        Integer mestBruktTeller = 0;

        for (String i : assosiasjonForanCount.keySet()) {
            Integer bruktteller = bruksTellerModifisert(i,assosiasjonForanCount.get(i));
            if ( bruktteller>mestBruktTeller){
                mestBruktTeller = bruktteller;
            }
        }
        for (String i : assosiasjonForanCount.keySet()) {
            Integer bruktteller = bruksTellerModifisert(i,assosiasjonForanCount.get(i));
            if ( bruktteller==mestBruktTeller){
                return i;
            }
        }

        return null;
    }

    public String getLikelyBackAssosiaction(String inputSentence) {
        HashMap<String, Integer> brukISetning = new HashMap<String, Integer>();

        String[] setningArray = inputSentence.split(" ");
        Integer mestBruktTeller = 0;

        for (String i : assosiasjonBakCount.keySet()) {
            Integer bruktteller = bruksTellerModifisert(i,assosiasjonBakCount.get(i));
            if ( bruktteller>mestBruktTeller){
                mestBruktTeller = bruktteller;
            }
        }
        for (String i : assosiasjonBakCount.keySet()) {
            Integer bruktteller = bruksTellerModifisert(i,assosiasjonBakCount.get(i));
            if ( bruktteller==mestBruktTeller){
                return i;
            }
        }

        return null;
    }

    private Integer bruksTellerModifisert(String ordSjekket,Integer baseTeller){
        //Log.i(logtag, "bruksTellerModifisert, ordSjekket="+ordSjekket+", baseTeller="+baseTeller);
        Integer tellerModifisert = baseTeller;

        if ( ordSjekket.equals("the")){
            tellerModifisert = tellerModifisert -2;
        } else if ( ordSjekket.equals("I")){
            tellerModifisert = tellerModifisert --;
        } else if ( ordSjekket.equals("u")){
            tellerModifisert = 0;
        } else if ( ordSjekket.equals("s")){
            tellerModifisert = 0;
        }
        return tellerModifisert;
    }

}
