package Orion.message

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ChangeLanguageActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_language)

        val radioGroup = findViewById<RadioGroup>(R.id.languageRadioGroup)
        var select_lang = findViewById<TextView>(R.id.select_lang)
        val saveButton = findViewById<Button>(R.id.saveLanguageButton)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        select_lang.text = getString(R.string.select_lang)

        // Preseleccionar el idioma actual
        val currentLang = sharedPreferences.getString("App_Language", Locale.getDefault().language)
        when (currentLang) {
            "en" -> radioGroup.check(R.id.radioEnglish)
            "es" -> radioGroup.check(R.id.radioSpanish)
            "ru" -> radioGroup.check(R.id.radioRussian)
            "de" -> radioGroup.check(R.id.radioGerman)
            "pt" -> radioGroup.check(R.id.radioPortuguese)
        }

        saveButton.setOnClickListener {
            val selectedLang = when (radioGroup.checkedRadioButtonId) {
                R.id.radioEnglish -> "en"
                R.id.radioSpanish -> "es"
                R.id.radioRussian -> "ru"
                R.id.radioGerman -> "de"
                R.id.radioPortuguese -> "pt"
                else -> Locale.getDefault().language
            }

            // Guardar configuración
            val editor = sharedPreferences.edit()
            editor.putString("App_Language", selectedLang)
            editor.apply()

            // Cambiar idioma
            setLocale(selectedLang)

            // Reiniciar actividad para aplicar cambios
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
