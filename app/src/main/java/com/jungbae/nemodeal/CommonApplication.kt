package com.jungbae.nemodeal

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo
import com.google.firebase.messaging.FirebaseMessaging
import com.jungbae.nemodeal.activity.MainActivity
import com.jungbae.nemodeal.preference.PreferenceManager
import okio.internal.commonAsUtf8ToByteArray
import com.jungbae.nemodeal.showToast

import kotlin.properties.Delegates

fun Int.isEqualHigherOreo(): Boolean {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return true
    }
    return false
}

class  CommonApplication : Application() {

    companion object {
        var context: Context by Delegates.notNull()
            private set

        val androidId: String
            get() {
                var androidId = getAdvertisingIdInfo(context).id
                Log.e("@@@","@@@ androidId $androidId")
                return androidId
            }

        lateinit var preferences: PreferenceManager

        fun sendNotification(data: Map<String, String>) {
            val title = data.get("title") as String
            val body = data.get("body") as String
            val link = data.get("link") as String

            val intent = Intent(context, MainActivity::class.java)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("link", link)
            }
            val pendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT)

            val channelId = context.getString(R.string.default_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            @SuppressWarnings("ConstantConditions")
            if (Build.VERSION.SDK_INT.isEqualHigherOreo()) {
                val channel = NotificationChannel(channelId, "네모딜 기본 채널", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        }

        fun subscribeTopic(topic: String) {
            topic.UTF8()?.let {
                FirebaseMessaging.getInstance().subscribeToTopic(it)
                    .addOnCompleteListener {
                        context.showToast(topic + "구독 완료")

                    }
            }
        }

        fun unsubscribeTopic(topic: String) {
            topic.UTF8()?.let {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(it)
                    .addOnCompleteListener {
                        context.showToast(topic + "구독 해지 완료")

                    }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        preferences = PreferenceManager()
        //createNotificationChannel()

        // 에어팟2 -> %EC%97%90%EC%96%B4%ED%8C%9F2

//        var str = ""
//        val bytes = "에어팟".commonAsUtf8ToByteArray()
//        for (b in bytes) {
//            val st = String.format("%02X", b)
//            str += "%" + st
//
//        }
//
//        Log.e("@@@","@@@ str $str")
//        FirebaseMessaging.getInstance().subscribeToTopic("1234")
//            .addOnCompleteListener { task ->
//                var msg = "토픽"
//                if (!task.isSuccessful) {
//                    msg = "토픽 에러"
//                }
//
//                Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
//            }
    }

//    fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notificationChannel =
//                NotificationChannel("네모딜", "네모딜 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
//                    description = "키워드 알림"
//                    enableLights(true)
//                    lightColor = Color.GREEN
//                    enableVibration(true)
//                    vibrationPattern = longArrayOf(100, 200, 100, 200)
//                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//                }
//            notificationManager.createNotificationChannel(notificationChannel)
//        }
//    }


    fun sendBroadcastWith(intent: Intent) {
        applicationContext.startActivity(intent)
    }
}
