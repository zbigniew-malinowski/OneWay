package com.zmalinowski.onewaydemo.v1

data class State(
        val data: List<Value> = emptyList(),
        val loading: Boolean = false,
        val error: Boolean = false
)

data class Value(
        val content: String,
        val checked : Boolean = false,
        val status: Status = Status.SAVING
) {
    enum class Status {
        NORMAL,
        SAVING,
        DELETING
    }
}

sealed class Message {

    sealed class Intention : Message() {
        object Get : Intention()
        data class Add(val content: String) : Intention()
        data class Update(val content: String, val checked : Boolean) : Intention()
        data class Remove(val content: String) : Intention()
    }

    sealed class Result : Message() {
        data class Loaded(val data: List<Value>) : Result()
        data class Added(val content: String) : Result()
        data class Removed(val content: String) : Result()
        data class Failed(val intention: Intention) : Result()
        data class Updated(val content: String, val checked : Boolean) : Result()
    }
}