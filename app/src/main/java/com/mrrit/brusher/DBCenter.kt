package com.mrrit.brusher

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

class DBCenter {

    private constructor(cont : Context){
        context = cont
        helper = DBCenterHelper(cont,DB_NAME,null, DB_VERSION)
        database = helper!!.readableDatabase
    }


    private var context : Context? = null
    private var helper : DBCenterHelper? = null
    private lateinit var database : SQLiteDatabase
    private val DB_NAME = "DEFAULT_DB_NAME"
    private val DB_VERSION = 1
    // Meta Data for table bookmarks starts here
    private val TABLE_BOOKMARKS = "TABLE_BOOKMARKS"
    private val BOOKMARKS_COLUMN_TITLE = "TABLE_BOOKMARKS_COLUMN_TITLE"
    private val BOOKMARKS_COLUMN_ID = "TABLE_BOOKMARKS_COLUMN_ID"
    private val BOOKMARKS_COLUMN_URL = "TABLE_BOOKMARKS_COLUMN_URL"
    // Meta Data for table bookmarks ends here

    // Meta Data for table history starts here

    private val TABLE_HISTORY = "TABLE_HISTORY"
    private val HISTORY_COLUMN_TITLE = "TABLE_HISTORY_COLUMN_TITLE"
    private val HISTORY_COLUMN_ID = "TABLE_HISTORY_COLUMN_ID"
    private val HISTORY_COLUMN_URL = "TABLE_HISTORY_COLUMN_URL"
    private val HISTORY_COLUMN_DATE = "TABLE_HISTORY_COLUMN_DATE"
    // Meta Data for table bookmarks ends here

    private val TABLE_BOOKMARKS_CREATION_COMMAND = """CREATE TABLE $TABLE_BOOKMARKS($BOOKMARKS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT , $BOOKMARKS_COLUMN_TITLE TEXT , $BOOKMARKS_COLUMN_URL NOT NULL , UNIQUE ($BOOKMARKS_COLUMN_URL))"""

    private val TABLE_HISTORY_CREATION_COMMAND = """CREATE TABLE $TABLE_HISTORY($HISTORY_COLUMN_ID INTEGER  PRIMARY KEY AUTOINCREMENT , $HISTORY_COLUMN_TITLE TEXT , $HISTORY_COLUMN_URL NOT NULL , $HISTORY_COLUMN_DATE NOT NULL)"""


    fun addToBookmarks(title : String , url : String) : Boolean{
        val cntValues = ContentValues()
        cntValues.put(BOOKMARKS_COLUMN_TITLE, title)
        cntValues.put(BOOKMARKS_COLUMN_URL, url)
        return database.insert(TABLE_BOOKMARKS,null, cntValues) >=  0

    }

    fun addToHistory(title : String , url : String, date : String) : Boolean{
        val cntValues = ContentValues()
        cntValues.put(HISTORY_COLUMN_TITLE, title)
        cntValues.put(HISTORY_COLUMN_URL, url)
        cntValues.put(HISTORY_COLUMN_DATE, date)
        return database.insert(TABLE_HISTORY,null, cntValues) >=  0
    }

    fun deleteFromBookmarksViaID(id : Int) : Boolean{
        return database.delete(TABLE_BOOKMARKS,"$BOOKMARKS_COLUMN_ID = $id", null) > 0
    }
    fun deleteFromBookmarksViaUrl(url : String) : Boolean{
        return database.delete(TABLE_BOOKMARKS,"$BOOKMARKS_COLUMN_URL = '$url'", null) > 0
    }

    fun getAllBookmarks() : ArrayList<BookmarksDS>?{
        val crsr = database.query(false, TABLE_BOOKMARKS, arrayOf(BOOKMARKS_COLUMN_ID, BOOKMARKS_COLUMN_TITLE, BOOKMARKS_COLUMN_URL)  ,null, null, null , null  , null , null)
        val dataToReturn = ArrayList<BookmarksDS>()
        if(crsr != null && crsr.count > 0){
            while (!crsr.isLast){
                crsr.moveToNext()
                dataToReturn.add(BookmarksDS(crsr.getInt(0), crsr.getString(1), crsr.getString(2)))
            }
            crsr.close()
            return dataToReturn
        }
        crsr?.close()
        return null
    }

    fun getAllHistory() : ArrayList<HistoryDS>?{
        val crsr = database.query(false, TABLE_HISTORY, arrayOf(HISTORY_COLUMN_ID, HISTORY_COLUMN_TITLE, HISTORY_COLUMN_URL, HISTORY_COLUMN_DATE)  ,null, null, null , null  , null , null)
        val dataToReturn = ArrayList<HistoryDS>()
        if(crsr != null && crsr.count > 0){
            while (!crsr.isLast){
                crsr.moveToNext()
                dataToReturn.add(HistoryDS(crsr.getInt(0), crsr.getString(1), crsr.getString(2), crsr.getString(3)))
            }
            crsr.close()
            return dataToReturn
        }
        crsr?.close()
        return null
    }

    fun updateBookmark(id: Int, title: String, url: String) : Boolean{
        val cntValues = ContentValues()
        cntValues.put(BOOKMARKS_COLUMN_TITLE, title)
        cntValues.put(BOOKMARKS_COLUMN_URL, url)
        return database.update(TABLE_BOOKMARKS,cntValues, "$BOOKMARKS_COLUMN_ID = $id", null) > 0
    }

    fun updateHistory(id: Int, title: String, url: String, date : String) : Boolean{
        val cntValues = ContentValues()
        cntValues.put(HISTORY_COLUMN_TITLE, title)
        cntValues.put(HISTORY_COLUMN_URL, url)
        cntValues.put(HISTORY_COLUMN_DATE, date)
        return database.update(TABLE_HISTORY,cntValues, "$HISTORY_COLUMN_ID = $id", null) > 0
    }


    fun deleteFromHistoryViaID(id : Int) : Boolean{
        return database.delete(TABLE_HISTORY,"$HISTORY_COLUMN_ID = $id", null) > 0
    }
    fun deleteFromHistoryViaUrl(url : String) : Boolean{
        return database.delete(TABLE_HISTORY,"$HISTORY_COLUMN_URL = '$url'", null) > 0
    }

    fun clearHistory(): Boolean {
        return database.delete(TABLE_HISTORY, null,null) >0
    }


    private inner class DBCenterHelper(cont : Context, dbName : String, factory : CursorFactory?, dbVersion: Int) : SQLiteOpenHelper(cont, dbName, factory, dbVersion){

        override fun onConfigure(db: SQLiteDatabase?) {
            super.onConfigure(db)
            db?.setForeignKeyConstraintsEnabled(true)
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(TABLE_HISTORY_CREATION_COMMAND)
            db?.execSQL(TABLE_BOOKMARKS_CREATION_COMMAND)
        }

        override fun onUpgrade(p0: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        }

    }

    companion object{
        private var currClassInstance : DBCenter? = null
        fun getDBCenter(cntx : Context) : DBCenter{
            return currClassInstance ?: DBCenter(cntx).also { currClassInstance = it }
        }
    }

}