/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

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
import me.mumin.android.files.file.JavaFile
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.asMimeTypeOrNull
import me.mumin.android.files.file.asFileSize
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.settings.SettingsActivity
import me.mumin.android.files.storage.Storage
import java.io.File
import java.io.IOException
import me.mumin.android.files.provider.common.WalkFileTreeSearchable
import me.mumin.android.files.ui.DrawerLayoutOnBackPressedCallback
import me.mumin.android.files.util.addOnBackPressedCallback

class HomeFragment : Fragment(), NavigationFragment.Listener {
    private lateinit var binding: HomeFragmentBinding
    private lateinit var navigationFragment: NavigationFragment

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupStorages()
            loadRecentFiles()
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
        setupCategories()
        setupStorages()
        loadRecentFiles()
        setupNavigationDrawer(savedInstanceState)
    }

    private lateinit var searchAdapter: SearchResultsAdapter

    private fun setupSearchAndSettings() {
        binding.searchBarCard.setOnClickListener(null)
        binding.searchBarCard.isClickable = false

        binding.searchMenuIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        binding.searchSettingsIcon.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
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
            navigateTo(Paths.get(extPath))
        }

        // Dynamically listen to custom storage devices (only when added)
        Settings.STORAGES.observe(viewLifecycleOwner) { storages ->
            updateCustomStorages(storages)
        }
    }

    private fun updateCustomStorages(storages: List<Storage>) {
        val container = binding.storageContainer
        val childCount = container.childCount
        if (childCount > 1) {
            container.removeViews(1, childCount - 1)
        }

        val inflater = LayoutInflater.from(requireContext())
        for (storage in storages) {
            if (!storage.isVisible) continue
            if (storage is me.mumin.android.files.storage.DeviceStorage) continue
            val itemBinding = StorageCardItemBinding.inflate(inflater, container, false)
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
                    navigateTo(path)
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
            container.addView(itemBinding.root)
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
        activity.navigateToPath(path)
    }

    override fun navigateToRoot(path: Path) {
        val activity = requireActivity() as FileListActivity
        activity.navigateToPath(path)
    }

    override fun navigateToDefaultRoot() {
        val extPath = Environment.getExternalStorageDirectory().absolutePath
        val activity = requireActivity() as FileListActivity
        activity.navigateToPath(Paths.get(extPath))
    }

    override fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit) {
        observer(Paths.get(Environment.getExternalStorageDirectory().absolutePath))
    }

    override fun closeNavigationDrawer() {
        if (this::binding.isInitialized) {
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
    }
}
