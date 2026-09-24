package com.example.helppet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout; // <-- ESTA FOI A LINHA QUE ADICIONEI
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1º BOTÃO: "Perdi um animal"
        CardView btnPerdiAnimal = findViewById(R.id.btn_perdi_animal);
        btnPerdiAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PerdiAnimalActivity.class);
                startActivity(intent);
            }
        });

        // 2º BOTÃO: "Encontrei um animal"
        CardView btnEncontreiAnimal = findViewById(R.id.btn_encontrei_animal);
        btnEncontreiAnimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EncontreiAnimalActivity.class);
                startActivity(intent);
            }
        });

        // 3º BOTÃO: "Quero adotar" (NOVO!)
        CardView btnQueroAdotar = findViewById(R.id.btn_quero_adotar);
        btnQueroAdotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QueroAdotarActivity.class);
                startActivity(intent);
            }
        });

        // =================================================================
        // BOTÃO DA BARRA INFERIOR: "Animais"
        // =================================================================
        LinearLayout navAnimais = findViewById(R.id.nav_animais);
        navAnimais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AnimaisActivity.class);
                startActivity(intent);
            }
        });
    }
}