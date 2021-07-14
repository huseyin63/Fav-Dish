package com.example.favdish.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.entities.FavDish
import kotlinx.coroutines.launch


class FavDishViewModel : ViewModel() {

    private val repository = FavDishRepository


    suspend fun insert(favDish: FavDish) {
        repository.insertFavDishData(favDish)
    }
}



