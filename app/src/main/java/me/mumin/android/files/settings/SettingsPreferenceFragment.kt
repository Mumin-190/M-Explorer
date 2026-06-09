package me.mumin.android.files.settings

import android.os.Build
import android.os.Bundle
import me.mumin.android.files.R
import me.mumin.android.files.theme.custom.CustomThemeHelper
import me.mumin.android.files.theme.custom.ThemeColor
import me.mumin.android.files.theme.custom.ThemeStyle
import me.mumin.android.files.theme.night.NightMode
import me.mumin.android.files.theme.night.NightModeHelper
import me.mumin.android.files.ui.PreferenceFragmentCompat
import me.mumin.android.files.util.valueCompat

class SettingsPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var localePreference: LocalePreference

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        localePreference = preferenceScreen.findPreference(getString(R.string.pref_key_locale))!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            localePreference.setApplicationLocalesPre33 = { locales ->
                val activity = requireActivity() as SettingsActivity
                activity.setApplicationLocalesPre33(locales)
            }
        }

        findPreference<androidx.preference.Preference>("key_request_shizuku_permission")?.setOnPreferenceClickListener {
            requestShizukuPermissionProgrammatically()
            true
        }
    }

    private fun requestShizukuPermissionProgrammatically() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                if (rikka.shizuku.Shizuku.pingBinder()) {
                    if (rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        Settings.ROOT_STRATEGY.putValue(me.mumin.android.files.provider.root.RootStrategy.ALWAYS)
                        android.widget.Toast.makeText(requireContext(), "Shizuku permission is already granted. Root access enabled.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val listener = object : rikka.shizuku.Shizuku.OnRequestPermissionResultListener {
                            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                                rikka.shizuku.Shizuku.removeRequestPermissionResultListener(this)
                                if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    Settings.ROOT_STRATEGY.putValue(me.mumin.android.files.provider.root.RootStrategy.ALWAYS)
                                    android.widget.Toast.makeText(requireContext(), "Shizuku authorization succeeded. Root access enabled.", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(requireContext(), "Shizuku permission denied by user", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        rikka.shizuku.Shizuku.addRequestPermissionResultListener(listener)
                        rikka.shizuku.Shizuku.requestPermission(1001)
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(), "Shizuku service is not running. Please start the Shizuku app first.", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                android.widget.Toast.makeText(requireContext(), "Error checking Shizuku: ${e.localizedMessage ?: e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(requireContext(), "Shizuku requires Android 6.0+", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private var initialThemeColor: ThemeColor? = null
    private var initialNightMode: NightMode? = null
    private var initialBlackNightMode: Boolean? = null
    private var initialMaterial3ThemeStyle: ThemeStyle? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewLifecycleOwner = viewLifecycleOwner
        initialThemeColor = Settings.THEME_COLOR.valueCompat
        initialNightMode = Settings.NIGHT_MODE.valueCompat
        initialBlackNightMode = Settings.BLACK_NIGHT_MODE.valueCompat
        initialMaterial3ThemeStyle = Settings.MATERIAL3_THEME_STYLE.valueCompat

        Settings.THEME_COLOR.observe(viewLifecycleOwner, this::onThemeColorChanged)
        Settings.NIGHT_MODE.observe(viewLifecycleOwner, this::onNightModeChanged)
        Settings.BLACK_NIGHT_MODE.observe(viewLifecycleOwner, this::onBlackNightModeChanged)
        Settings.MATERIAL3_THEME_STYLE.observe(viewLifecycleOwner, this::onMaterial3ThemeStyleChanged)
        updatePreferenceDependencies()
    }

    private fun updatePreferenceDependencies() {
        val themeStyle = Settings.MATERIAL3_THEME_STYLE.valueCompat

        val m3StylePref = findPreference<androidx.preference.Preference>(getString(R.string.pref_key_material3_theme_style))
        val themeColorPref = findPreference<androidx.preference.Preference>(getString(R.string.pref_key_theme_color))

        m3StylePref?.isEnabled = true
        themeColorPref?.isEnabled = themeStyle == ThemeStyle.CUSTOM
    }

    private fun onThemeColorChanged(themeColor: ThemeColor) {
        updatePreferenceDependencies()
        if (initialThemeColor != null && initialThemeColor != themeColor) {
            CustomThemeHelper.sync(true)
        }
        initialThemeColor = themeColor
    }

    private fun onMaterial3ThemeStyleChanged(themeStyle: ThemeStyle) {
        updatePreferenceDependencies()
        if (initialMaterial3ThemeStyle != null && initialMaterial3ThemeStyle != themeStyle) {
            CustomThemeHelper.sync(true)
        }
        initialMaterial3ThemeStyle = themeStyle
    }

    private fun onNightModeChanged(nightMode: NightMode) {
        if (initialNightMode != null && initialNightMode != nightMode) {
            NightModeHelper.sync()
        }
        initialNightMode = nightMode
    }

    private fun onBlackNightModeChanged(blackNightMode: Boolean) {
        if (initialBlackNightMode != null && initialBlackNightMode != blackNightMode) {
            CustomThemeHelper.sync(true)
        }
        initialBlackNightMode = blackNightMode
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Refresh locale preference summary because we aren't notified for an external change
            // between system default and the locale that's the current system default.
            localePreference.notifyChanged()
        }
    }
}
