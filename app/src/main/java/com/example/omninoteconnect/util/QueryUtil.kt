package com.example.omninoteconnect.util

import androidx.sqlite.db.SimpleSQLiteQuery

object QueryUtil {

    fun sortedQuery(sortType: SortType): SimpleSQLiteQuery {
        val query = StringBuilder().append("SELECT * FROM notes ")
        when (sortType) {
            SortType.TIME -> query.append("ORDER BY date, strftime('%H:%M', startTime)")
            SortType.TITLE -> query.append("ORDER BY title COLLATE NOCASE")
        }

        return SimpleSQLiteQuery(query.toString())
    }

    fun nearestQuery(type: QueryType): SimpleSQLiteQuery {
        var query = ""
        when (type) {
            QueryType.CURRENT_DAY -> query = """
                 SELECT * FROM course 
                 WHERE day = (strftime('%w', 'now', 'localtime') + 1)
                 AND strftime('%H:%M', startTime) > strftime('%H:%M', 'now', 'localtime')
                 ORDER BY strftime('%H:%M', startTime) ASC LIMIT 1
                 """

            QueryType.NEXT_DAY -> query = """
                 SELECT * FROM course 
                 WHERE day > (strftime('%w', 'now', 'localtime') + 1)
                 ORDER BY day,strftime('%H:%M', startTime) ASC LIMIT 1
                 """

            QueryType.PAST_DAY -> query = """
                 SELECT * FROM course 
                 WHERE day >= 0
                 ORDER BY day, strftime('%H:%M', startTime) ASC LIMIT 1
                 """
        }

        return SimpleSQLiteQuery(query)
    }
}

enum class QueryType {
    CURRENT_DAY,
    NEXT_DAY,
    PAST_DAY
}