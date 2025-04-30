package com.example.solverapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.solverapp.DB.DBHelper;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<DataClass> dataList;
    DBHelper db ;
    public HistoryAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db = new DBHelper(context);
                boolean isDeleted = db.deleteData(data.getId());
                if(!isDeleted) {
                    Toast.makeText(context, "Failed to delete data.", Toast.LENGTH_SHORT).show();
                }
                dataList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }

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
        ImageButton deleteBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            chatId = itemView.findViewById(R.id.chatId);
            questions = itemView.findViewById(R.id.questions);
            solution = itemView.findViewById(R.id.solution);
            deleteBtn =itemView.findViewById(R.id.deleteBtn);
        }
    }
}
