# 🐾 HelpPet

O **HelpPet** é o meu projeto desenvolvido em Kotlin para a faculdade. A ideia principal é criar uma rede de apoio na comunidade para facilitar a adoção de animais, ajudar a encontrar pets perdidos e gerir alertas em tempo real.

---

## 🚀 O que o app faz?
* **Login flexível:** Dá para entrar com E-mail e senha, ou de forma rápida usando a conta do Google e da Apple.
* **Mapa interativo:** Mostra a localização de pets perdidos ou disponíveis para adoção por perto usando o Google Maps.
* **Notificações inteligentes:** Quando alguém avisa que encontrou um pet, o sistema avisa diretamente o dono (alerta privado), enquanto os alertas gerais avisam a comunidade.
* **Contato direto:** Botão integrado para abrir o WhatsApp do protetor ou dono do pet num clique.

---

## 🛠️ Tecnologias que utilizei
* **Linguagem:** Kotlin
* **Interface:** Jetpack Compose (100% nativo)
* **Base de dados e Auth:** Firebase (Firestore & Firebase Auth)
* **Mapas:** Google Maps SDK
* **Imagens:** Coil

---

## 📱 Como rodar o projeto
1. Clona o repositório no teu PC.
2. Abre o projeto no **Android Studio**.
3. Certifica-te de que o ficheiro `google-services.json` está na pasta `app/` (com as tuas credenciais do Firebase).
4. Adiciona a chave da API do ImgBB (upload de fotos) no ficheiro `local.properties` na raiz do projeto (este ficheiro não vai para o GitHub):
   ```
   IMGBB_API_KEY=a_tua_chave_aqui
   ```
5. Sincroniza o Gradle e clica em **Run** para testar no emulador ou num telemóvel físico.

---
Feito com 💙 por Yan Gabriel  e Equipe.