package com.zmalinowski.onewaydemo.v2

import com.zmalinowski.oneway.Reducer

class DemoReducer:Reducer<State, Action> {
    override fun invoke(state: State, action: Action): State = when (action) {
        is Action.Loading -> state.loading()
        is Action.Loaded -> state.loaded(action.data)
        is Action.Adding -> state.add(action.content)
        is Action.Added -> state.added(action.content)
        is Action.Removed -> state.removed(action.content)
        is Action.Removing -> state.remove(action.content)
        is Action.Failed -> state.revert(action.intention)
        is Action.Update -> state.update(action)
    }
}

private fun State.update(action: Action.Update): State = copy(data = data.update(action.content, action.checked))

private fun State.loaded(data: List<Value>): State = copy(loading = false, data = data)

private fun State.loading(): State = copy(loading = true, error = false)

private fun State.revert(intention: Intention): State {
    return when (intention) {
        is Intention.Get -> copy(loading = false, error = true)
        is Intention.Add -> remove(intention.content)
        is Intention.Remove -> copy(data = data.update(intention.content, Value.Status.NORMAL))
        is Intention.Update -> copy(data = data.update(intention.content, !intention.checked))
    }
}

private fun State.removed(content: String): State = copy(data = data.remove(content))

private fun State.added(content: String): State = copy(data = data.update(content, Value.Status.NORMAL))

private fun State.remove(content: String): State = copy(data = data.update(content, Value.Status.DELETING))

private fun State.add(content: String): State = copy(data = data.add(content))

fun List<Value>.update(content: String, status: Value.Status): List<Value> {
    val index = indexOfFirst { it.content == content }
    return toMutableList().apply { set(index, get(index).copy(status = status)) }
}

fun List<Value>.update(content: String, checked: Boolean): List<Value> {
    val index = indexOfFirst { it.content == content }
    return toMutableList().apply { set(index, get(index).copy(checked = checked)) }
}

private fun List<Value>.add(content: String): List<Value> = toMutableList().apply { add(Value(content)) }

private fun List<Value>.remove(content: String): List<Value> = filter { it.content != content }