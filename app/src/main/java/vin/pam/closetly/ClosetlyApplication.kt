package vin.pam.closetly

import android.app.Application
import vin.pam.closetly.core.config.AppConfig
import vin.pam.closetly.di.AppContainer

class ClosetlyApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(config = AppConfig.fromBuildConfig())
    }
}
