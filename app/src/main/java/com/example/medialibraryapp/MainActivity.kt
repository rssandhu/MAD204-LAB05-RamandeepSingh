/**
 * Course: MOBILE APP DEVELOPMENT - Lab 5
 * Student: Ramandeep Singh - A00194321
 * Date: December 09, 2025
 * Description: Main activity for Media Library App with PROPER permission handling,
 * Room database, RecyclerView, GSON export/import, and SharedPreferences.
 */

package com.example.medialibraryapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import com.example.medialibraryapp.R
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mediaImageView: ImageView
    private lateinit var mediaVideoView: VideoView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addToFavoritesBtn: FloatingActionButton
    private lateinit var exportBtn: FloatingActionButton
    private lateinit var importBtn: FloatingActionButton

    private lateinit var db: FavoritesDatabase
    private lateinit var adapter: FavoritesAdapter
    private var currentMediaUri: Uri? = null
    private var currentMediaType: String? = null
    private val gson = Gson()

    // ✅ Permission launcher for Android 12 and below
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_LONG).show()
        }
    }

    private val singlePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleMediaSelected(it) }
    }

    private val multiplePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { handleMediaSelected(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initDatabase()
        setupRecyclerView()
        loadLastMedia()
        setupClickListeners()
        refreshFavorites()
    }

    private fun initViews() {
        mediaImageView = findViewById(R.id.mediaImageView)
        mediaVideoView = findViewById(R.id.mediaVideoView)
        recyclerView = findViewById(R.id.recyclerView)
        addToFavoritesBtn = findViewById(R.id.addToFavoritesBtn)
        exportBtn = findViewById(R.id.exportBtn)
        importBtn = findViewById(R.id.importBtn)
    }

    private fun initDatabase() {
        db = Room.databaseBuilder(
            applicationContext,
            FavoritesDatabase::class.java,
            "favorites_db"
        ).build()
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter { media -> showDeleteConfirmation(media) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.pickSingleBtn).setOnClickListener {
            checkPermissionsAndPickSingle()
        }

        findViewById<View>(R.id.pickMultipleBtn).setOnClickListener {
            checkPermissionsAndPickMultiple()
        }

        addToFavoritesBtn.setOnClickListener {
            currentMediaUri?.let { uri ->
                currentMediaType?.let { type ->
                    lifecycleScope.launch {
                        val media = FavoriteMedia(uri = uri.toString(), type = type)
                        db.favoriteDao().insert(media)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Added to favorites!", Toast.LENGTH_SHORT).show()
                        }
                        refreshFavoritesSuspend()
                    }
                }
            } ?: Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }

        exportBtn.setOnClickListener { showExportDialog() }
        importBtn.setOnClickListener { showImportDialog() }
    }

    // ✅ Permission handling for single picker
    private fun checkPermissionsAndPickSingle() {
        if (hasMediaPermissions()) {
            singlePicker.launch("image/* video/*")
        } else {
            requestMediaPermission()
        }
    }

    // ✅ Permission handling for multiple picker
    private fun checkPermissionsAndPickMultiple() {
        if (hasMediaPermissions()) {
            multiplePicker.launch("image/* video/*")
        } else {
            requestMediaPermission()
        }
    }

    // ✅ Check if app has media permissions
    private fun hasMediaPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - READ_MEDIA_* permissions from manifest
            true
        } else {
            // Android 12 and below - READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ Request media permission
    private fun requestMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - auto-granted by manifest
            Toast.makeText(this, "Permissions ready! Picking media...", Toast.LENGTH_SHORT).show()
            singlePicker.launch("image/* video/*")
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // ✅ Non-suspend wrapper - call from anywhere
    private fun refreshFavorites() {
        lifecycleScope.launch {
            refreshFavoritesSuspend()
        }
    }

    // ✅ Suspend function - call only from coroutines
    private suspend fun refreshFavoritesSuspend() {
        val favorites = db.favoriteDao().getAllFavorites()
        adapter.submitList(favorites)
    }

    private fun handleMediaSelected(uri: Uri) {
        currentMediaUri = uri
        val prefs = getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_uri", uri.toString()).apply()

        val type = if (uri.toString().contains("image", ignoreCase = true)) "image" else "video"
        currentMediaType = type

        when (type) {
            "image" -> {
                mediaImageView.visibility = View.VISIBLE
                mediaVideoView.visibility = View.GONE
                mediaImageView.setImageURI(uri)
            }
            "video" -> {
                mediaImageView.visibility = View.GONE
                mediaVideoView.visibility = View.VISIBLE
                mediaVideoView.setVideoURI(uri)
                mediaVideoView.start()
            }
        }
    }

    private fun showDeleteConfirmation(media: FavoriteMedia) {
        AlertDialog.Builder(this)
            .setTitle("Delete Favorite")
            .setMessage("Remove this media from favorites?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.favoriteDao().delete(media)
                    runOnUiThread {
                        Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                lifecycleScope.launch {
                                    db.favoriteDao().insert(media)
                                    refreshFavoritesSuspend()
                                }
                            }.show()
                    }
                    refreshFavoritesSuspend()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExportDialog() {
        lifecycleScope.launch {
            val favorites = db.favoriteDao().getAllFavorites()
            val json = gson.toJson(favorites)
            Log.d("MediaLibrary", "Export JSON: $json")

            val file = File(filesDir, "favorites_export.json")
            file.writeText(json)

            runOnUiThread {
                Toast.makeText(this@MainActivity, "Exported ${favorites.size} items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImportDialog() {
        AlertDialog.Builder(this)
            .setTitle("Import JSON")
            .setMessage("Import sample data?")
            .setPositiveButton("Import") { _, _ -> importSampleData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importSampleData() {
        val sampleJson = """
        [
            {"id":1,"uri":"content://media/external/images/media/123","type":"image"},
            {"id":2,"uri":"content://media/external/video/media/456","type":"video"}
        ]
        """.trimIndent()

        lifecycleScope.launch {
            val typeToken = object : TypeToken<List<FavoriteMedia>>() {}
            val favorites: List<FavoriteMedia> = gson.fromJson(sampleJson, typeToken.type) ?: emptyList()

            favorites.forEach {
                lifecycleScope.launch {
                    db.favoriteDao().insert(it)
                }
            }

            refreshFavoritesSuspend()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Imported ${favorites.size} items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadLastMedia() {
        val prefs = getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
        val lastUri = prefs.getString("last_uri", null)
        lastUri?.let { uriString ->
            val uri = Uri.parse(uriString)
            handleMediaSelected(uri)
        }
    }
}
