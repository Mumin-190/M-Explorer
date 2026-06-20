package me.mumin.android.files.filelist

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java8.nio.file.Path
import java8.nio.file.Paths
import me.mumin.android.files.util.extraPath
import me.mumin.android.files.util.valueCompat
import androidx.lifecycle.LifecycleOwner
import me.mumin.android.files.navigation.NavigationFragment
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mumin.android.files.R
import me.mumin.android.files.databinding.HomeFragmentBinding
import me.mumin.android.files.databinding.StorageCardItemBinding
import me.mumin.android.files.databinding.ServerDeviceItemBinding
import me.mumin.android.files.file.JavaFile
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.asMimeTypeOrNull
import me.mumin.android.files.file.asFileSize
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.settings.SettingsActivity
import me.mumin.android.files.about.AboutActivity
import me.mumin.android.files.storage.Storage
import me.mumin.android.files.storage.ServerStatusManager
import me.mumin.android.files.storage.FtpServerAuthenticator
import me.mumin.android.files.storage.SftpServerAuthenticator
import me.mumin.android.files.storage.SmbServerAuthenticator
import me.mumin.android.files.storage.WebDavServerAuthenticator
import me.mumin.android.files.provider.common.newDirectoryStream
import java.io.File
import java.io.IOException
import me.mumin.android.files.provider.common.WalkFileTreeSearchable
import me.mumin.android.files.ui.DrawerLayoutOnBackPressedCallback
import me.mumin.android.files.util.addOnBackPressedCallback

class HomeFragment : Fragment(), NavigationFragment.Listener {
    private lateinit var binding: HomeFragmentBinding
    private lateinit var navigationFragment: NavigationFragment

    private var downloadsObserver: android.os.FileObserver? = null
    private var statsJob: kotlinx.coroutines.Job? = null

