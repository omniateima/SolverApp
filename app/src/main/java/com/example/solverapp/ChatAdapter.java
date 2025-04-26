package com.example.solverapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
     Context context;
    private ArrayList<DataClass> dataList;

    public ChatAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataClass data = dataList.get(position);
        holder.chatId.setText(String.valueOf(data.getId()));
        if (data.getImage() != null) {
            holder.questions.setImageBitmap(data.getImage());
        } else {
            System.out.println("Error: Image is null for ID: " + data.getId());
        }
        holder.solution.setOnClickListener(v -> {
            Intent intent = new Intent(context, solveActivity.class);
            intent.putExtra("id", data.getId()); // Pass the ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatId;
        ImageView questions;
        Button solution;

        public ViewHolder(View itemView) {
            super(itemView);
            chatId = itemView.findViewById(R.id.chatId);
            questions = itemView.findViewById(R.id.questions);
            solution = itemView.findViewById(R.id.solution);
        }
    }
}
