package com.zmalinowski.onewaydemo.v2

data class State(
        val data: List<Value> = emptyList(),
        val loading: Boolean = false,
        val error: Boolean = false
)

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

sealed class Action {
    object Loading : Action()
    data class Loaded(val data: List<Value>) : Action()
    data class Adding(val content: String) : Action()
    data class Added(val content: String) : Action()
    data class Removed(val content: String) : Action()
    data class Removing(val content: String) : Action()
    data class Failed(val intention: Intention) : Action()
    data class Update(val content: String, val checked: Boolean) : Action()
}