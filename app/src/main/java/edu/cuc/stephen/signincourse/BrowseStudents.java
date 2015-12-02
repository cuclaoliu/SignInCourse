package edu.cuc.stephen.signincourse;


import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import edu.cuc.stephen.signincourse.utils.AssetsDatabaseManager;
import edu.cuc.stephen.signincourse.utils.StudentBean;


/**
 * A simple {@link Fragment} subclass.
 */
public class BrowseStudents extends Fragment {

    private AssetsDatabaseManager databaseManager;
    private SQLiteDatabase database;
    private GestureDetector gestureDetector;
    private StudentBean student = new StudentBean();
    private String databaseName = "eda.db";
    private final String tableName = "students";
    private Cursor cursor;
    private String pathPhotos;

    private TextView textViewNumber, textViewName, textViewMajor;
    private ImageView imageViewPhoto, imageViewSex;

    public BrowseStudents() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        cursor.close();
        AssetsDatabaseManager.closeAllDatabase();
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_browse_students, container, false);

        Toast.makeText(getActivity(), pathPhotos, Toast.LENGTH_LONG).show();
        databaseManager = AssetsDatabaseManager.getManager();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dbName = preferences.getString("course", "eda");
        if (dbName == null || dbName.equals(""))
            databaseName = "eda.db";
        else
            databaseName = dbName + ".db";
        openDatabase();
        textViewNumber = (TextView) view.findViewById(R.id.tv_number_value);
        textViewName = (TextView) view.findViewById(R.id.tv_name_value);
        textViewMajor = (TextView) view.findViewById(R.id.tv_major_value);
        imageViewPhoto = (ImageView) view.findViewById(R.id.iv_photo);
        imageViewSex = (ImageView) view.findViewById(R.id.iv_sex);

        //String querySQL = "select * from " + tableName;
        //cursor = database.rawQuery(querySQL, null);
        readRecordAndToView();

        gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getY() - e2.getY() > 30) {
                    readRecordAndToView(true);
                }else if( e2.getY() - e1.getY() > 30) {
                    readRecordAndToView(false);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        return view;
    }

    private String findStudentsPhotos() {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursorPhotos = contentResolver.query(imageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
        while (cursorPhotos.moveToNext()) {
            String pathFile = cursorPhotos.getString(cursorPhotos.getColumnIndex(MediaStore.Images.Media.DATA));
            File parentFile = new File(pathFile).getParentFile();
            if (parentFile == null)
                continue;
            String pathFolder = parentFile.toString();
            pathFolder = pathFolder.substring(pathFolder.lastIndexOf("/") + 1);
            String pathName = "eda_mcu_photos";
            if ("ee2014.db".equals(databaseName)){
                pathName = "ee2014";
            }
            if (!pathName.equals(pathFolder))
                continue;
            return parentFile.getAbsolutePath().toString();
        }
        return null;
    }

    private void readRecordAndToView() {
        readRecordAndToView(true);
    }

    private void readRecordAndToView(boolean next) {
        checkDatabaseName();
        if (next) {
            if (!cursor.moveToNext())
                return;
        } else {
            if (!cursor.moveToPrevious())
                return;
        }
        try {
            int index = cursor.getColumnIndex("number");
            String number = cursor.getString(index);
            student.setNumber(number);//
            student.setName(cursor.getString(cursor.getColumnIndex("name")));
            student.setSex(cursor.getString(cursor.getColumnIndex("sex")));
            student.setMajor(cursor.getString(cursor.getColumnIndex("major")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        textViewNumber.setText(student.getNumber());
        textViewName.setText(student.getName());
        if ("男".equals(student.getSex())) {
            imageViewSex.setImageResource(R.drawable.male);
        } else if ("女".equals(student.getSex())) {
            imageViewSex.setImageResource(R.drawable.female);
        }
        textViewMajor.setText(student.getMajor());
        String filePhoto = pathPhotos + "/" + student.getNumber() + ".jpg";
        if (new File(filePhoto).exists()) {
            imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(filePhoto));
        } else
            imageViewPhoto.setImageResource(R.drawable.nophoto);
    }

    private void checkDatabaseName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dbName = preferences.getString("course", "eda");
        if (dbName == null || dbName.equals(""))
            dbName = "eda";
        dbName = dbName + ".db";
        if (!dbName.equals(databaseName)){
            databaseName = dbName;
            openDatabase();
        }
    }

    private void openDatabase() {
        if (cursor != null)
            cursor.close();
        if (database != null)
            AssetsDatabaseManager.closeAllDatabase();
        database = databaseManager.getDatabase(databaseName);
        String querySQL = "select * from " + tableName;
        cursor = database.rawQuery(querySQL, null);
        pathPhotos = findStudentsPhotos();
    }
}
