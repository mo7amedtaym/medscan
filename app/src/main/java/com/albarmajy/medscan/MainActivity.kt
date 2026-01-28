package com.albarmajy.medscan

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.albarmajy.medscan.data.local.worker.DoseSystemWorker
import com.albarmajy.medscan.di.appModule
import com.albarmajy.medscan.notification.NotificationHelper
import com.albarmajy.medscan.ui.navigation.NavigationRoot
import org.koin.androidx.workmanager.koin.workManagerFactory
import com.albarmajy.medscan.ui.theme.MedScanTheme
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(applicationContext, "notifications not work", Toast.LENGTH_SHORT).show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                PrimaryBlue.hashCode(),
                PrimaryBlue.hashCode(),
            )
        )
        setContent {
            MedScanTheme {
                NavigationRoot()
            }

        }
    }

}


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        startKoin {
            androidContext(this@BaseApplication)
            workManagerFactory()
            modules(appModule)
        }
        setupRecurringWork()
    }
    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<DoseSystemWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // إرسال المهمة للنظام
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DoseRenewalWork",
            ExistingPeriodicWorkPolicy.KEEP, // إذا كانت المهمة موجودة لا تحذفها (تحافظ على التوقيت الأصلي)
            workRequest
        )
    }


}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MedScanTheme {
        Greeting("Android")
    }
}