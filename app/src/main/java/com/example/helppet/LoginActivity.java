package com.example.helppet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private Button btnLogin;
    // As variáveis abaixo estão desativadas para não dar erro no novo design sem botões sociais
    // private ImageButton btnGoogle, btnApple, btnBiometry;
    private TextView txtSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupSignUpText();
        setupListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        // Desativados para o novo design:
        // btnGoogle = findViewById(R.id.btnGoogle);
        // btnApple = findViewById(R.id.btnApple);
        // btnBiometry = findViewById(R.id.btnBiometry);
    }

    private void setupSignUpText() {
        String prompt = getString(R.string.no_account_prompt);
        String signUp = getString(R.string.sign_up);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(prompt);

        int start = builder.length();
        builder.append(signUp);
        int end = builder.length();

        int primaryColor = ContextCompat.getColor(this, R.color.primary_green);

        builder.setSpan(new ForegroundColorSpan(primaryColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Aqui está a mudança: agora abre o ecrã de cadastro em vez de mostrar apenas o Toast
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        builder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtSignUp.setText(builder);
        txtSignUp.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txtSignUp.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> validateAndLogin());

        // Cliques desativados para o novo design:
        // btnGoogle.setOnClickListener(v ->
        //         Toast.makeText(this, "Login com Google", Toast.LENGTH_SHORT).show());

        // btnApple.setOnClickListener(v ->
        //         Toast.makeText(this, "Login com Apple", Toast.LENGTH_SHORT).show());

        // btnBiometry.setOnClickListener(v ->
        //         Toast.makeText(this, "Autenticação biométrica", Toast.LENGTH_SHORT).show());
    }

    private void validateAndLogin() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.error_invalid_email));
            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError(getString(R.string.error_empty_password));
            edtPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

        // Abre a Tela Principal e fecha o Login
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}