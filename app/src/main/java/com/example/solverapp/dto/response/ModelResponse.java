package com.example.solverapp.dto.response;

import java.util.ArrayList;

public class ModelResponse {
    public ArrayList<Choice> choices;
    public int created;
    public String id;
    public String model;
    public String object;
    public Object prompt_logprobs;
    public Usage usage;

    public String getMessage(){
        return this.choices.get(0).message.content;
    }
}
