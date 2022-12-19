package com.example.recipeapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.recipeapplication.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.security.Permission

class SplashScreenActivity : AppCompatActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivitySplashScreenBinding
    private var READ_STORAGE_PERM = 123
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            getReadStoragePermission()
        }, 3000)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.addAuthStateListener {
            if (it.currentUser != null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getReadStoragePermission() {
        if(hasStoragePermission()){
            binding.loader.visibility = View.INVISIBLE
            binding.btnGetStarted.text = "Im ready"
            binding.btnGetStarted.visibility = View.VISIBLE
        }else{
            EasyPermissions.requestPermissions(
                this,
                "This app requires storage permission",
                READ_STORAGE_PERM,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onRationaleAccepted(requestCode: Int) {}

    override fun onRationaleDenied(requestCode: Int) {}

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        binding.loader.visibility = View.INVISIBLE
        binding.btnGetStarted.visibility = View.VISIBLE
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}