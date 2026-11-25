package com.gua.props.db

import android.content.ContentValues
import android.database.Cursor
import com.gua.props.model.PlanItem
import java.util.UUID

/**
 * 今日清单的读写。所有 SQL 只出现在这个文件里，界面层不碰 Cursor。
 *
 * 说明：MVP 数据量小（一天几十条），直接在主线程读写；手机上如果感觉卡，
 * 再把调用点包进 lifecycleScope.launch(Dispatchers.IO)。
 */
class PlanItemDao(private val helper: DbHelper) {

    private val db get() = helper.writableDatabase

    // ---------------- 查 ----------------

    fun listByDate(date: String): List<PlanItem> {
        val sql = "SELECT * FROM ${DbHelper.T_PLAN} " +
                "WHERE ${DbHelper.P_DATE} = ? " +
                "ORDER BY ${DbHelper.P_SORT} ASC, ${DbHelper.P_CREATED_AT} ASC"
        val out = ArrayList<PlanItem>()
        db.rawQuery(sql, arrayOf(date)).use { c ->
            while (c.moveToNext()) out.add(read(c))
        }
        return out
    }

    /** 返回 (已完成数, 总数) */
    fun counts(date: String): Pair<Int, Int> {
        val sql = "SELECT COUNT(*), SUM(${DbHelper.P_DONE}) FROM ${DbHelper.T_PLAN} " +
                "WHERE ${DbHelper.P_DATE} = ?"
        db.rawQuery(sql, arrayOf(date)).use { c ->
            if (c.moveToFirst()) {
                val total = c.getInt(0)
                val done = if (c.isNull(1)) 0 else c.getInt(1)
                return done to total
            }
        }
        return 0 to 0
    }

    fun existsDate(date: String): Boolean {
        val sql = "SELECT COUNT(*) FROM ${DbHelper.T_PLAN} WHERE ${DbHelper.P_DATE} = ? LIMIT 1"
        db.rawQuery(sql, arrayOf(date)).use { c ->
            return c.moveToFirst() && c.getInt(0) > 0
        }
    }

    // ---------------- 增 ----------------

    /** 加一条，自动补 id / sort / createdAt */
    fun insert(item: PlanItem): PlanItem {
        val now = System.currentTimeMillis()
        val filled = item.copy(
            id = item.id.ifBlank { UUID.randomUUID().toString() },
            sort = item.sort.takeIf { it > 0 } ?: (maxSort(item.date) + 1),
            createdAt = item.createdAt.takeIf { it > 0 } ?: now
        )
        db.insert(DbHelper.T_PLAN, null, toValues(filled))
        return filled
    }

    /**
     * 套用模板：批量插入，同名 + 同类已存在就跳过。
     * 调用前请把"按件展开"处理完（帽子 #1…#5 已经拆好行）。
     */
    fun addFromTemplate(date: String, items: List<PlanItem>): AddResult {
        val existing = HashSet<String>()
        db.rawQuery(
            "SELECT ${DbHelper.P_TITLE}, ${DbHelper.P_CATEGORY} FROM ${DbHelper.T_PLAN} " +
                    "WHERE ${DbHelper.P_DATE} = ?", arrayOf(date)
        ).use { c ->
            while (c.moveToNext()) existing.add(key(c.getString(0), c.getString(1)))
        }

        var sort = maxSort(date)
        var added = 0
        var skipped = 0
        val now = System.currentTimeMillis()

        db.beginTransaction()
        try {
            for (it in items) {
                if (key(it.title, it.category) in existing) {
                    skipped++
                    continue
                }
                sort++
                val row = it.copy(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    done = false,
                    doneAt = 0L,
                    sort = sort,
                    createdAt = now
                )
                db.insert(DbHelper.T_PLAN, null, toValues(row))
                existing.add(key(row.title, row.category))
                added++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return AddResult(added, skipped)
    }

    // ---------------- 改 ----------------

    /** 勾选 / 取消勾选；doneAt 只在勾上时记时间 */
    fun setDone(id: String, done: Boolean) {
        val cv = ContentValues().apply {
            put(DbHelper.P_DONE, if (done) 1 else 0)
            put(DbHelper.P_DONE_AT, if (done) System.currentTimeMillis() else 0L)
        }
        db.update(DbHelper.T_PLAN, cv, "${DbHelper.P_ID} = ?", arrayOf(id))
    }

    /** 改标题 / 分类 / 数量 */
    fun update(item: PlanItem) {
        val cv = ContentValues().apply {
            put(DbHelper.P_TITLE, item.title)
            put(DbHelper.P_CATEGORY, item.category)
            put(DbHelper.P_QTY, item.qty)
            put(DbHelper.P_NOTE, item.note)
        }
        db.update(DbHelper.T_PLAN, cv, "${DbHelper.P_ID} = ?", arrayOf(item.id))
    }

    // ---------------- 删 ----------------

    fun delete(id: String) {
        db.delete(DbHelper.T_PLAN, "${DbHelper.P_ID} = ?", arrayOf(id))
    }

    /** 删整天（留着给"清空今天"用） */
    fun deleteDate(date: String) {
        db.delete(DbHelper.T_PLAN, "${DbHelper.P_DATE} = ?", arrayOf(date))
    }

    // ---------------- 内部 ----------------

    private fun maxSort(date: String): Int {
        db.rawQuery(
            "SELECT IFNULL(MAX(${DbHelper.P_SORT}), 0) FROM ${DbHelper.T_PLAN} " +
                    "WHERE ${DbHelper.P_DATE} = ?", arrayOf(date)
        ).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    private fun key(title: String, category: String) = "$title|$category"

    private fun toValues(i: PlanItem) = ContentValues().apply {
        put(DbHelper.P_ID, i.id)
        put(DbHelper.P_DATE, i.date)
        put(DbHelper.P_TITLE, i.title)
        put(DbHelper.P_CATEGORY, i.category)
        put(DbHelper.P_QTY, i.qty)
        put(DbHelper.P_DONE, if (i.done) 1 else 0)
        put(DbHelper.P_NOTE, i.note)
        put(DbHelper.P_SORT, i.sort)
        put(DbHelper.P_CREATED_AT, i.createdAt)
        put(DbHelper.P_DONE_AT, i.doneAt)
    }

    private fun read(c: Cursor) = PlanItem(
        id = c.getString(c.getColumnIndexOrThrow(DbHelper.P_ID)),
        date = c.getString(c.getColumnIndexOrThrow(DbHelper.P_DATE)),
        title = c.getString(c.getColumnIndexOrThrow(DbHelper.P_TITLE)),
        category = c.getString(c.getColumnIndexOrThrow(DbHelper.P_CATEGORY)),
        qty = c.getInt(c.getColumnIndexOrThrow(DbHelper.P_QTY)),
        done = c.getInt(c.getColumnIndexOrThrow(DbHelper.P_DONE)) == 1,
        note = c.getString(c.getColumnIndexOrThrow(DbHelper.P_NOTE)) ?: "",
        sort = c.getInt(c.getColumnIndexOrThrow(DbHelper.P_SORT)),
        createdAt = c.getLong(c.getColumnIndexOrThrow(DbHelper.P_CREATED_AT)),
        doneAt = c.getLong(c.getColumnIndexOrThrow(DbHelper.P_DONE_AT))
    )

    /** 套用模板的结果，用来提示"已加 9 条，跳过 3 条重复" */
    data class AddResult(val added: Int, val skipped: Int)
}
