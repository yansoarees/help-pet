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

    @Test
    fun `celular com DDD e formatado com 5 digitos antes do traco`() {
        assertEquals("(81) 99999-8888", formatarTelefone("81999998888"))
    }

    @Test
    fun `fixo com DDD e formatado com 4 digitos antes do traco`() {
        assertEquals("(81) 3333-4444", formatarTelefone("8133334444"))
    }

    @Test
    fun `telefone sendo digitado e formatado aos poucos`() {
        assertEquals("", formatarTelefone(""))
        assertEquals("(8", formatarTelefone("8"))
        assertEquals("(81) 9", formatarTelefone("819"))
        assertEquals("(81) 9999-9", formatarTelefone("8199999"))
    }
}
