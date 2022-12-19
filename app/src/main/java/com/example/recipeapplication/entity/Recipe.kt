package com.example.recipeapplication.entity

data class Recipe(
    val recipeId : String = "",
    val userId : String = "",
    val recipeName : String = "",
    val recipeImage : String = "",
    val ingredients : String = "",
    val instructions : String = "",
    val category : String = "",
)