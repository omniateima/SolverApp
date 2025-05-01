package com.example.solverapp;

import android.graphics.Bitmap;

public class DataClass {
    private int id;
    private Bitmap image;


    public DataClass(int id, Bitmap image) {
        this.id = id;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

}
