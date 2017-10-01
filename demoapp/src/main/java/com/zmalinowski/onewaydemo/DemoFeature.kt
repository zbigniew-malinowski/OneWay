package com.zmalinowski.onewaydemo

import com.zmalinowski.oneway.AbstractFeature
import com.zmalinowski.onewaydemo.Message.Intention
import com.zmalinowski.onewaydemo.Message.Result
import com.zmalinowski.onewaydemo.Value.Status
import io.reactivex.Observable

class DemoFeature(
        intentions: Observable<Intention>,
        private val executor: Executor
) : AbstractFeature<State, Message, Intention, Result>(
        State(),
        intentions
) {
    override fun onMessage(state: State, message: Message): Pair<State, Observable<Result>?> = when (message) {
        is Intention.Get -> state.loading() to executor.get(message)
        is Intention.Add -> state.add(message.content) to executor.add(message)
        is Intention.Remove -> state.remove(message.content) to executor.remove(message)
        is Result.Added -> state.added(message.content) to null
        is Result.Removed -> state.removed(message.content) to null
        is Result.Failed -> state.revert(message.intention) to null
        is Result.Loaded -> state.loaded(message.data) to null
        is Message.Intention.Update -> state.update(message) to executor.update(message)
        is Message.Result.Updated -> state to null
    }
}

private fun State.update(message: Intention.Update): State = copy(data = data.update(message.content, message.checked))

private fun State.loaded(data: List<Value>): State = copy(loading = false, data = data)

private fun State.loading(): State = copy(loading = true, error = false)

private fun State.revert(intention: Intention): State {
    return when (intention) {
        is Message.Intention.Get -> copy(loading = false, error = true)
        is Message.Intention.Add -> remove(intention.content)
        is Message.Intention.Remove -> copy(data = data.update(intention.content, Status.NORMAL))
        is Message.Intention.Update -> copy(data = data.update(intention.content, !intention.checked))
    }
}

private fun State.removed(content: String): State = copy(data = data.remove(content))

private fun State.added(content: String): State = copy(data = data.update(content, Status.NORMAL))

private fun State.remove(content: String): State = copy(data = data.update(content, Status.DELETING))

private fun State.add(content: String): State = copy(data = data.add(content))

fun List<Value>.update(content: String, status: Status): List<Value> {
    val index = indexOfFirst { it.content == content }
    return toMutableList().apply { set(index, get(index).copy(status = status)) }
}

fun List<Value>.update(content: String, checked: Boolean): List<Value> {
    val index = indexOfFirst { it.content == content }
    return toMutableList().apply { set(index, get(index).copy(checked = checked)) }
}


private fun List<Value>.add(content: String): List<Value> = toMutableList().apply { add(Value(content)) }

private fun List<Value>.remove(content: String): List<Value> = filter { it.content != content }