    private val mediaStoreObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: android.net.Uri?) {
            super.onChange(selfChange, uri)
            scheduleCategoryStatsLoad()
        }
    }

    private fun scheduleCategoryStatsLoad() {
        if (!isAdded) return
        statsJob?.cancel()
        statsJob = viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            loadCategoryStats()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupStorages()
            loadRecentFiles()
            loadCategoryStats()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        HomeFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        setupSearchAndSettings()
        setupHamburgerMenu()
        setupCategories()
        loadCachedCategoryStats()
        setupStorages()
        setupNavigationDrawer(savedInstanceState)

        // Setup MediaStore ContentObserver and Downloads FileObserver
        try {
            requireContext().contentResolver.registerContentObserver(
                android.provider.MediaStore.Files.getContentUri("external"),
                true,
                mediaStoreObserver
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setupDownloadsObserver()
    }

    override fun onResume() {
        super.onResume()
        setupStorages() // Refresh Root storage availability status on resume
        loadCategoryStats()
        loadRecentFiles()
    }

    private fun setupHamburgerMenu() {
        binding.homeMenuButton.setOnClickListener {
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeMenuButton)
            val menu = popup.menu

            menu.add(0, 1, 0, "Settings")

            val hiddenFilesItem = menu.add(0, 3, 1, "Hidden Files")
            hiddenFilesItem.isCheckable = true
            hiddenFilesItem.isChecked = Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat

            menu.add(0, 5, 2, "Storage Analysis")
            menu.add(0, 6, 3, "About")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        startActivity(Intent(requireContext(), SettingsActivity::class.java))
                        true
                    }
                    3 -> {
                        val newValue = !Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat
                        Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(newValue)
                        item.isChecked = newValue
                        true
                    }
                    5 -> {
                        val intent = Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(requireContext(), "Unable to open storage settings", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        true
                    }
                    6 -> {
                        startActivity(Intent(requireContext(), AboutActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private lateinit var searchAdapter: SearchResultsAdapter

    private fun setupSearchAndSettings() {
        binding.searchBarCard.setOnClickListener(null)
        binding.searchBarCard.isClickable = false

        binding.searchMenuIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        binding.btnRecentViewAll.setOnClickListener {
            val activity = requireActivity() as FileListActivity
            val extPath = Environment.getExternalStorageDirectory().absolutePath
            activity.navigateToPath(Paths.get(extPath))
        }

        searchAdapter = SearchResultsAdapter(requireContext(), emptyList()) { file, mimeType ->
            val intent = OpenFileActivity.createIntent(Paths.get(file.path), mimeType)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.searchResultsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecycler.adapter = searchAdapter

        binding.searchClearIcon.setOnClickListener {
            binding.searchInput.setText("")
            hideKeyboard()
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) {
                    binding.searchClearIcon.visibility = View.VISIBLE
                    binding.homeContentLayout.visibility = View.GONE
                    binding.searchResultsRecycler.visibility = View.VISIBLE
                    performLocalSearch(query)
                } else {
                    binding.searchClearIcon.visibility = View.GONE
                    binding.homeContentLayout.visibility = View.VISIBLE
                    binding.searchResultsRecycler.visibility = View.GONE
                    searchAdapter.updateData(emptyList())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun performLocalSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) { querySearchFiles(query) }
            if (binding.searchInput.text.toString().trim() == query) {
                searchAdapter.updateData(results)
            }
        }
    }

    private class LimitReachedException : IOException()

    private fun querySearchFiles(query: String): List<RecentFile> {
        val list = mutableListOf<RecentFile>()
        val extPath = Environment.getExternalStorageDirectory().absolutePath
        val rootPath = Paths.get(extPath)
        try {
            WalkFileTreeSearchable.search(rootPath, query, 50) { paths ->
                for (path in paths) {
                    val file = File(path.toString())
                    if (file.isFile && !file.absolutePath.contains("/Android/data")) {
                        val name = file.name
                        val size = file.length()
                        val lastModified = file.lastModified() / 1000
                        val uri = android.net.Uri.fromFile(file)
                        list.add(RecentFile(path.hashCode().toLong(), name, file.absolutePath, size, lastModified, uri))
                        if (list.size >= 50) {
                            throw LimitReachedException()
                        }
                    }
                }
            }
        } catch (e: LimitReachedException) {
            // Normal termination
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun hideKeyboard() {
        val view = view ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupCategories() {
        val extPath = Environment.getExternalStorageDirectory().absolutePath

        binding.catDownloads.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "Downloads")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath, Environment.DIRECTORY_DOWNLOADS), intent)
        }
        binding.catImages.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "Images")
                putExtra("search_query", "exts:.jpg,.jpeg,.png,.webp,.gif,.bmp")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath), intent)
        }
        binding.catVideos.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "Videos")
                putExtra("search_query", "exts:.mp4,.mkv,.webm,.avi,.3gp,.mov")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath), intent)
        }
        binding.catAudio.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "Audio")
                putExtra("search_query", "exts:.mp3,.wav,.ogg,.m4a,.flac,.aac")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath), intent)
        }
        binding.catDocuments.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "Documents")
                putExtra("search_query", "exts:.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.rtf,.odt")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath), intent)
        }
        binding.catApks.setOnClickListener {
            val intent = Intent().apply {
                putExtra("custom_title", "APKs")
                putExtra("search_query", "exts:.apk")
            }
            val activity = requireActivity() as FileListActivity
            activity.navigateToPath(Paths.get(extPath), intent)
        }
    }

    private fun setupStorages() {
        val context = requireContext()
        val extPath = Environment.getExternalStorageDirectory().absolutePath

        // Internal Storage
        try {
            val total = JavaFile.getTotalSpace(extPath)
            val free = JavaFile.getFreeSpace(extPath)
            val used = total - free
            val pct = if (total > 0) (used * 100 / total).toInt() else 0

            binding.storageInternalProgress.progress = pct
            binding.storageInternalSpace.text = "${used.asFileSize().formatHumanReadable(context)} used of ${total.asFileSize().formatHumanReadable(context)}"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.storageInternalSpace.text = "Internal storage details unavailable"
        }

        binding.storageInternal.setOnClickListener {
            navigateToRoot(Paths.get(extPath))
        }

        // Root Storage
        Settings.ENABLE_ROOT_ACCESS.observe(viewLifecycleOwner) { isRootEnabled ->
            if (isRootEnabled) {
                binding.storageRoot.visibility = View.VISIBLE
                val accessible = isRootAccessible()
                if (accessible) {
                    binding.storageRootTitle.text = "Root"
                    try {
                        val rootPath = "/"
                        val total = JavaFile.getTotalSpace(rootPath).let { 
                            if (it > 0) it else {
                                val systemPath = Environment.getRootDirectory().path
                                JavaFile.getTotalSpace(systemPath)
                            }
                        }
                        val free = JavaFile.getFreeSpace(rootPath).let { 
                            if (it > 0) it else {
                                val systemPath = Environment.getRootDirectory().path
                                JavaFile.getFreeSpace(systemPath)
                            }
                        }
                        val used = total - free
                        val pct = if (total > 0) (used * 100 / total).toInt() else 0

                        binding.storageRootProgress.progress = pct
                        binding.storageRootProgress.visibility = View.VISIBLE
                        binding.storageRootSpace.text = "${used.asFileSize().formatHumanReadable(requireContext())} used of ${total.asFileSize().formatHumanReadable(requireContext())}"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.storageRootSpace.text = "Root storage details unavailable"
                    }
                } else {
                    binding.storageRootTitle.text = "Root (Unavailable)"
                    binding.storageRootSpace.text = "Shizuku service is not running or permission is not granted"
                    binding.storageRootProgress.visibility = View.GONE
                }
                binding.storageRoot.setOnClickListener {
                    if (isRootAccessible()) {
                        navigateToRoot(Paths.get("/"))
                    } else {
                        requestShizukuPermission()
                    }
                }
            } else {
                binding.storageRoot.visibility = View.GONE
            }
        }

        // Dynamically listen to custom storage devices (only when added)
        Settings.STORAGES.observe(viewLifecycleOwner) { storages ->
            updateCustomStorages(storages)
        }
    }

    private fun updateCustomStorages(storages: List<Storage>) {
        val storageContainer = binding.storageContainer
        val serverContainer = binding.serverContainer

        val storageChildCount = storageContainer.childCount
        if (storageChildCount > 2) {
            storageContainer.removeViews(2, storageChildCount - 2)
        }
        serverContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        var hasServers = false

        for (storage in storages) {
            if (!storage.isVisible) continue
            if (storage is me.mumin.android.files.storage.PrimaryStorageVolume) continue
            if (storage is me.mumin.android.files.storage.FileSystemRoot) continue

            val isServer = storage is me.mumin.android.files.storage.FtpServer 
                || storage is me.mumin.android.files.storage.SftpServer
                || storage is me.mumin.android.files.storage.SmbServer
                || storage is me.mumin.android.files.storage.WebDavServer

            if (isServer) {
                hasServers = true
                val serverBinding = ServerDeviceItemBinding.inflate(inflater, serverContainer, false)
                serverBinding.serverTitle.text = storage.getName(requireContext())

                val type = when (storage) {
                    is me.mumin.android.files.storage.FtpServer -> "FTP Connection"
                    is me.mumin.android.files.storage.SftpServer -> "SFTP Connection"
                    is me.mumin.android.files.storage.SmbServer -> "SMB Connection"
                    is me.mumin.android.files.storage.WebDavServer -> "WebDAV Connection"
                    else -> "Network Connection"
                }
                serverBinding.serverConnectionType.text = type

                var host = ""
                var username = ""
                try {
                    val authorityField = storage.javaClass.getDeclaredField("authority")
                    authorityField.isAccessible = true
                    val authorityObj = authorityField.get(storage)
                    if (authorityObj != null) {
                        val hostField = authorityObj.javaClass.getDeclaredField("host")
                        hostField.isAccessible = true
                        host = hostField.get(authorityObj) as? String ?: ""

                        val usernameField = authorityObj.javaClass.getDeclaredField("username")
                        usernameField.isAccessible = true
                        username = usernameField.get(authorityObj) as? String ?: ""
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (username.isNotEmpty() && host.isNotEmpty()) {
                    serverBinding.serverAddress.text = "$username@$host"
                    serverBinding.serverAddress.visibility = View.VISIBLE
                } else if (host.isNotEmpty()) {
                    serverBinding.serverAddress.text = host
                    serverBinding.serverAddress.visibility = View.VISIBLE
                } else {
                    serverBinding.serverAddress.visibility = View.GONE
                }

                // Determine connection state dynamically in background using ServerStatusManager
                val serverId = storage.id.toString()
                
                fun updateServerUI(status: String, lastSuccessTime: Long) {
                    val dotColor = when (status) {
                        "Connecting" -> "#FFD54F"
                        "Connected" -> "#81C784"
                        "Authentication Failed" -> "#E57373"
                        else -> "#888888"
                    }
                    serverBinding.serverStatusDot.visibility = View.VISIBLE
                    serverBinding.serverStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(dotColor))
                    
                    if (status == "Offline" || status == "Connecting") {
                        val ctx = context
                        val lastSeenTime = if (lastSuccessTime > 0L) lastSuccessTime else {
                            ctx?.getSharedPreferences("server_last_seen", Context.MODE_PRIVATE)?.getLong(serverId, 0L) ?: 0L
                        }
                        if (lastSeenTime > 0L) {
                            val diffMs = System.currentTimeMillis() - lastSeenTime
                            val diffMinutes = diffMs / (1000 * 60)
                            val diffHours = diffMinutes / 60
                            val diffDays = diffHours / 24
                            val lastSeenStr = when {
                                diffDays > 0 -> "Last seen ${diffDays}d ago"
                                diffHours > 0 -> "Last seen ${diffHours}h ago"
                                diffMinutes > 0 -> "Last seen ${diffMinutes}m ago"
                                else -> "Last seen just now"
                            }
                            serverBinding.serverStatusText.text = if (status == "Connecting") "Connecting… ($lastSeenStr)" else lastSeenStr
                        } else {
                            serverBinding.serverStatusText.text = status
                        }
                    } else {
                        serverBinding.serverStatusText.text = status
                    }
                }

                // Apply initial/cached state immediately
                val cached = ServerStatusManager.getCachedStatus(serverId)
                if (cached != null) {
                    updateServerUI(cached.status, cached.lastSuccessfulConnection)
                } else {
                    updateServerUI("Connecting", 0L)
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val ctx = context ?: return@launch
                    val info = ServerStatusManager.checkServerStatus(ctx, storage, force = false)
                    updateServerUI(info.status, info.lastSuccessfulConnection)
                }

                try {
                    serverBinding.serverIcon.setImageResource(storage.iconRes)
                } catch (e: Exception) {
                    serverBinding.serverIcon.setImageResource(R.drawable.computer_icon_white_24dp)
                }

                serverBinding.root.setOnClickListener {
                    val ctx = context
                    if (ctx != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                ServerStatusManager.checkServerStatus(ctx, storage, force = true)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    val path = storage.path
                    if (path != null) {
                        navigateToRoot(path)
                    } else {
                        val intent = storage.createIntent()
                        if (intent != null) {
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                serverContainer.addView(serverBinding.root)
            } else {
                val itemBinding = StorageCardItemBinding.inflate(inflater, storageContainer, false)
                itemBinding.storageTitle.text = storage.getName(requireContext())
                itemBinding.storageSubtitle.text = storage.description
                try {
                    itemBinding.storageIcon.setImageResource(storage.iconRes)
                } catch (e: Exception) {
                    itemBinding.storageIcon.setImageResource(R.drawable.computer_icon_white_24dp)
                }

                itemBinding.root.setOnClickListener {
                    val path = storage.path
                    if (path != null) {
                        navigateToRoot(path)
                    } else {
                        val intent = storage.createIntent()
                        if (intent != null) {
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                storageContainer.addView(itemBinding.root)
            }
        }

        if (hasServers) {
            binding.serverDevicesHeader.visibility = View.VISIBLE
            binding.serverContainer.visibility = View.VISIBLE
        } else {
            binding.serverDevicesHeader.visibility = View.GONE
            binding.serverContainer.visibility = View.GONE
        }
    }

    private suspend fun queryCategoryStats(
        contentUri: android.net.Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var count = 0
        var size = 0L
        val context = context ?: return@withContext Pair(0, 0L)
        try {
            context.contentResolver.query(
                contentUri,
                arrayOf(MediaStore.Files.FileColumns.SIZE),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                while (cursor.moveToNext()) {
                    count++
                    if (sizeColumn >= 0) {
                        size += cursor.getLong(sizeColumn)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Pair(count, size)
    }

    private fun loadCachedCategoryStats() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("category_stats_cache", Context.MODE_PRIVATE)
        val numberFormat = java.text.NumberFormat.getInstance()

        binding.catDownloadsCount.text = "${numberFormat.format(prefs.getInt("downloads_count", 0))} files"
        binding.catDownloadsSize.text = prefs.getLong("downloads_size", 0L).asFileSize().formatHumanReadable(context)
        
        binding.catImagesCount.text = "${numberFormat.format(prefs.getInt("images_count", 0))} files"
        binding.catImagesSize.text = prefs.getLong("images_size", 0L).asFileSize().formatHumanReadable(context)

        binding.catVideosCount.text = "${numberFormat.format(prefs.getInt("videos_count", 0))} files"
        binding.catVideosSize.text = prefs.getLong("videos_size", 0L).asFileSize().formatHumanReadable(context)

        binding.catAudioCount.text = "${numberFormat.format(prefs.getInt("audio_count", 0))} files"
        binding.catAudioSize.text = prefs.getLong("audio_size", 0L).asFileSize().formatHumanReadable(context)

        binding.catDocumentsCount.text = "${numberFormat.format(prefs.getInt("documents_count", 0))} files"
        binding.catDocumentsSize.text = prefs.getLong("documents_size", 0L).asFileSize().formatHumanReadable(context)

        binding.catApksCount.text = "${numberFormat.format(prefs.getInt("apks_count", 0))} files"
        binding.catApksSize.text = prefs.getLong("apks_size", 0L).asFileSize().formatHumanReadable(context)
    }

    private fun loadCategoryStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            val context = context ?: return@launch

            val numberFormat = java.text.NumberFormat.getInstance()

            // Downloads
            val downloadsStats = withContext(Dispatchers.IO) {
                var count = 0
                var size = 0L
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                fun scan(file: File) {
                    if (file.isDirectory) {
                        file.listFiles()?.forEach { scan(it) }
                    } else if (file.isFile) {
                        count++
                        size += file.length()
                    }
                }
                if (downloadDir.exists()) {
                    scan(downloadDir)
                }
                Pair(count, size)
            }
            binding.catDownloadsCount.text = "${numberFormat.format(downloadsStats.first)} files"
            binding.catDownloadsSize.text = downloadsStats.second.asFileSize().formatHumanReadable(context)

            // Images
            val imagesStats = queryCategoryStats(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null)
            binding.catImagesCount.text = "${numberFormat.format(imagesStats.first)} files"
            binding.catImagesSize.text = imagesStats.second.asFileSize().formatHumanReadable(context)

            // Videos
            val videosStats = queryCategoryStats(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null)
            binding.catVideosCount.text = "${numberFormat.format(videosStats.first)} files"
            binding.catVideosSize.text = videosStats.second.asFileSize().formatHumanReadable(context)

            // Audio
            val audioStats = queryCategoryStats(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null)
            binding.catAudioCount.text = "${numberFormat.format(audioStats.first)} files"
            binding.catAudioSize.text = audioStats.second.asFileSize().formatHumanReadable(context)

            // Documents
            val docExtensions = listOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf", ".odt")
            val docSelection = docExtensions.map { "${MediaStore.Files.FileColumns.DATA} LIKE ?" }.joinToString(" OR ")
            val docArgs = docExtensions.map { "%$it" }.toTypedArray()
            val docStats = queryCategoryStats(MediaStore.Files.getContentUri("external"), docSelection, docArgs)
            binding.catDocumentsCount.text = "${numberFormat.format(docStats.first)} files"
            binding.catDocumentsSize.text = docStats.second.asFileSize().formatHumanReadable(context)

            // APKs
            val apkStats = queryCategoryStats(
                MediaStore.Files.getContentUri("external"),
                "${MediaStore.Files.FileColumns.DATA} LIKE ?",
                arrayOf("%.apk")
            )
            binding.catApksCount.text = "${numberFormat.format(apkStats.first)} files"
            binding.catApksSize.text = apkStats.second.asFileSize().formatHumanReadable(context)

            // Save to persistent cache
            val prefs = context.getSharedPreferences("category_stats_cache", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("downloads_count", downloadsStats.first)
                .putLong("downloads_size", downloadsStats.second)
                .putInt("images_count", imagesStats.first)
                .putLong("images_size", imagesStats.second)
                .putInt("videos_count", videosStats.first)
                .putLong("videos_size", videosStats.second)
                .putInt("audio_count", audioStats.first)
                .putLong("audio_size", audioStats.second)
                .putInt("documents_count", docStats.first)
                .putLong("documents_size", docStats.second)
                .putInt("apks_count", apkStats.first)
                .putLong("apks_size", apkStats.second)
                .apply()
        }
    }

    private fun loadRecentFiles() {
        viewLifecycleOwner.lifecycleScope.launch {
            val recents = withContext(Dispatchers.IO) { queryRecentFiles() }
            if (recents.isNotEmpty()) {
                binding.recentSection.visibility = View.VISIBLE
                binding.recentRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.recentRecycler.adapter = RecentFilesAdapter(requireContext(), recents) { file, mimeType ->
                    val intent = OpenFileActivity.createIntent(Paths.get(file.path), mimeType)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                binding.recentSection.visibility = View.GONE
            }
        }
    }

    private fun queryRecentFiles(): List<RecentFile> {
        val list = mutableListOf<RecentFile>()
        val context = context ?: return list
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.Files.FileColumns.SIZE} > 0 AND ${MediaStore.Files.FileColumns.DATA} NOT LIKE '%/Android/data%'"
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC LIMIT 8"

        try {
            context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: continue
                    val path = cursor.getString(pathColumn) ?: continue
                    val size = cursor.getLong(sizeColumn)
                    val date = cursor.getLong(dateColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    list.add(RecentFile(id, name, path, size, date, contentUri))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun setupNavigationDrawer(savedInstanceState: Bundle?) {
        val existing = childFragmentManager.findFragmentById(R.id.navigationFragment)
        if (existing != null) {
            navigationFragment = existing as NavigationFragment
        } else {
            navigationFragment = NavigationFragment()
            childFragmentManager.commit { add(R.id.navigationFragment, navigationFragment) }
        }
        navigationFragment.listener = this
        addOnBackPressedCallback(DrawerLayoutOnBackPressedCallback(binding.drawerLayout))
    }

    // NavigationFragment.Listener implementation
    override val currentPath: Path
        get() = Paths.get(Environment.getExternalStorageDirectory().absolutePath)

    override fun navigateTo(path: Path) {
        val activity = requireActivity() as FileListActivity
        activity.selectTabAndNavigate(R.id.tab_browse, path)
    }

    override fun navigateToRoot(path: Path) {
        val activity = requireActivity() as FileListActivity
        activity.navigateToPath(path)
    }

    override fun navigateToDefaultRoot() {
        val extPath = Environment.getExternalStorageDirectory().absolutePath
        val activity = requireActivity() as FileListActivity
        activity.selectTabAndNavigate(R.id.tab_browse, Paths.get(extPath))
    }

    override fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit) {
        observer(Paths.get(Environment.getExternalStorageDirectory().absolutePath))
    }

    override fun closeNavigationDrawer() {
        if (this::binding.isInitialized) {
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireContext().contentResolver.unregisterContentObserver(mediaStoreObserver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        downloadsObserver?.stopWatching()
    }

    private fun setupDownloadsObserver() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir.exists()) {
            val mask = android.os.FileObserver.CREATE or 
                       android.os.FileObserver.DELETE or 
                       android.os.FileObserver.MOVED_FROM or 
                       android.os.FileObserver.MOVED_TO
            downloadsObserver = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                object : android.os.FileObserver(downloadDir, mask) {
                    override fun onEvent(event: Int, path: String?) {
                        scheduleCategoryStatsLoad()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                object : android.os.FileObserver(downloadDir.path, mask) {
                    override fun onEvent(event: Int, path: String?) {
                        scheduleCategoryStatsLoad()
                    }
                }
            }
            downloadsObserver?.startWatching()
        }
    }

    private fun isRootAccessible(): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return false
        }
        return try {
            if (rikka.shizuku.Shizuku.pingBinder()) {
                rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                rikka.sui.Sui.isSui()
            }
        } catch (e: Throwable) {
            false
        }
    }

    private fun requestShizukuPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                if (rikka.shizuku.Shizuku.pingBinder()) {
                    if (rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        Settings.ROOT_STRATEGY.putValue(me.mumin.android.files.provider.root.RootStrategy.ALWAYS)
                        android.widget.Toast.makeText(requireContext(), "Shizuku permission already granted", android.widget.Toast.LENGTH_SHORT).show()
                        setupStorages() // Refresh space and title
                    } else {
                        val listener = object : rikka.shizuku.Shizuku.OnRequestPermissionResultListener {
                            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                                rikka.shizuku.Shizuku.removeRequestPermissionResultListener(this)
                                if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    Settings.ROOT_STRATEGY.putValue(me.mumin.android.files.provider.root.RootStrategy.ALWAYS)
                                    android.widget.Toast.makeText(requireContext(), "Shizuku permission granted", android.widget.Toast.LENGTH_SHORT).show()
                                    setupStorages() // Refresh space and title
                                } else {
                                    android.widget.Toast.makeText(requireContext(), "Shizuku permission denied", android.widget.Toast.LENGTH_SHORT).show()
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
}
