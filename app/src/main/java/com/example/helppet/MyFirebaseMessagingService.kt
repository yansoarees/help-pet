package com.example.helppet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Apanhar o título e a mensagem que vêm do Firebase
        val titulo = remoteMessage.notification?.title ?: "Novo Alerta HelpPet"
        val mensagem = remoteMessage.notification?.body ?: "Há um novo animal na tua região!"

        // 2. Chamar a função que desenha a notificação no ecrã
        mostrarNotificacao(titulo, mensagem)
    }

    private fun mostrarNotificacao(titulo: String, mensagem: String) {
        val channelId = "helppet_canal_alertas"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Os telemóveis mais recentes (Android 8+) obrigam a criar um "Canal" de notificações
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                "Alertas HelpPet",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(canal)
        }

        // Construir o design do balão
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Vai usar o ícone padrão da tua app
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Faz a notificação saltar no ecrã
            .setAutoCancel(true) // Desaparece quando a pessoa clica

        // Gera um número aleatório para que as notificações não se sobreponham umas às outras
        val notificacaoId = Random.nextInt()
        notificationManager.notify(notificacaoId, builder.build())
    }
}