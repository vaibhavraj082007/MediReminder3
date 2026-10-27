package com.example.medireminder3;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class CaregiverActivity extends AppCompatActivity {

    EditText etCode;
    ListView listStatus;
    ArrayList<String> lines = new ArrayList<>();
    ArrayAdapter<String> adapter;
    DatabaseReference ref;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etCode = findViewById(R.id.etCode);
        listStatus = findViewById(R.id.listStatus);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines);
        listStatus.setAdapter(adapter);

        findViewById(R.id.btnWatch).setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter the patient's code", Toast.LENGTH_SHORT).show();
                return;
            }
            startWatching(code);
        });
    }

    private void startWatching(String code) {
        stopWatching();
        ref = FirebaseDatabase.getInstance().getReference("patients")
                .child(code).child("status");

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lines.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    lines.add(String.valueOf(s.getValue()));
                }
                if (lines.isEmpty()) lines.add("No data yet for this code");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CaregiverActivity.this, error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
        ref.addValueEventListener(listener);
    }

    private void stopWatching() {
        if (ref != null && listener != null) ref.removeEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWatching();
    }
}