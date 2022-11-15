package com.example.drawingactivity

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import dagger.*
import javax.inject.Scope


@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(activity: DrawActivity)
}

@Module
class ApplicationModule {

    @Provides
    fun providesDialogUtil(): DialogUtils {
        return object : DialogUtils() {}
    }
}


class MyApplication : Application() {
    // Reference to Application Dagger Graph
    val applicationComponent = DaggerApplicationComponent.create()
}