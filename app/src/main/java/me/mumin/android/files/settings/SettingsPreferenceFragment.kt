/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

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
                        android.widget.Toast.makeText(requireContext(), "Shizuku permission is already granted", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        rikka.shizuku.Shizuku.requestPermission(1001)
                    }
                } else {
                    android.widget.Toast.makeText(requireContext(), "Shizuku is not running", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                android.widget.Toast.makeText(requireContext(), "Error checking Shizuku: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var initialThemeColor: ThemeColor? = null
    private var initialMaterialDesign3: Boolean? = null
    private var initialNightMode: NightMode? = null
    private var initialBlackNightMode: Boolean? = null
    private var initialMaterial3ThemeStyle: ThemeStyle? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewLifecycleOwner = viewLifecycleOwner
        initialThemeColor = Settings.THEME_COLOR.valueCompat
        initialMaterialDesign3 = Settings.MATERIAL_DESIGN_3.valueCompat
        initialNightMode = Settings.NIGHT_MODE.valueCompat
        initialBlackNightMode = Settings.BLACK_NIGHT_MODE.valueCompat
        initialMaterial3ThemeStyle = Settings.MATERIAL3_THEME_STYLE.valueCompat

        Settings.THEME_COLOR.observe(viewLifecycleOwner, this::onThemeColorChanged)
        Settings.MATERIAL_DESIGN_3.observe(viewLifecycleOwner, this::onMaterialDesign3Changed)
        Settings.NIGHT_MODE.observe(viewLifecycleOwner, this::onNightModeChanged)
        Settings.BLACK_NIGHT_MODE.observe(viewLifecycleOwner, this::onBlackNightModeChanged)
        Settings.MATERIAL3_THEME_STYLE.observe(viewLifecycleOwner, this::onMaterial3ThemeStyleChanged)
        updatePreferenceDependencies()
    }

    private fun updatePreferenceDependencies() {
        val isM3 = Settings.MATERIAL_DESIGN_3.valueCompat
        val themeStyle = Settings.MATERIAL3_THEME_STYLE.valueCompat

        val m3StylePref = findPreference<androidx.preference.Preference>(getString(R.string.pref_key_material3_theme_style))
        val themeColorPref = findPreference<androidx.preference.Preference>(getString(R.string.pref_key_theme_color))

        m3StylePref?.isEnabled = isM3
        themeColorPref?.isEnabled = !isM3 || (isM3 && themeStyle == ThemeStyle.CUSTOM)
    }

    private fun onThemeColorChanged(themeColor: ThemeColor) {
        updatePreferenceDependencies()
        if (initialThemeColor != null && initialThemeColor != themeColor) {
            CustomThemeHelper.sync(true)
        }
        initialThemeColor = themeColor
    }

    private fun onMaterialDesign3Changed(isMaterialDesign3: Boolean) {
        updatePreferenceDependencies()
        if (initialMaterialDesign3 != null && initialMaterialDesign3 != isMaterialDesign3) {
            CustomThemeHelper.sync(true)
        }
        initialMaterialDesign3 = isMaterialDesign3
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
