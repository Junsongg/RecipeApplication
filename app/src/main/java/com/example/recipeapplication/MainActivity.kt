package com.example.recipeapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeapplication.databinding.ActivityMainBinding
import com.example.recipeapplication.entity.Recipe
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore
    private lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        userId =firebaseAuth.currentUser!!.uid
        firestore = FirebaseFirestore.getInstance()

        firebaseAuth.addAuthStateListener {
            if (it.currentUser == null){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener{
            createDialog()
        }

        val category = resources.getStringArray(R.array.Category)
        val spinner = binding.spinner
        spinner.adapter =  ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, category)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(spinner.selectedItem.toString() != "All Recipe"){
                    val query = firestore.collection("recipe")
                        .whereEqualTo("category", spinner.selectedItem)
                        .whereEqualTo("userId", userId)
                    updateRecipeRecycler(query)
                }else{
                    val query = firestore.collection("recipe")
                        .whereEqualTo("userId", userId)
                    updateRecipeRecycler(query)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                val query = firestore.collection("recipe")
                    .whereEqualTo("userId", userId)
                updateRecipeRecycler(query)
            }

        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0.toString().isEmpty()) {
                    val query = firestore.collection("recipe")
                        .whereEqualTo("userId", userId)
                    updateRecipeRecycler(query)
                }else{
                    val query = firestore.collection("recipe")
                        .whereGreaterThanOrEqualTo("recipeName", p0.toString())
                        .whereLessThanOrEqualTo("recipeName", p0.toString() + "\uf8ff")
                        .whereEqualTo("userId", userId)
                    updateRecipeRecycler(query)
                }
                return false
            }

        })
    }

    private fun updateRecipeRecycler(query: Query) {
        val options = FirestoreRecyclerOptions.Builder<Recipe>()
            .setQuery(query, Recipe::class.java).setLifecycleOwner(this).build()
        val adapter = object:FirestoreRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
                val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.item_rv_recipe, parent, false)
                return RecipeViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, model: Recipe) {
                val tvDish: TextView = holder.itemView.findViewById(R.id.tv_dish_name)
                val recipeImage: ImageView = holder.itemView.findViewById(R.id.img_dish)
                tvDish.text = model.recipeName
                Glide.with(this@MainActivity).load(model.recipeImage).into(recipeImage)

                holder.itemView.setOnClickListener{
                    val intent = Intent(this@MainActivity, DetailRecipeActivity::class.java)
                    intent.putExtra("position",position.toString())
                    intent.putExtra("recipeId",model.recipeId)
                    intent.putExtra("recipeName",model.recipeName)
                    intent.putExtra("recipeImage",model.recipeImage)
                    intent.putExtra("ingredients",model.ingredients)
                    intent.putExtra("instructions",model.instructions)
                    intent.putExtra("category",model.category)
                    startActivity(intent)
                    finish()
                }
            }
        }
        binding.rvRecipe.hasFixedSize()
        binding.rvRecipe.layoutManager = LinearLayoutManager(this)
        binding.rvRecipe.itemAnimator = null
        binding.rvRecipe.adapter = adapter
    }

    private fun createDialog(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Logout")
        dialog.setMessage("Are you sure?")
        dialog.setPositiveButton("Logout"){ _:DialogInterface, _:Int ->
            val intent = Intent(this, LoginActivity::class.java)
            firebaseAuth.signOut()
            startActivity(intent)
            finish()
        }
        dialog.setNegativeButton("cancel"){ _:DialogInterface, _:Int ->}
        dialog.create().show()
    }
}
