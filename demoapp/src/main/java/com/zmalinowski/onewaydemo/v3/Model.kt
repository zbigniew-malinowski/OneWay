package com.zmalinowski.onewaydemo.v3

data class Value(
        val content: String,
        val checked: Boolean = false,
        val status: Status = Status.SAVING
) {
    enum class Status {
        NORMAL,
        SAVING,
        DELETING
    }
}

sealed class Intention {
    object Get : Intention()
    data class Add(val content: String) : Intention()
    data class Update(val content: String, val checked: Boolean) : Intention()
    data class Remove(val content: String) : Intention()
}
