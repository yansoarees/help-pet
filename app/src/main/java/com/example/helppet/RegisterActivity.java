package com.example.helppet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPhone, edtPassword;
    private Button btnRegister;
    private TextView txtBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> validateAndRegister());

        // Botão para voltar ao login
        txtBackToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndRegister() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (name.isEmpty()) {
            edtName.setError("Insira o seu nome completo");
            edtName.requestFocus();
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Insira um e-mail válido");
            edtEmail.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() < 8) {
            edtPhone.setError("Insira um número de telefone válido");
            edtPhone.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            edtPassword.setError("A senha deve ter pelo menos 6 caracteres");
            edtPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
        finish(); // Fecha o cadastro e volta para o login
    }
}