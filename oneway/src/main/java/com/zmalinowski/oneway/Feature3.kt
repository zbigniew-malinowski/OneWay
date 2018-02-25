package com.zmalinowski.oneway

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class Feature3<Intention, State>(
        intentions: Observable<Intention>,
        executor: Executor<Intention, State>, initialState: State
) : Disposable {

    private val dispose = BehaviorSubject.create<Unit>()
    val state: Observable<State> = intentions
            .takeUntil(dispose)
            .flatMap(executor)
            .scan(initialState) { state, command -> command(state) }
            .replay(1)
            .autoConnect(0)
            .distinctUntilChanged()
            .share()

    override fun isDisposed() = dispose.hasValue()
    override fun dispose() = dispose.onNext(Unit)
}

typealias Command<State> = (State) -> State
interface Executor<in Intention, State> : (Intention) -> Observable<Command<State>>
