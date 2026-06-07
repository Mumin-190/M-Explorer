package me.mumin.android.files.sftpserver

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import me.mumin.android.files.compat.doWithStartForegroundServiceAllowed

@RequiresApi(Build.VERSION_CODES.N)
class SftpServerTileService : TileService() {
    private val observer = Observer<SftpServerService.State> { onSftpServerStateChanged(it) }

    override fun onStartListening() {
        super.onStartListening()

        SftpServerService.stateLiveData.observeForever(observer)
    }

    override fun onStopListening() {
        super.onStopListening()

        SftpServerService.stateLiveData.removeObserver(observer)
    }

    private fun onSftpServerStateChanged(state: SftpServerService.State) {
        val tile = qsTile ?: return
        when (state) {
            SftpServerService.State.STARTING,
            SftpServerService.State.RUNNING -> tile.state = Tile.STATE_ACTIVE
            SftpServerService.State.STOPPING -> tile.state = Tile.STATE_UNAVAILABLE
            SftpServerService.State.STOPPED -> tile.state = Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (isLocked) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        doWithStartForegroundServiceAllowed { SftpServerService.toggle(this) }
    }
}
