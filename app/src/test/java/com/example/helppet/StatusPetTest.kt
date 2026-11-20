package com.example.helppet

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusPetTest {

    @Test
    fun `etiqueta no nome define o status`() {
        assertEquals(StatusPet.PERDIDO, statusDoAnimal(Animal(nome = "Bob (Perdido)")))
        assertEquals(StatusPet.ADOCAO, statusDoAnimal(Animal(nome = "Thor (Para Adoção)")))
        assertEquals(StatusPet.ENCONTRADO, statusDoAnimal(Animal(nome = "Gato preto (Está comigo)")))
    }

    @Test
    fun `sem etiqueta, a descricao do formulario de adocao indica adocao`() {
        assertEquals(StatusPet.ADOCAO, statusDoAnimal(Animal(nome = "Bob", descricao = "Saúde: Vacinado. muito dócil")))
    }

    @Test
    fun `sem etiqueta, a descricao do formulario de perdido indica desaparecido`() {
        assertEquals(StatusPet.PERDIDO, statusDoAnimal(Animal(nome = "Mia", descricao = "Visto em: Boa Viagem. Características: coleira")))
    }

    @Test
    fun `sem etiqueta e sem pista na descricao, conta como encontrado`() {
        assertEquals(StatusPet.ENCONTRADO, statusDoAnimal(Animal(nome = "Nick", descricao = "por boa viagem, na orla")))
    }

    @Test
    fun `nome exibido nao mostra a etiqueta`() {
        assertEquals("Bob", nomeDeExibicao(Animal(nome = "Bob (Perdido)")))
    }
}
