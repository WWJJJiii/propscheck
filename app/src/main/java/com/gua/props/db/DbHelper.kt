package com.gua.props.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 唯一的建库入口。三张表：
 *   plan_item  今日清单 / 任务
 *   tpl_item   模板（一行一项，靠 tpl_name 分组）
 *   memo       快速备忘
 *
 * 第一次调用 writableDatabase 时自动建表。
 */
class DbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        SQL_PLAN.let(db::execSQL)
        SQL_TPL.let(db::execSQL)
        SQL_MEMO.let(db::execSQL)
        IDX_PLAN.let(db::execSQL)
        IDX_TPL.let(db::execSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // MVP 阶段：版本变了直接重建（会丢数据）。
        // 等你手机上有真实数据后，这里改成按版本 ALTER TABLE，别再用 DROP。
        db.execSQL("DROP TABLE IF EXISTS $T_PLAN")
        db.execSQL("DROP TABLE IF EXISTS $T_TPL")
        db.execSQL("DROP TABLE IF EXISTS $T_MEMO")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "shoot.db"
        const val DB_VERSION = 1

        // ---------------- plan_item ----------------
        const val T_PLAN = "plan_item"
        const val P_ID = "id"
        const val P_DATE = "date"
        const val P_TITLE = "title"
        const val P_CATEGORY = "category"
        const val P_QTY = "qty"
        const val P_DONE = "done"
        const val P_NOTE = "note"
        const val P_SORT = "sort"
        const val P_CREATED_AT = "created_at"
        const val P_DONE_AT = "done_at"

        private val SQL_PLAN = """
            CREATE TABLE $T_PLAN (
                $P_ID         TEXT PRIMARY KEY NOT NULL,
                $P_DATE       TEXT NOT NULL,
                $P_TITLE      TEXT NOT NULL,
                $P_CATEGORY   TEXT NOT NULL DEFAULT '道具',
                $P_QTY        INTEGER NOT NULL DEFAULT 1,
                $P_DONE       INTEGER NOT NULL DEFAULT 0,
                $P_NOTE       TEXT,
                $P_SORT       INTEGER NOT NULL DEFAULT 0,
                $P_CREATED_AT INTEGER NOT NULL DEFAULT 0,
                $P_DONE_AT    INTEGER
            )
        """.trimIndent()

        // ---------------- tpl_item ----------------
        const val T_TPL = "tpl_item"
        const val T_ID = "id"
        const val T_TPL_NAME = "tpl_name"
        const val T_TITLE = "title"
        const val T_CATEGORY = "category"
        const val T_QTY = "qty"
        const val T_EXPAND = "expand"
        const val T_SORT = "sort"
        const val T_CREATED_AT = "created_at"
        const val T_LAST_USED_AT = "last_used_at"

        private val SQL_TPL = """
            CREATE TABLE $T_TPL (
                $T_ID           TEXT PRIMARY KEY NOT NULL,
                $T_TPL_NAME     TEXT NOT NULL,
                $T_TITLE        TEXT NOT NULL,
                $T_CATEGORY     TEXT NOT NULL DEFAULT '道具',
                $T_QTY          INTEGER NOT NULL DEFAULT 1,
                $T_EXPAND       INTEGER NOT NULL DEFAULT 0,
                $T_SORT         INTEGER NOT NULL DEFAULT 0,
                $T_CREATED_AT   INTEGER NOT NULL DEFAULT 0,
                $T_LAST_USED_AT INTEGER
            )
        """.trimIndent()

        // ---------------- memo ----------------
        const val T_MEMO = "memo"
        const val M_ID = "id"
        const val M_CONTENT = "content"
        const val M_DONE = "done"
        const val M_CREATED_AT = "created_at"

        private val SQL_MEMO = """
            CREATE TABLE $T_MEMO (
                $M_ID         TEXT PRIMARY KEY NOT NULL,
                $M_CONTENT    TEXT NOT NULL,
                $M_DONE       INTEGER NOT NULL DEFAULT 0,
                $M_CREATED_AT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        // ---------------- 索引 ----------------
        private val IDX_PLAN = "CREATE INDEX idx_plan_date ON $T_PLAN($P_DATE, $P_SORT)"
        private val IDX_TPL = "CREATE INDEX idx_tpl_name ON $T_TPL($T_TPL_NAME, $T_SORT)"

        // ---------------- 单例 ----------------
        @Volatile
        private var instance: DbHelper? = null

        fun get(ctx: Context): DbHelper =
            instance ?: synchronized(this) {
                instance ?: DbHelper(ctx).also { instance = it }
            }
    }
}
