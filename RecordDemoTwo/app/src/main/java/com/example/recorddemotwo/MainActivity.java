package com.example.recorddemotwo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.recorddemotwo.act.MyWaveViewOneAct;
import com.example.recorddemotwo.act.RecordRAct;

public class MainActivity extends AppCompatActivity {
    private Button button_j;
    private Button button_t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_j = findViewById(R.id.button_j);
        button_t = findViewById(R.id.button_t);
        button_j.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyWaveViewOneAct.class);
                startActivity(intent);
            }
        });
        button_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intenta = new Intent(MainActivity.this, RecordRAct.class);
                startActivity(intenta);
            }
        });
    }
}
