package com.businesscardscanner

import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.businesscardscanner.databinding.ActivityCardDetailBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CardDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCardDetailBinding
    private lateinit var textRecognizer: TextRecognizer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        
        setupUI()
        processIntent()
    }
    
    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.btnSaveContact.setOnClickListener {
            saveContact()
        }
        
        binding.btnSaveCard.setOnClickListener {
            saveCard()
        }
    }
    
    private fun processIntent() {
        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }
        val existingCard = intent.getParcelableExtra<BusinessCard>("business_card")
        
        if (existingCard != null) {
            displayCardDetails(existingCard)
        } else if (imageUri != null) {
            processImage(imageUri)
        }
    }
    
    private fun processImage(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                val card = ContactParser.parseBusinessCard(extractedText)
                displayCardDetails(card)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to process image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    
    private fun displayCardDetails(card: BusinessCard) {
        binding.apply {
            etName.setText(card.name)
            etTitle.setText(card.title)
            etCompany.setText(card.company)
            etPhone.setText(card.phone)
            etEmail.setText(card.email)
            etAddress.setText(card.address)
        }
    }
    
    private fun saveContact() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                REQUEST_WRITE_CONTACTS
            )
            return
        }
        
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val company = binding.etCompany.text.toString()
        val title = binding.etTitle.text.toString()
        val address = binding.etAddress.text.toString()
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val operations = ArrayList<ContentProviderOperation>()
        val rawContactId = 0
        
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        
        operations.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build()
        )
        
        if (phone.isNotEmpty()) {
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    )
                    .build()
            )
        }
        
        if (email.isNotEmpty()) {
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                    .withValue(
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                    )
                    .build()
            )
        }
        
        if (company.isNotEmpty() || title.isNotEmpty()) {
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, title)
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.TYPE_WORK
                    )
                    .build()
            )
        }
        
        if (address.isNotEmpty()) {
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                        address
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
                    )
                    .build()
            )
        }
        
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to save contact: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun saveCard() {
        val card = BusinessCard(
            name = binding.etName.text.toString(),
            title = binding.etTitle.text.toString(),
            company = binding.etCompany.text.toString(),
            phone = binding.etPhone.text.toString(),
            email = binding.etEmail.text.toString(),
            address = binding.etAddress.text.toString()
        )
        
        CardStorage.saveCard(this, card)
        setResult(RESULT_OK)
        finish()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_WRITE_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveContact()
            } else {
                Toast.makeText(
                    this,
                    "Contacts permission is required to save contacts",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    companion object {
        private const val REQUEST_WRITE_CONTACTS = 1001
    }
}
