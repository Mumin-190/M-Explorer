package me.mumin.android.files.app

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import me.mumin.android.files.R
import me.mumin.android.files.compat.getColorCompat
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.theme.custom.CustomThemeHelper
import me.mumin.android.files.theme.custom.ThemeStyle
import me.mumin.android.files.theme.night.NightModeHelper
import me.mumin.android.files.util.valueCompat

abstract class AppActivity : AppCompatActivity() {
    private var isDelegateCreated = false

    override fun getDelegate(): AppCompatDelegate {
        val delegate = super.getDelegate()

        if (!isDelegateCreated) {
            isDelegateCreated = true
            NightModeHelper.apply(this)
        }
        return delegate
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CustomThemeHelper.apply(this)

        val themeStyle = Settings.MATERIAL3_THEME_STYLE.valueCompat
        when (themeStyle) {
            ThemeStyle.DYNAMIC -> {
                DynamicColors.applyToActivityIfAvailable(this)
            }
            ThemeStyle.MONOCHROME -> {
                val options = DynamicColorsOptions.Builder()
                    .setContentBasedSource(Color.GRAY)
                    .build()
                DynamicColors.applyToActivityIfAvailable(this, options)
            }
            ThemeStyle.CUSTOM -> {
                val themeColor = Settings.THEME_COLOR.valueCompat
                val colorInt = getColorCompat(themeColor.resourceId)
                val options = DynamicColorsOptions.Builder()
                    .setContentBasedSource(colorInt)
                    .build()
                DynamicColors.applyToActivityIfAvailable(this, options)
            }
        }

        if (Settings.BLACK_NIGHT_MODE.valueCompat && NightModeHelper.isInNightMode(this)) {
            theme.applyStyle(R.style.ThemeOverlay_MaterialFiles_Black, true)
        }

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }
}
