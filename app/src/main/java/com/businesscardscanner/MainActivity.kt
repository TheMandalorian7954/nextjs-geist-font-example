package com.businesscardscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.businesscardscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var cardAdapter: BusinessCardAdapter
    private val scannedCards = mutableListOf<BusinessCard>()
    
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                processImage(uri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(
                this,
                "Storage permission is required to access images",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        loadSavedCards()
    }
    
    private fun setupRecyclerView() {
        cardAdapter = BusinessCardAdapter(scannedCards) { card ->
            val intent = Intent(this, CardDetailActivity::class.java).apply {
                putExtra("business_card", card)
            }
            startActivity(intent)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = cardAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddCard.setOnClickListener {
            checkPermissionAndOpenGallery()
        }
        
        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }
    }
    
    private fun checkPermissionAndOpenGallery() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }
    
    private fun processImage(uri: Uri) {
        val intent = Intent(this, CardDetailActivity::class.java).apply {
            putExtra("image_uri", uri.toString())
        }
        startActivityForResult(intent, REQUEST_CARD_DETAIL)
    }
    
    private fun loadSavedCards() {
        scannedCards.clear()
        scannedCards.addAll(CardStorage.getAllCards(this))
        cardAdapter.notifyDataSetChanged()
    }
    
    private fun exportToExcel() {
        if (scannedCards.isEmpty()) {
            Toast.makeText(this, "No cards to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        ExcelExporter.exportCards(this, scannedCards) { success, filePath ->
            if (success) {
                Toast.makeText(
                    this,
                    "Excel file saved to: $filePath",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Failed to export Excel file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CARD_DETAIL && resultCode == RESULT_OK) {
            loadSavedCards()
        }
    }
    
    companion object {
        private const val REQUEST_CARD_DETAIL = 1001
    }
}
