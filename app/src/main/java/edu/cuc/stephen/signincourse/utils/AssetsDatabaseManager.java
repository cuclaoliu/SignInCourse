package edu.cuc.stephen.signincourse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.cuc.stephen.signincourse.R;

/**
 * This is a Assets Database Manager
 * Use it, you can use a assets database file in you application
 * It will copy the database file to "/data/data/[your application package name]/database" when you first time you use it
 * Then you can get a SQLiteDatabase object by the assets database file
 *
 * @author RobinTang
 * @time 2012-09-20
 * <p/>
 * <p/>
 * How to use:
 * 1. Initialize AssetsDatabaseManager
 * 2. Get AssetsDatabaseManager
 * 3. Get a SQLiteDatabase object through database file
 * 4. Use this database object
 * <p/>
 * Using example:
 * AssetsDatabaseManager.initManager(getApplication()); // this method is only need call one time
 * AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();   // get a AssetsDatabaseManager object
 * SQLiteDatabase db1 = mg.getDatabase("db1.db");   // get SQLiteDatabase object, db1.db is a file in assets folder
 * db1.???  // every operate by you want
 * Of cause, you can use AssetsDatabaseManager.getManager().getDatabase("xx") to get a database when you need use a database
 */
public class AssetsDatabaseManager {
    private static final String databasePath = "/data/data/%s/database";
    private String tag = "LOGINFO";

    // A mapping from assets database file to SQLiteDatabase object
    private Map<String, SQLiteDatabase> databases = new HashMap<>();

    // Context of application
    private Context context = null;

    // Singleton Pattern
    private static AssetsDatabaseManager instance = null;

    public AssetsDatabaseManager(Context context) {
        this.context = context;
        this.tag = context.getString(R.string.app_name);
    }

    public static void initManager(Context context) {
        if (instance == null)
            instance = new AssetsDatabaseManager(context);
    }

    public static AssetsDatabaseManager getManager() {
        return instance;
    }

    public SQLiteDatabase getDatabase(String dbFile) {
        SQLiteDatabase database = databases.get(dbFile);
        if (database != null) {
            Log.e(tag, String.format("Return a database copy of %s", dbFile));
            return database;
        }

        if (context == null)
            return null;

        Log.e(tag, String.format("Create database %s", dbFile));
        String path = getDatabaseFilePath();
        String fileName = getDatabaseFile(dbFile);

        File file = new File(fileName);
        SharedPreferences preferences = context.getSharedPreferences(AssetsDatabaseManager.class.toString(), 0);
        // Get Database file flag, if true means database file is true and valid
        boolean flag = preferences.getBoolean(dbFile, false);
        if (!flag || !file.exists()) {
            file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                Log.e(tag, "Create \"" + path + "\" fail!");
                return null;
            }
            if (!copyAssetsToFileSystem(dbFile, fileName)) {
                Log.e(tag, String.format("Copy %s to %s fail!", dbFile, fileName));
                return null;
            }

            preferences.edit().putBoolean(dbFile, true).commit();
        }

        database = SQLiteDatabase.openDatabase(fileName, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        if (database != null) {
            databases.put(dbFile, database);
        }
        return database;
    }

    private boolean copyAssetsToFileSystem(String assetsSource, String destination) {
        Log.e(tag, "Copy " + assetsSource + " to " + destination);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            AssetManager manager = context.getResources().getAssets();
            inputStream = manager.open(assetsSource);
            outputStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private String getDatabaseFile(String dbFile) {
        return getDatabaseFilePath() + "/" + dbFile;
    }

    private String getDatabaseFilePath() {
        return String.format(databasePath, context.getApplicationInfo().packageName);
    }

    public boolean closeDatabase(String dbFile){
        SQLiteDatabase database = databases.get(dbFile);
        if(database!=null){
            database.close();
            databases.remove(dbFile);
            return true;
        }
        return false;
    }

    public static void closeAllDatabase(){
        if(instance!=null){
            for(int i=0; i<instance.databases.size(); i++){
                if(instance.databases.get(i)!=null)
                    instance.databases.get(i).close();
            }
            instance.databases.clear();
        }
    }

    public static void deleteAllDatabase(){
        closeAllDatabase();
        String path = instance.getDatabaseFilePath();
        File pathFile = new File(path);
        File files[] = pathFile.listFiles();
        if(files != null){
            for (File f : files){
                if (f.isFile()){
                    if (f.exists()){
                        try {
                            f.delete();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
