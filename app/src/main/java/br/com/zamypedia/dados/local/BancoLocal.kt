// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/BancoLocal.kt
package br.com.zamypedia.dados.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** O banco de dados que fica dentro do celular. É a "fonte da verdade" do app. */
@Database(entities = [PlantaJardim::class], version = 1, exportSchema = true)
abstract class BancoLocal : RoomDatabase() {
    abstract fun plantaJardimDao(): PlantaJardimDao

    companion object {
        fun criar(context: Context): BancoLocal =
            Room.databaseBuilder(context, BancoLocal::class.java, "zamypedia.db").build()
    }
}
