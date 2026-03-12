package com.example.timemarkbase.config.fb_ext

import com.example.timemarkbase.config.model.User
import com.example.timemarkbase.config.model.UserConfig
import org.json.JSONObject

fun parseUserConfig(jsonString: String): UserConfig {
    val root = JSONObject(jsonString)
    val array = root.getJSONArray("list_user_id")

    val users = mutableListOf<User>()

    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        users.add(
            User(
                userId = obj.getString("user_id"),
                activeKey = obj.getString("active_key"),
                dueDate = obj.getString("due_date")
            )
        )
    }

    return UserConfig(listUserId = users)
}