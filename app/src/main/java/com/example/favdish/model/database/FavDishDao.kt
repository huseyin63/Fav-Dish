package com.example.favdish.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.favdish.model.entities.FavDish

@Dao
interface FavDishDao {

    @Insert
    suspend fun insertFavDishDetail(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table ORDER BY id")
    fun getAllFavDishes(): LiveData<List<FavDish>>

    @Update
    suspend fun updateFavDish(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table WHERE favorite_dish")
    fun getFavoriteDishesList(): LiveData<List<FavDish>>


}