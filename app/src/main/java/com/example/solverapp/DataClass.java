package com.example.solverapp;

import android.graphics.Bitmap;

public class DataClass {
    private int id;
    private Bitmap image;
    private String answer;

    public DataClass(int id, Bitmap image, String answer) {
        this.id = id;
        this.image = image;
        this.answer = answer;
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

}
