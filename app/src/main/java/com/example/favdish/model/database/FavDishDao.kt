package com.example.favdish.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.favdish.model.entities.FavDish

@Dao
interface FavDishDao {

    @Insert
    suspend fun insertFavDishDetail(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table ORDER BY id")
    fun getAllFavDishes(): LiveData<List<FavDish>>


}