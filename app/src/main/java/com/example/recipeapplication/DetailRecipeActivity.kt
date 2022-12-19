package com.example.recipeapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeapplication.databinding.ActivityDetailRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DetailRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailRecipeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mStorageRef: StorageReference
    private lateinit var bitmap: Bitmap
    private lateinit var uri: Uri
    private lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")

        var recipeId = intent.getStringExtra("recipeId").toString()
        var position = intent.getStringExtra("position").toString()
        binding.tvRecipeName.text = intent.getStringExtra("recipeName").toString()
        Glide.with(this).load(intent.getStringExtra("recipeImage").toString()).into(binding.recipeImage)
        binding.tvIngredients.text = intent.getStringExtra("ingredients").toString()
        binding.tvInstructions.text = intent.getStringExtra("instructions").toString()
        binding.etRecipeName.setText(intent.getStringExtra("recipeName").toString())
        binding.etInstructions.setText(intent.getStringExtra("instructions").toString())
        binding.etIngredients.setText(intent.getStringExtra("ingredients").toString())

        binding.etRecipeName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString() == binding.tvRecipeName.text){
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnSave.visibility = View.GONE
                }else{
                    binding.btnCancel.visibility = View.GONE
                    binding.btnSave.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.etIngredients.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString() == binding.tvIngredients.text){
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnSave.visibility = View.GONE
                }else{
                    binding.btnCancel.visibility = View.GONE
                    binding.btnSave.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.etInstructions.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString() == binding.tvInstructions.text){
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnSave.visibility = View.GONE
                }else{
                    binding.btnCancel.visibility = View.GONE
                    binding.btnSave.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnEdit.setOnClickListener {
            binding.recipeImageWrapper.visibility = View.VISIBLE
            binding.ivWrapper.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.tvRecipeName.visibility = View.GONE
            binding.tvInstructions.visibility = View.GONE
            binding.tvIngredients.visibility = View.GONE
            binding.etRecipeName.visibility = View.VISIBLE
            binding.etIngredients.visibility = View.VISIBLE
            binding.etInstructions.visibility = View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            binding.recipeImageWrapper.visibility = View.GONE
            binding.ivWrapper.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.VISIBLE
            binding.tvRecipeName.visibility = View.VISIBLE
            binding.tvInstructions.visibility = View.VISIBLE
            binding.tvIngredients.visibility = View.VISIBLE
            binding.etRecipeName.visibility = View.GONE
            binding.etIngredients.visibility = View.GONE
            binding.etInstructions.visibility = View.GONE
        }

        binding.btnDelete.setOnClickListener {
            createDialog(recipeId)
        }

        binding.btnSave.setOnClickListener {
            saveChanges(recipeId)
        }

        binding.recipeImageWrapper.setOnClickListener {
            openFileChooser()
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
                binding.recipeImage.setImageBitmap(bitmap)
            }else{
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                binding.recipeImage.setImageBitmap(bitmap)
            }
        }
        uploadFile()
    }

    private fun uploadFile() {
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
        binding.btnCancel.visibility = View.GONE
        binding.btnSave.visibility = View.VISIBLE
        binding.recipeImageWrapper.visibility = View.GONE
        binding.ivWrapper.visibility = View.GONE
    }

    private fun saveChanges(recipeId: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Saving Changes....").setCancelable(false)
        val alert = dialog.create()
        alert.show()
        val recipeMap = mapOf(
            "recipeId" to recipeId,
            "userId" to firebaseAuth.currentUser!!.uid.toString(),
            "recipeName" to binding.etRecipeName.text.toString(),
            "recipeImage" to downloadUrl,
            "ingredients" to binding.etIngredients.text.toString(),
            "instructions" to binding.etInstructions.text.toString(),
            "category" to intent.getStringExtra("category").toString()
        )

        val query = firestore.collection("recipe").whereEqualTo("recipeId", recipeId)
        query.get().addOnSuccessListener {
            it.documents.forEach { doc->
                doc.reference.update(recipeMap)
            }
            binding.tvRecipeName.text = binding.etRecipeName.text.toString()
            binding.tvIngredients.text = binding.etIngredients.text.toString()
            binding.tvInstructions.text = binding.etInstructions.text.toString()

            Handler(Looper.getMainLooper()).postDelayed({
                alert.dismiss()
            }, 1000)
            binding.recipeImageWrapper.visibility = View.GONE
            binding.ivWrapper.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnSave.visibility = View.GONE
            binding.tvRecipeName.visibility = View.VISIBLE
            binding.tvInstructions.visibility = View.VISIBLE
            binding.tvIngredients.visibility = View.VISIBLE
            binding.etRecipeName.visibility = View.GONE
            binding.etIngredients.visibility = View.GONE
            binding.etInstructions.visibility = View.GONE
        }
    }

    private fun createDialog(recipeId: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure to permanently delete this?")
        dialog.setPositiveButton("Delete"){ _: DialogInterface, _:Int ->
            var query = firestore.collection("recipe").whereEqualTo("recipeId", recipeId)
            query.get().addOnSuccessListener {
                it.documents.forEach { doc->
                    doc.reference.delete().addOnCompleteListener { delete->
                        if(delete.isSuccessful){
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        dialog.setNegativeButton("cancel"){ _: DialogInterface, _:Int ->}
        dialog.create().show()
    }
}