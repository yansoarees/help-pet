package com.example.helppet

object FormValidator {

    // O nome do animal não pode estar vazio e deve ter pelo menos 2 letras
    fun validarNomePet(nome: String): Boolean {
        return nome.trim().isNotEmpty() && nome.trim().length >= 2
    }

    // O telefone tem de ter pelo menos 9 dígitos (para garantir que é um número válido no Brasil)
    fun validarTelefone(telefone: String): Boolean {
        val apenasNumeros = telefone.filter { it.isDigit() }
        return apenasNumeros.length >= 9
    }
}