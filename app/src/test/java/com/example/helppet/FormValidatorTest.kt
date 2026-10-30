package com.example.helppet

import org.junit.Assert.assertEquals
import org.junit.Test

class FormValidatorTest {

    @Test
    fun `quando o nome esta vazio, deve retornar falso`() {
        val resultado = FormValidator.validarNomePet("")
        assertEquals(false, resultado)
    }

    @Test
    fun `quando o nome tem apenas um espaco, deve retornar falso`() {
        val resultado = FormValidator.validarNomePet("   ")
        assertEquals(false, resultado)
    }

    @Test
    fun `quando o nome e valido, deve retornar verdadeiro`() {
        val resultado = FormValidator.validarNomePet("Rex")
        assertEquals(true, resultado)
    }

    @Test
    fun `quando o telefone tem menos de 9 digitos, deve retornar falso`() {
        val resultado = FormValidator.validarTelefone("12345")
        assertEquals(false, resultado)
    }

    @Test
    fun `quando o telefone e valido, deve retornar verdadeiro`() {
        val resultado = FormValidator.validarTelefone("81999999999")
        assertEquals(true, resultado)
    }
}