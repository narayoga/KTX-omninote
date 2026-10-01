package com.example.omninoteconnect.util

import androidx.sqlite.db.SimpleSQLiteQuery

object QueryUtil {

    fun sortedQuery(sortType: SortType): SimpleSQLiteQuery {
        val query = StringBuilder().append("SELECT * FROM notes ")
        when (sortType) {
            SortType.TIME -> query.append("ORDER BY isDone, date, strftime('%H:%M', startTime)")
            SortType.TITLE -> query.append("ORDER BY title COLLATE NOCASE")
        }

        return SimpleSQLiteQuery(query.toString())
    }
}
