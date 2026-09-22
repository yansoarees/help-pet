package com.example.helppet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Carrega exatamente o desenho XML que criámos para o Dashboard
        setContentView(R.layout.activity_main);
    }
}