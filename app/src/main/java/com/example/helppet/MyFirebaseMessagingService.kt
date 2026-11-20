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


        val titulo = remoteMessage.notification?.title ?: "Novo Alerta HelpPet"
        val mensagem = remoteMessage.notification?.body ?: "Há um novo animal na sua região!"


        mostrarNotificacao(titulo, mensagem)
    }

    private fun mostrarNotificacao(titulo: String, mensagem: String) {
        val channelId = "helppet_canal_alertas"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                "Alertas HelpPet",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(canal)
        }


        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)


        val notificacaoId = Random.nextInt()
        notificationManager.notify(notificacaoId, builder.build())
    }
}