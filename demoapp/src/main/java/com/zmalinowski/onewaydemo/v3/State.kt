package com.zmalinowski.onewaydemo.v3

data class State(
        val data: List<Value> = emptyList(),
        val loading: Boolean = false,
        val error: Boolean = false
) {
    fun update(update: Intention.Update): State = copy(data = data.update(update.content, update.checked))

    fun loaded(data: List<Value>): State = copy(loading = false, data = data)

    fun loading(): State = copy(loading = true, error = false)

    fun revert(intention: Intention): State {
        return when (intention) {
            is Intention.Get -> copy(loading = false, error = true)
            is Intention.Add -> copy(data = data.update(intention.content, Value.Status.DELETING))
            is Intention.Remove -> copy(data = data.update(intention.content, Value.Status.NORMAL))
            is Intention.Update -> copy(data = data.update(intention.content, !intention.checked))
        }
    }

    fun removed(intention: Intention.Remove): State = copy(data = data.remove(intention.content))

    fun added(intention: Intention.Add): State = copy(data = data.update(intention.content, Value.Status.NORMAL))

    fun remove(intention: Intention.Remove): State = copy(data = data.update(intention.content, Value.Status.DELETING))

    fun add(intention: Intention.Add): State = copy(data = data.add(intention.content))

    private fun List<Value>.add(content: String): List<Value> = toMutableList().apply { add(Value(content)) }

    private fun List<Value>.remove(content: String): List<Value> = filter { it.content != content }

    private fun List<Value>.update(content: String, status: Value.Status): List<Value> {
        val index = indexOfFirst { it.content == content }
        return toMutableList().apply { set(index, get(index).copy(status = status)) }
    }

    private fun List<Value>.update(content: String, checked: Boolean): List<Value> {
        val index = indexOfFirst { it.content == content }
        return toMutableList().apply { set(index, get(index).copy(checked = checked)) }
    }
}

