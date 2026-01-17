package com.albarmajy.medscan

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.albarmajy.medscan.di.appModule
import com.albarmajy.medscan.ui.navigation.NavigationRoot
import com.albarmajy.medscan.ui.screens.MainAppEntryPoint
import com.albarmajy.medscan.ui.theme.MedScanTheme
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                PrimaryBlue.hashCode(),
                PrimaryBlue.hashCode(),
            )
        )
        setContent {
            MedScanTheme {
//              MainAppEntryPoint()
                NavigationRoot()
            }

        }
    }
}


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApplication)
            modules(appModule)
        }
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