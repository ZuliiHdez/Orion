package Orion.message

import android.app.Application
import android.content.SharedPreferences
import java.util.*

class OrionClass : Application() {

    override fun onCreate() {
        super.onCreate()
        applyAppLanguage()
    }

    private fun applyAppLanguage() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val language = sharedPreferences.getString("App_Language", Locale.getDefault().language) ?: "en"

        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
