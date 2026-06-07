package me.mumin.android.files.sftpserver

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.lifecycle.Observer
import androidx.preference.SwitchPreferenceCompat

class SftpServerStatePreference : SwitchPreferenceCompat {
    private val observer = Observer<SftpServerService.State> { onStateChanged(it) }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isPersistent = false
    }

    override fun onAttached() {
        super.onAttached()
        SftpServerService.stateLiveData.observeForever(observer)
    }

    override fun onDetached() {
        super.onDetached()
        SftpServerService.stateLiveData.removeObserver(observer)
    }

    private fun onStateChanged(state: SftpServerService.State) {
        val summaryText = when (state) {
            SftpServerService.State.STARTING -> "Starting..."
            SftpServerService.State.RUNNING -> "Running"
            SftpServerService.State.STOPPING -> "Stopping..."
            SftpServerService.State.STOPPED -> "Stopped"
        }
        summary = summaryText
        isChecked = state == SftpServerService.State.STARTING || state == SftpServerService.State.RUNNING
        isEnabled = !(state == SftpServerService.State.STARTING || state == SftpServerService.State.STOPPING)
    }

    override fun onClick() {
        SftpServerService.toggle(context)
    }
}
