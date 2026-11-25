package com.gua.props.db

import android.content.ContentValues
import android.database.Cursor
import com.gua.props.model.TplItem
import java.util.UUID

/**
 * 模板读写。没有模板主表：同一个 tpl_name 的若干行 = 一个模板。
 */
class TplItemDao(private val helper: DbHelper) {

    private val db get() = helper.writableDatabase

    /** 弹层用：模板名 + 项数 + 最近使用时间，按最近使用倒序 */
    data class TplBrief(val name: String, val count: Int, val lastUsedAt: Long)

    fun listBrief(): List<TplBrief> {
        val sql = """
            SELECT $T_TPLNAME, COUNT(*), IFNULL(MAX($T_LAST), 0)
            FROM $TBL
            GROUP BY $T_TPLNAME
        """.trimIndent()
        val out = ArrayList<TplBrief>()
        db.rawQuery(sql, null).use { c ->
            while (c.moveToNext()) {
                out.add(TplBrief(c.getString(0), c.getInt(1), c.getLong(2)))
            }
        }
        out.sortByDescending { it.lastUsedAt }
        return out
    }

    /** 某个模板的所有行 */
    fun itemsOf(tplName: String): List<TplItem> {
        val out = ArrayList<TplItem>()
        val sql = "SELECT * FROM $TBL WHERE $T_TPLNAME = ? ORDER BY $T_SORT ASC"
        db.rawQuery(sql, arrayOf(tplName)).use { c ->
            while (c.moveToNext()) out.add(read(c))
        }
        return out
    }

    fun exists(tplName: String): Boolean {
        db.rawQuery("SELECT COUNT(*) FROM $TBL WHERE $T_TPLNAME = ?", arrayOf(tplName)).use { c ->
            return c.moveToFirst() && c.getInt(0) > 0
        }
    }

    /**
     * 存为模板：同名先删后插（等于覆盖）。
     */
    fun saveAsTemplate(tplName: String, rows: List<TplItem>) {
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            db.delete(TBL, "$T_TPLNAME = ?", arrayOf(tplName))
            rows.forEachIndexed { index, row ->
                val cv = ContentValues().apply {
                    put(T_ID, UUID.randomUUID().toString())
                    put(T_TPLNAME, tplName)
                    put(T_TITLE, row.title)
                    put(T_CATEGORY, row.category)
                    put(T_QTY, row.qty)
                    put(T_EXPAND, if (row.expand) 1 else 0)
                    put(T_SORT, index + 1)
                    put(T_CREATED, now)
                    put(T_LAST, now)
                }
                db.insert(TBL, null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** 套用一次，把该模板标记为"最近使用" */
    fun touch(tplName: String) {
        val cv = ContentValues().apply { put(T_LAST, System.currentTimeMillis()) }
        db.update(TBL, cv, "$T_TPLNAME = ?", arrayOf(tplName))
    }

    /** 删整个模板 */
    fun deleteTemplate(tplName: String) {
        db.delete(TBL, "$T_TPLNAME = ?", arrayOf(tplName))
    }

    /** 从模板里删掉某一行 */
    fun deleteRow(id: String) {
        db.delete(TBL, "$T_ID = ?", arrayOf(id))
    }

    // ---------------- 内部 ----------------

    private fun read(c: Cursor) = TplItem(
        id = c.getString(c.getColumnIndexOrThrow(T_ID)),
        tplName = c.getString(c.getColumnIndexOrThrow(T_TPLNAME)),
        title = c.getString(c.getColumnIndexOrThrow(T_TITLE)),
        category = c.getString(c.getColumnIndexOrThrow(T_CATEGORY)),
        qty = c.getInt(c.getColumnIndexOrThrow(T_QTY)),
        expand = c.getInt(c.getColumnIndexOrThrow(T_EXPAND)) == 1,
        sort = c.getInt(c.getColumnIndexOrThrow(T_SORT)),
        createdAt = c.getLong(c.getColumnIndexOrThrow(T_CREATED)),
        lastUsedAt = c.getLong(c.getColumnIndexOrThrow(T_LAST))
    )

    private companion object {
        const val TBL = DbHelper.T_TPL
        const val T_ID = DbHelper.T_ID
        const val T_TPLNAME = DbHelper.T_TPL_NAME
        const val T_TITLE = DbHelper.T_TITLE
        const val T_CATEGORY = DbHelper.T_CATEGORY
        const val T_QTY = DbHelper.T_QTY
        const val T_EXPAND = DbHelper.T_EXPAND
        const val T_SORT = DbHelper.T_SORT
        const val T_CREATED = DbHelper.T_CREATED_AT
        const val T_LAST = DbHelper.T_LAST_USED_AT
    }
}
