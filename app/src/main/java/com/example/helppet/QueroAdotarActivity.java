package com.example.helppet;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class QueroAdotarActivity extends AppCompatActivity {

    private TextView tvEspecie, tvIdade;
    private ActivityResultLauncher<Intent> pegarFotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quero_adotar);

        ImageView btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvEspecie = findViewById(R.id.tv_especie);
        tvEspecie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpcoesEspecie();
            }
        });

        tvIdade = findViewById(R.id.tv_idade);
        tvIdade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarOpcoesIdade();
            }
        });

        pegarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Toast.makeText(this, "Foto selecionada com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        LinearLayout boxFoto = findViewById(R.id.box_foto);
        boxFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pegarFotoLauncher.launch(intent);
            }
        });
    }

    private void mostrarOpcoesEspecie() {
        String[] opcoes = {"Cachorro", "Gato", "Pássaro", "Outros"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione a Espécie");
        builder.setItems(opcoes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int posicaoEscolhida) {
                tvEspecie.setText(opcoes[posicaoEscolhida]);
                tvEspecie.setTextColor(Color.parseColor("#1C2723"));
            }
        });
        builder.show();
    }

    private void mostrarOpcoesIdade() {
        String[] opcoes = {"Filhote (até 1 ano)", "Jovem (1 a 3 anos)", "Adulto (3 a 7 anos)", "Idoso (mais de 7 anos)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione a Idade");
        builder.setItems(opcoes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int posicaoEscolhida) {
                tvIdade.setText(opcoes[posicaoEscolhida]);
                tvIdade.setTextColor(Color.parseColor("#1C2723"));
            }
        });
        builder.show();
    }
}