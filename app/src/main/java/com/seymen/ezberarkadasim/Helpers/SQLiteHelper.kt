package com.seymen.ezberarkadasim.Helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.seymen.ezberarkadasim.model.WordsModel
import java.lang.Exception

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {
     var kelimelerDB : ArrayList<String> = ArrayList()
     var idDB : ArrayList<Int> = ArrayList()
    companion object{
        private const val DATABASE_NAME = "kelimeDB"
        private const val DATABASE_VERSION = 1
        private const val ID  = "id"
        private const val TBLKELIME = "tbl_kelime"
        private const val KELIMELER = "kelimeler"
    }
    override fun onCreate(p0: SQLiteDatabase?) {
    p0?.execSQL("CREATE TABLE IF NOT EXISTS ${TBLKELIME} ($ID INTEGER PRIMARY KEY, $KELIMELER TEXT)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    p0!!.execSQL("DROP TABLE IF EXISTS $TBLKELIME")
        onCreate(p0)
    }

    fun addWord(klme : WordsModel) : Long{
        val db=this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, klme.idModel)
        contentValues.put(KELIMELER, klme.kelimeModel)

        val success = db.insert(TBLKELIME, null, contentValues)
        db.close()
        return success
    }

    fun getAllWord() : ArrayList<WordsModel>{
        val klmList : ArrayList<WordsModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBLKELIME"
        val db = this.readableDatabase
        val cursor : Cursor?
        try {
            cursor = db.rawQuery(selectQuery,null)
        }
        catch (e : Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var kelime : String
        var id : Int
        if(cursor.moveToFirst()){
            val idColumnIndex = cursor.getColumnIndex("id")
            val kelimeColumnIndex = cursor.getColumnIndex("kelimeler")
            do {
                id = cursor.getInt(idColumnIndex)
                kelime = cursor.getString(kelimeColumnIndex)
                val klm = WordsModel(idModel = id, kelimeModel = kelime)
                klmList.add(klm)
            }
                while (cursor.moveToNext())
        }
        return klmList
    }

    fun getOnlyWord() : ArrayList<String>{
        val db = this.readableDatabase
        val  cursor = db.rawQuery("SELECT kelimeler FROM tbl_kelime",null)
        try {
            var kelime : String
            val kelimeColumnIndex = cursor.getColumnIndex("kelimeler")
            while (cursor.moveToNext()){
                kelime = cursor.getString(kelimeColumnIndex)
                kelimelerDB.add(kelime)
            }
            cursor.close()
        }
        catch (e : Exception){
            e.printStackTrace()
        }
        return kelimelerDB
    }
    fun getOnlyID() : ArrayList<Int>{
        val db = this.readableDatabase
        val  cursor = db.rawQuery("SELECT id FROM tbl_kelime",null)
        try {
            var id : Int
            val kelimeColumnIndex = cursor.getColumnIndex("id")
            while (cursor.moveToNext()){
                id = cursor.getInt(kelimeColumnIndex)
                idDB.add(id)
            }
            cursor.close()
        }
        catch (e : Exception){
            e.printStackTrace()
        }
        return idDB
    }

    fun deleteWordByID (id : Int) : Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID,id)
        val success = db.delete(TBLKELIME, "id=$id",null)
        db.close()
        return success
    }
}