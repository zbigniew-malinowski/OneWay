package com.zmalinowski.onewaydemo.v3

import com.zmalinowski.oneway.Command
import com.zmalinowski.oneway.Executor
import com.zmalinowski.onewaydemo.DataSource
import io.reactivex.Observable

object Actor : Executor<Intention, State> {

    private val dataSource = DataSource()

    override fun invoke(intent: Intention): Observable<Command<State>> = when (intent) {
        is Intention.Get -> onGet(intent)
        is Intention.Add -> onAdd(intent)
        is Intention.Update -> onUpdate(intent)
        is Intention.Remove -> onRemove(intent)
    }

    fun onGet(message: Intention.Get): Observable<Command<State>> =
            dataSource.get()
                    .map { loaded(it) }
                    .onErrorReturn { revert(message) }
                    .startWith(State::loading)

    fun onAdd(message: Intention.Add): Observable<Command<State>> =
            dataSource.add(Value(message.content))
                    .map { added(message) }
                    .onErrorReturn { revert(message) }
                    .startWith(add(message))

    fun onRemove(message: Intention.Remove): Observable<Command<State>> =
            dataSource.remove(message.content)
                    .map { removed(message) }
                    .onErrorReturn { revert(message) }
                    .startWith(remove(message))

    fun onUpdate(message: Intention.Update): Observable<Command<State>> =
            dataSource
                    .update(message)
                    .ignoreElements().toObservable<Command<State>>()
                    .onErrorReturn { revert(message) }
                    .startWith(update(message))

    private fun loaded(list: List<Value>): Command<State> = { it.loaded(list) }
    private fun revert(message: Intention): Command<State> = { it.revert(message) }
    private fun added(message: Intention.Add): Command<State> = { it.added(message) }
    private fun add(message: Intention.Add): Command<State> = { it.add(message) }
    private fun removed(message: Intention.Remove): Command<State> = { it.removed(message) }
    private fun remove(message: Intention.Remove): Command<State> = { it.remove(message) }
    private fun update(message: Intention.Update): Command<State> = { it.update(message) }
}
