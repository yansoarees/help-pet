package com.example.helppet

object FormValidator {


    fun validarNomePet(nome: String): Boolean {
        return nome.trim().isNotEmpty() && nome.trim().length >= 2
    }


    fun validarTelefone(telefone: String): Boolean {
        val apenasNumeros = telefone.filter { it.isDigit() }
        return apenasNumeros.length >= 9
    }
}