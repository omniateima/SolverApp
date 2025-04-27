package com.example.solverapp.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class DBHelper extends SQLiteOpenHelper {
    byte[] imageInBytes;
    public DBHelper( Context context) {
        super(context, "history.db", null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table History(id INTEGER primary key autoincrement,image Blob,answer TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("Drop table if exists History");
    }

    public boolean insertData(Bitmap image, String answer) {
        SQLiteDatabase db =getWritableDatabase();
        ByteArrayOutputStream objectByteOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG,100,objectByteOutputStream);
        imageInBytes = objectByteOutputStream.toByteArray();

        ContentValues values= new ContentValues();
        values.put("image",imageInBytes);
        values.put("answer", answer);
        long result = db.insert("History",null,values);
        return result >=0;
    }

    public boolean deleteData(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete("History", "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public Cursor getSingleData(int id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM History WHERE id = ?", new String[]{String.valueOf(id)});
    }


    public Cursor getAllDataCursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM History ORDER BY id DESC", null);
    }

}
