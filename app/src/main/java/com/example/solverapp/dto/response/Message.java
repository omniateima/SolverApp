package com.example.solverapp.dto.response;

import java.util.ArrayList;

public class Message {
    public String content;
    public Object reasoning_content;
    public String role;
    public ArrayList<Object> tool_calls;
}
