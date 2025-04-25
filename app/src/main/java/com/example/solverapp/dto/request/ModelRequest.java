package com.example.solverapp.dto.request;

import android.util.Log;

import java.util.ArrayList;

public class ModelRequest {
    public String model;
    public ArrayList<Message> messages;
    public int max_tokens;
    public double temperature;
    public double top_p;
    public boolean stream;

    public ModelRequest(String image) {
        ArrayList<Message> list = new ArrayList<>();
        String q = String.format("answer questions in this image <img src=\"data:image/png;base64,%s\" />", image);
        list.add(new Message("system", "You are an image-based question answering assistant. Only respond to questions that can be answered using the content visible in the image. If no question is present in the image, reply with: 'Sorry, I can't see any questions — are you sure you're not the blind one?'"));

        list.add(new Message("user", q));
        this.model = "meta/llama-4-scout-17b-16e-instruct";
        this.max_tokens = 512;
        this.temperature = 1.00;
        this.top_p = 1.00;
        this.stream = false;
        this.messages = list;
    }

}
