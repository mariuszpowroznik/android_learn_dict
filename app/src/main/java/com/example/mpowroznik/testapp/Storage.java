package com.example.mpowroznik.testapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;
import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xmlpull.v1.*;

import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

public class Storage {

    private Context m_ctx;
    private String ns = null;
    private static final String m_file = "dict.xml";
    private static final String m_backfile = "dict_backup.xml";
    private static Storage mStorageInstance;

    private int mSelectedList;
    private int mListVersion;
    private ArrayList<Map<Integer, String>> mEngLists;
    private ArrayList<Map<Integer, String>> mPlLists;
    private ArrayList<String> mListsName;
    private ArrayList<Boolean> mListCheck;
    private int mAppListVersion = 0; //-666;

    public static void createStorage(Context ctx){
        mStorageInstance = new Storage(ctx);
    }

    public static Storage getStorage(){
        return mStorageInstance;
    }

    Storage(Context ctx) {
        m_ctx = ctx;
        mEngLists = new ArrayList<>();
        mPlLists = new ArrayList<>();
        mListsName = new ArrayList<>();
        mListCheck = new ArrayList<>();
        mListVersion = 0;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_ctx);
        mSelectedList = sharedPref.getInt("selectedListIdx", 0);
    }

    public ArrayList<String> getLists() {
        return mListsName;
    }

    public ArrayList<Boolean> getChecked() {
        return mListCheck;
    }

    public Map<Integer, String> getEngMap(int idx) {
        return mEngLists.get(idx);
    }
    public Map<Integer, String> getPlMap(int idx) {
        return mPlLists.get(idx);
    }

    public void addNewList() {
        mEngLists.add(new TreeMap<Integer, String>());
        mPlLists.add(new TreeMap<Integer, String>());
    }

    public void remList(int idx) {
        mEngLists.remove(idx);
        mPlLists.remove(idx);
        mListsName.remove(idx);
        mListCheck.remove(idx);
    }

    public void updateMaps(int idx, Map<Integer, String> pl, Map<Integer, String> en) {
        mPlLists.set(idx, pl);
        mEngLists.set(idx, en);
    }
    public void setSelectedList(int idx) {
        mSelectedList = idx;
    }
    public int getSelectedList() {
        return mSelectedList;
    }

    public void flush(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_ctx);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedListIdx", mSelectedList);
        editor.commit();

        //SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putInt(getString(R.string.saved_high_score_key), newHighScore);
        //editor.commit();

        writeStorageFile();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean readXmlFile(String fileName)
    {
        boolean bParseOk = false;
        File file = new File(m_ctx.getExternalFilesDir(null), fileName);
        try {
            InputStream is = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            //no namespaces
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, ns, "version");
            parser.next();
            String txt = parser.getText();
            mListVersion = Integer.parseInt(txt);
            parser.next();
            parser.require(XmlPullParser.END_TAG, ns, "version");
            parser.next();

            int idx = 0;
            String name;
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    name = parser.getName();
                    if (name.equalsIgnoreCase("list")) {

                        mEngLists.add(new TreeMap<Integer, String>());
                        mPlLists.add(new TreeMap<Integer, String>());

                        parser.next();
                        parser.require(XmlPullParser.START_TAG, ns, "name");
                        parser.next();
                        mListsName.add(parser.getText());
                        mListCheck.add(false);
                        parser.next();
                        parser.require(XmlPullParser.END_TAG, ns, "name");

                        parser.next();
                        parser.require(XmlPullParser.START_TAG, ns, "words");
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            name = parser.getName();
                            // Starts by looking for the entry tag
                            if (name.equalsIgnoreCase("word")) {
                                if (parser.getAttributeCount() == 3) {

                                    Map<String, String> mapAttrib = new TreeMap<>();
                                    for (int i = 0; i < parser.getAttributeCount(); ++i) {
                                        mapAttrib.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                                    }

                                    if (mapAttrib.containsKey("id")
                                            && mapAttrib.containsKey("eng")
                                            && mapAttrib.containsKey("pl")) {
                                        mEngLists.get(idx).put(Integer.parseInt(mapAttrib.get("id")), mapAttrib.get("eng"));
                                        mPlLists.get(idx).put(Integer.parseInt(mapAttrib.get("id")), mapAttrib.get("pl"));
                                    }
                                }
                            }

                            parser.nextTag();
                        }

                        idx++;
                    }
                }

                eventType = parser.next();
            }

            if (mListsName.size() > 0) {
                bParseOk = true;
            }

            is.close();

        } catch (FileNotFoundException ex) {
            Log.w("File", "Cannot read xml file");
        } catch (XmlPullParserException pex){
            Log.w("File", "Cannot parse xml file");
        } catch (IOException ioex) {
            Log.w("File", "Cannot parse xml file, io exception");
        }

        return bParseOk;
    }

    public boolean readStorageFile()
    {
        boolean bFileOk = true;

        if (!readXmlFile(m_file)) {
            if(!readXmlFile(m_backfile)) {
                bFileOk = false;
            }
        }

        if(bFileOk == false || mAppListVersion == -666 || mAppListVersion > mListVersion){

            ArrayList<Map<Integer, String>> pl = new ArrayList<>();
            ArrayList<Map<Integer, String>> en = new ArrayList<>();
            ArrayList<String> name = new ArrayList<>();
            ArrayList<Boolean> check = new ArrayList<>();
            fillDefaultDictionary(pl, en, name, check);
            mergeLists(pl, en, name, check);

            bFileOk = writeStorageFile();
        }

        return bFileOk;
    }

    protected boolean writeStorageFile()
    {
        boolean bFileOk = true;
        try {
            File file = new File(m_ctx.getExternalFilesDir(null), m_file);
            File backfile = new File(m_ctx.getExternalFilesDir(null), m_backfile);
            OutputStream os1 = new FileOutputStream(file);
            OutputStream os2 = new FileOutputStream(backfile);

            String xmlString = createXmlFile();
            os1.write(xmlString.getBytes(Charset.forName("UTF-8")));
            os2.write(xmlString.getBytes(Charset.forName("UTF-8")));
            os1.flush();
            os2.flush();
            os1.close();
            os2.close();
        } catch (FileNotFoundException ex) {
            Log.w("File", "Cannot write xml file");
            showMessage("Error!", "Cannot write xml file, file not found exception");
            bFileOk = false;
        } catch (IOException ioex) {
            showMessage("Error!", "Cannot write xml file, io exception");
            Log.w("File", "Cannot write xml file, io exception");
            bFileOk = false;
        }

        return bFileOk;
    }

    protected String createXmlFile()
    {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);

            serializer.startTag("", "version");
            String str = Integer.toString(mAppListVersion == -666 ? 1 : mAppListVersion);
            serializer.text(str);
            serializer.endTag("", "version");

            for (int i=0; i<mListsName.size(); ++i){
                serializer.startTag("", "list");
                serializer.startTag("", "name");
                serializer.text(mListsName.get(i));
                serializer.endTag("", "name");
                serializer.startTag("", "words");
                Iterator<Map.Entry<Integer, String>> itPl = mPlLists.get(i).entrySet().iterator();
                for (Map.Entry<Integer, String> itEng : mEngLists.get(i).entrySet()) {
                    serializer.startTag("", "word");
                    serializer.attribute("", "id", itEng.getKey().toString());
                    serializer.attribute("", "eng", itEng.getValue());
                    Map.Entry<Integer, String> pair = itPl.next();
                    serializer.attribute("", "pl", pair.getValue());
                    serializer.endTag("", "word");
                }
                serializer.endTag("", "words");
                serializer.endTag("", "list");
            }

            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            showMessage("Error!", "Cannot create Xml file !!!");
        }
        return "";
    }

    protected void mergeLists(ArrayList<Map<Integer, String>> pl,
                                      ArrayList<Map<Integer, String>> en,
                                      ArrayList<String> names,
                                      ArrayList<Boolean> check) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_ctx);
        boolean bUpdateExisting = prefs.getBoolean("merger", false);
        if (bUpdateExisting) {
            mListsName = names;
            mListCheck = check;
            mPlLists = pl;
            mEngLists = en;
        }
        else {
            int idx = 0;
            for (String name: names) {
                if(mListsName.contains(name) == false) {
                    mListsName.add(name);
                    mListCheck.add(false);
                    mPlLists.add(pl.get(idx));
                    mEngLists.add(en.get(idx));
                }
                idx++;
            }
        }
    }

    protected void fillDefaultDictionary(ArrayList<Map<Integer, String>> pl,
                                      ArrayList<Map<Integer, String>> en,
                                      ArrayList<String> name,
                                      ArrayList<Boolean> check) {

        en.add(new TreeMap<Integer, String>());
        pl.add(new TreeMap<Integer, String>());
        name.add("Example List");
        check.add(false);
        en.get(0).put(1, "bike");          pl.get(0).put(1, "rower");
        en.get(0).put(2, "table");         pl.get(0).put(2, "stół");
        en.get(0).put(3, "chair");         pl.get(0).put(3, "krzesło");
        en.get(0).put(4, "go to sleep");   pl.get(0).put(4, "iść spać");
        en.get(0).put(5, "bed");           pl.get(0).put(5, "łóżko");
        en.get(0).put(6, "wake up");       pl.get(0).put(6, "obudzić się");
        en.get(0).put(7, "go to school");  pl.get(0).put(7, "iść do szkoły");
        en.get(0).put(8, "pillow");        pl.get(0).put(8, "poduszka");
        en.get(0).put(9, "wardrobe");      pl.get(0).put(9, "szafa");
        en.get(0).put(10, "desk");         pl.get(0).put(10, "biurko");

        en.add(new TreeMap<Integer, String>());
        pl.add(new TreeMap<Integer, String>());
        name.add("Verbs List");
        check.add(false);
        en.get(1).put(1, "work");          pl.get(1).put(1, "pracować");
        en.get(1).put(2, "pour");          pl.get(1).put(2, "lać");
        en.get(1).put(3, "read");          pl.get(1).put(3, "czytać");
        en.get(1).put(4, "write");         pl.get(1).put(4, "pisać");
        en.get(1).put(5, "walk");          pl.get(1).put(5, "iść");
        en.get(1).put(6, "listen");        pl.get(1).put(6, "słuchać");

        int i=1;
        en.add(new TreeMap<Integer, String>());
        pl.add(new TreeMap<Integer, String>());
        name.add("BBC List");
        check.add(false);
        en.get(2).put(i, "whizz");          pl.get(2).put(i++, "quick and noisy");
        en.get(2).put(i, "in a bid to");          pl.get(2).put(i++, "trying to achieve sth");
        en.get(2).put(i, "levy");          pl.get(2).put(i++, "collection, raise ex tax");
        en.get(2).put(i, "bid");          pl.get(2).put(i++, "auction, offer");
        en.get(2).put(i, "drive");          pl.get(2).put(i++, "attempt to achieve sth");
        en.get(2).put(i, "excess");          pl.get(2).put(i++, "amount more than necessary");
        en.get(2).put(i, "creep");          pl.get(2).put(i++, "person who make you uncomfortable");
        en.get(2).put(i, "relentless");          pl.get(2).put(i++, "never stopping");
        en.get(2).put(i, "engulf");          pl.get(2).put(i++, "covers completely");
        en.get(2).put(i, "bloodthirsty");          pl.get(2).put(i++, "keen to enjoy violence");
        en.get(2).put(i, "iron guts");          pl.get(2).put(i++, "strong stomach");
        en.get(2).put(i, "live off");          pl.get(2).put(i++, "depend on sth or someone");
        en.get(2).put(i, "live on");          pl.get(2).put(i++, "continue to live");
        en.get(2).put(i, "pardoned");          pl.get(2).put(i++, "officially forgiven for crime");
        en.get(2).put(i, "sanitised");          pl.get(2).put(i++, "made less offensive");
        en.get(2).put(i, "fumble");          pl.get(2).put(i++, "do sth inefficiently");
        en.get(2).put(i, "seesaw");          pl.get(2).put(i++, "changes repeatedly");
        en.get(2).put(i, "dig in heels");          pl.get(2).put(i++, "refuse to cooperate");
        en.get(2).put(i, "crash");          pl.get(2).put(i++, "attend party without invitation");
        en.get(2).put(i, "grab");          pl.get(2).put(i++, "gets what other want");
        en.get(2).put(i, "movements");          pl.get(2).put(i++, "group to achieve a shared aim");
        en.get(2).put(i, "subdued");          pl.get(2).put(i++, "quieter than usual");
        en.get(2).put(i, "sparse");          pl.get(2).put(i++, "small in number");
        en.get(2).put(i, "pin hopes on");          pl.get(2).put(i++, "hope sth will help");
        en.get(2).put(i, "invaluable");          pl.get(2).put(i++, "extremely useful");
        en.get(2).put(i, "foolproof");          pl.get(2).put(i++, "impossible to get wrong");
        en.get(2).put(i, "break ice");          pl.get(2).put(i++, "reduce tension");
        en.get(2).put(i, "launch");          pl.get(2).put(i++, "start project");
        en.get(2).put(i, "harassment");          pl.get(2).put(i++, "repeated unpleasant behaviour");
        en.get(2).put(i, "unveil");          pl.get(2).put(i++, "officially announce sth new");
        en.get(2).put(i, "reusable");          pl.get(2).put(i++, "able to be used again");
        en.get(2).put(i, "milestone");          pl.get(2).put(i++, "important stage in process");
        en.get(2).put(i, "put down");          pl.get(2).put(i++, "kill old animal");
        en.get(2).put(i, "face");          pl.get(2).put(i++, "deal with a bad situation");
        en.get(2).put(i, "in doubt");          pl.get(2).put(i++, "unlikely to continue");
        en.get(2).put(i, "likely");          pl.get(2).put(i++, "most probably");

    }

    protected void showMessage(String title, String message) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(m_ctx);
        builder.setTitle(title);
        builder.setMessage(message);
        // add a button
        builder.setPositiveButton("OK", null);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
