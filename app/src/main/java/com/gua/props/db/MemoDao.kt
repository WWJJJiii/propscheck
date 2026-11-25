package com.gua.props.db

import android.content.ContentValues
import android.database.Cursor
import com.gua.props.model.Memo
import java.util.UUID

/** 快速备忘读写。未完成的排前面，完成的下沉。 */
class MemoDao(private val helper: DbHelper) {

    private val db get() = helper.writableDatabase

    fun listAll(): List<Memo> {
        val out = ArrayList<Memo>()
        val sql = "SELECT * FROM ${DbHelper.T_MEMO} " +
                "ORDER BY ${DbHelper.M_DONE} ASC, ${DbHelper.M_CREATED_AT} DESC"
        db.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) out.add(read(c))
        }
        return out
    }

    fun countUndone(): Int {
        db.rawQuery(
            "SELECT COUNT(*) FROM ${DbHelper.T_MEMO} WHERE ${DbHelper.M_DONE} = 0", null
        ).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun insert(content: String): Memo {
        val m = Memo(
            id = UUID.randomUUID().toString(),
            content = content,
            done = false,
            createdAt = System.currentTimeMillis()
        )
        val cv = ContentValues().apply {
            put(DbHelper.M_ID, m.id)
            put(DbHelper.M_CONTENT, m.content)
            put(DbHelper.M_DONE, 0)
            put(DbHelper.M_CREATED_AT, m.createdAt)
        }
        db.insert(DbHelper.T_MEMO, null, cv)
        return m
    }

    fun setDone(id: String, done: Boolean) {
        val cv = ContentValues().apply { put(DbHelper.M_DONE, if (done) 1 else 0) }
        db.update(DbHelper.T_MEMO, cv, "${DbHelper.M_ID} = ?", arrayOf(id))
    }

    fun updateContent(id: String, content: String) {
        val cv = ContentValues().apply { put(DbHelper.M_CONTENT, content) }
        db.update(DbHelper.T_MEMO, cv, "${DbHelper.M_ID} = ?", arrayOf(id))
    }

    fun delete(id: String) {
        db.delete(DbHelper.T_MEMO, "${DbHelper.M_ID} = ?", arrayOf(id))
    }

    private fun read(c: Cursor) = Memo(
        id = c.getString(c.getColumnIndexOrThrow(DbHelper.M_ID)),
        content = c.getString(c.getColumnIndexOrThrow(DbHelper.M_CONTENT)),
        done = c.getInt(c.getColumnIndexOrThrow(DbHelper.M_DONE)) == 1,
        createdAt = c.getLong(c.getColumnIndexOrThrow(DbHelper.M_CREATED_AT))
    )
}
