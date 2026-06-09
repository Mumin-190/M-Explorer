package me.mumin.android.files.sftpserver

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import me.mumin.android.files.databinding.ServerFragmentBinding
import me.mumin.android.files.ftpserver.FtpServerActivity
import me.mumin.android.files.ftpserver.FtpServerService
import me.mumin.android.files.ftpserver.FtpServerUrl
import me.mumin.android.files.util.valueCompat
import me.mumin.android.files.storage.AddStorageDialogActivity
import me.mumin.android.files.storage.AddLanSmbServerActivity
import me.mumin.android.files.storage.EditSftpServerActivity
import me.mumin.android.files.storage.EditSftpServerFragment
import me.mumin.android.files.storage.EditFtpServerActivity
import me.mumin.android.files.storage.EditFtpServerFragment
import me.mumin.android.files.util.createIntent
import me.mumin.android.files.util.putArgs
import me.mumin.android.files.util.startActivitySafe

class ServerFragment : Fragment() {
    private lateinit var binding: ServerFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ServerFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFtpServer()
        setupSftpServer()
        setupQuickActions()
    }

    private fun setupFtpServer() {
        // Observe FTP server state
        FtpServerService.stateLiveData.observe(viewLifecycleOwner) { state ->
            val context = requireContext()
            when (state) {
                FtpServerService.State.RUNNING -> {
                    binding.ftpSwitch.isEnabled = true
                    binding.ftpSwitch.isChecked = true
                    binding.ftpStatusText.text = "Status: Running"
                    val url = FtpServerUrl.getUrl()
                    if (url != null) {
                        binding.ftpUrlLayout.visibility = View.VISIBLE
                        binding.ftpUrlText.text = url
                    } else {
                        binding.ftpUrlLayout.visibility = View.GONE
                    }
                }
                FtpServerService.State.STARTING -> {
                    binding.ftpSwitch.isEnabled = false
                    binding.ftpSwitch.isChecked = true
                    binding.ftpStatusText.text = "Status: Starting…"
                    binding.ftpUrlLayout.visibility = View.GONE
                }
                FtpServerService.State.STOPPING -> {
                    binding.ftpSwitch.isEnabled = false
                    binding.ftpSwitch.isChecked = false
                    binding.ftpStatusText.text = "Status: Stopping…"
                    binding.ftpUrlLayout.visibility = View.GONE
                }
                FtpServerService.State.STOPPED, null -> {
                    binding.ftpSwitch.isEnabled = true
                    binding.ftpSwitch.isChecked = false
                    binding.ftpStatusText.text = "Status: Stopped"
                    binding.ftpUrlLayout.visibility = View.GONE
                }
            }
        }

        // Toggle on switch click
        binding.ftpSwitch.setOnClickListener {
            FtpServerService.toggle(requireContext())
        }

        // Copy button
        binding.ftpCopyButton.setOnClickListener {
            val url = binding.ftpUrlText.text.toString()
            copyToClipboard("FTP URL", url)
        }

        // Settings button
        binding.ftpSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), FtpServerActivity::class.java))
        }
    }

    private fun setupSftpServer() {
        // Observe SFTP server state
        SftpServerService.stateLiveData.observe(viewLifecycleOwner) { state ->
            val context = requireContext()
            when (state) {
                SftpServerService.State.RUNNING -> {
                    binding.sftpSwitch.isEnabled = true
                    binding.sftpSwitch.isChecked = true
                    binding.sftpStatusText.text = "Status: Running"
                    val url = SftpServerUrl.getUrl()
                    if (url != null) {
                        binding.sftpUrlLayout.visibility = View.VISIBLE
                        binding.sftpUrlText.text = url
                    } else {
                        binding.sftpUrlLayout.visibility = View.GONE
                    }
                }
                SftpServerService.State.STARTING -> {
                    binding.sftpSwitch.isEnabled = false
                    binding.sftpSwitch.isChecked = true
                    binding.sftpStatusText.text = "Status: Starting…"
                    binding.sftpUrlLayout.visibility = View.GONE
                }
                SftpServerService.State.STOPPING -> {
                    binding.sftpSwitch.isEnabled = false
                    binding.sftpSwitch.isChecked = false
                    binding.sftpStatusText.text = "Status: Stopping…"
                    binding.sftpUrlLayout.visibility = View.GONE
                }
                SftpServerService.State.STOPPED, null -> {
                    binding.sftpSwitch.isEnabled = true
                    binding.sftpSwitch.isChecked = false
                    binding.sftpStatusText.text = "Status: Stopped"
                    binding.sftpUrlLayout.visibility = View.GONE
                }
            }
        }

        // Toggle on switch click
        binding.sftpSwitch.setOnClickListener {
            SftpServerService.toggle(requireContext())
        }

        // Copy button
        binding.sftpCopyButton.setOnClickListener {
            val url = binding.sftpUrlText.text.toString()
            copyToClipboard("SFTP URL", url)
        }

        // Settings button
        binding.sftpSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SftpServerActivity::class.java))
        }
    }

    private fun setupQuickActions() {
        binding.actionAddServer.setOnClickListener {
            startActivitySafe(AddStorageDialogActivity::class.createIntent())
        }
        binding.actionScanNetwork.setOnClickListener {
            startActivitySafe(AddLanSmbServerActivity::class.createIntent())
        }
        binding.actionSshConnection.setOnClickListener {
            startActivitySafe(EditSftpServerActivity::class.createIntent().putArgs(EditSftpServerFragment.Args()))
        }
        binding.actionSmbConnection.setOnClickListener {
            startActivitySafe(AddLanSmbServerActivity::class.createIntent())
        }
        binding.actionFtpConnection.setOnClickListener {
            startActivitySafe(EditFtpServerActivity::class.createIntent().putArgs(EditFtpServerFragment.Args()))
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
