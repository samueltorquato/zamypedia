// Arquivo: app/src/main/java/br/com/zamypedia/dados/local/BancoLocal.kt
package br.com.zamypedia.dados.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** O banco de dados que fica dentro do celular. É a "fonte da verdade" do app. */
@Database(entities = [PlantaJardim::class, Registro::class], version = 2, exportSchema = true)
abstract class BancoLocal : RoomDatabase() {
    abstract fun plantaJardimDao(): PlantaJardimDao
    abstract fun registroDao(): RegistroDao

    companion object {
        fun criar(context: Context): BancoLocal =
            Room.databaseBuilder(context, BancoLocal::class.java, "zamypedia.db")
                .addMigrations(MIGRACAO_1_2)
                .build()

        /**
         * Versão 1 para 2 (segunda entrega): acrescenta os campos dos Blocos 01 e 02
         * e cria a tabela de registros. As plantas já cadastradas são mantidas.
         */
        val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("taxonomia", "luz", "agua", "solo", "nutrientes", "sinais_folhas", "sinais_pragas", "floracao")
                    .forEach { db.execSQL("ALTER TABLE `planta_jardim` ADD COLUMN `$it` TEXT") }
                listOf("desde_ano", "desde_mes", "desde_dia")
                    .forEach { db.execSQL("ALTER TABLE `planta_jardim` ADD COLUMN `$it` INTEGER") }

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `registro` (" +
                        "`id` TEXT NOT NULL, `planta_id` TEXT NOT NULL, `tipo` TEXT NOT NULL, `data` INTEGER NOT NULL, " +
                        "`criado_em` INTEGER NOT NULL, `alterado_em` INTEGER NOT NULL, `apagado` INTEGER NOT NULL, " +
                        "`pendente_envio` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registro_planta_id` ON `registro` (`planta_id`)")
            }
        }
    }
}
