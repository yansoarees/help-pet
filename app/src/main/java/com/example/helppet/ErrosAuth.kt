package com.example.helppet

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun mensagemErroAuth(e: Exception?): String = when (e) {
    is FirebaseNetworkException ->
        "Sem conexão com a internet. Verifique sua rede e tente de novo."
    is FirebaseTooManyRequestsException ->
        "Muitas tentativas seguidas. Aguarde alguns minutos e tente de novo."
    is FirebaseAuthWeakPasswordException ->
        "Senha fraca: use pelo menos 6 caracteres, com letras e números."
    is FirebaseAuthUserCollisionException ->
        "Já existe uma conta com este e-mail. Volte e faça login."
    is FirebaseAuthInvalidUserException -> when (e.errorCode) {
        "ERROR_USER_DISABLED" -> "Esta conta foi desativada."
        else -> "Não existe conta com este e-mail. Toque em \"Cadastre-se\" para criar uma."
    }
    is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
        "ERROR_INVALID_EMAIL" -> "O e-mail digitado não é válido."
        "ERROR_WRONG_PASSWORD" -> "Senha incorreta. Tente de novo."
        else -> "E-mail ou senha incorretos. Confira os dados ou crie uma conta em \"Cadastre-se\"."
    }
    else -> "Algo deu errado. Tente novamente."
}

fun erroSenhaCadastro(senha: String): String? = when {
    senha.isEmpty() -> "Digite uma senha"
    senha.length < 6 -> "A senha precisa ter pelo menos 6 caracteres"
    senha.none { it.isLetter() } -> "A senha precisa ter pelo menos uma letra"
    senha.none { it.isDigit() } -> "A senha precisa ter pelo menos um número"
    else -> null
}
