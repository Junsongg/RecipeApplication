package com.example.recipeapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeapplication.databinding.ActivityAddRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference
    private lateinit var bitmap: Bitmap
    private lateinit var uri: Uri
    private lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")
        firestore = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        downloadUrl = ""

        val categorySpinner = resources.getStringArray(R.array.Category)
        val spinner = binding.spinner
        var category = ""
        spinner.adapter =  ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorySpinner)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                category = spinner.selectedItem.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.uploadImg.setOnClickListener {
            openFileChooser()
        }

        binding.btnBack.setOnClickListener{
            finish()
        }

        binding.btnSave.setOnClickListener {
            val etRecipeName = binding.etRecipeName
            val etIngredients = binding.etIngredients
            val etInstructions = binding.etInstructions
            val recipeName = etRecipeName.text.toString()
            val ingredients = etIngredients.text.toString()
            val instructions = etInstructions.text.toString()
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("Saving....").setCancelable(false)
            val alert = dialog.create()
            alert.show()

            if(recipeName.isEmpty()){
                alert.dismiss()
                etRecipeName.error = "This field cannot be left empty!"
                etRecipeName.requestFocus()
                return@setOnClickListener
            }

            if(ingredients.isEmpty()){
                alert.dismiss()
                etIngredients.error = "This field cannot be left empty!"
                etIngredients.requestFocus()
                return@setOnClickListener
            }

            if(instructions.isEmpty()){
                alert.dismiss()
                etInstructions.error = "This field cannot be left empty!"
                etInstructions.requestFocus()
                return@setOnClickListener
            }

            if(downloadUrl == ""){
                alert.dismiss()
                Toast.makeText(this, "Image not added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val recipeId = UUID.randomUUID().toString()
            val userId = mAuth.currentUser!!.uid
            val recipeMap = hashMapOf(
                "recipeId" to recipeId,
                "userId" to userId,
                "recipeName" to recipeName,
                "recipeImage" to downloadUrl,
                "ingredients" to ingredients,
                "instructions" to instructions,
                "category" to category
            )


            firestore.collection("recipe").document().set(recipeMap).addOnSuccessListener {
                Toast.makeText(this, "Recipe Saved", Toast.LENGTH_SHORT).show()
                alert.dismiss()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong, Please try again", Toast.LENGTH_SHORT).show()
                alert.dismiss()
                finish()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadFile(){
        if(uri != null){
            val fileRef = mStorageRef.child(System.currentTimeMillis().toString() + "." + getFileExtension(uri))
            fileRef.putFile(uri).addOnSuccessListener {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                fileRef.downloadUrl.addOnSuccessListener { e->
                    downloadUrl = e.toString()
                }
            }.addOnFailureListener{
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentRes = this.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentRes.getType(uri))
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            uri = data.data!!
            if(Build.VERSION.SDK_INT >= 28){
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                bitmap = ImageDecoder.decodeBitmap(source)
                binding.uploadImg.setImageBitmap(bitmap)
            }else{
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                binding.uploadImg.setImageBitmap(bitmap)
            }
        }
        uploadFile()
    }
}