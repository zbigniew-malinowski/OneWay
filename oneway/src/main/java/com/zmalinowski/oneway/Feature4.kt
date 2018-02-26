package com.zmalinowski.oneway

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class Feature4<Intention, State>(private val reducer: Reducer4<Intention, State>) : Observer<Intention>, ObservableSource<State> {


    private val intentions = PublishSubject.create<Intention>()

    private val states: Observable<State> =
            intentions.flatMap { reducer(it) }
                    .replay(1)
                    .autoConnect(0)
                    .distinctUntilChanged()

    override fun subscribe(observer: Observer<in State>) = states.subscribe(observer)

    override fun onError(e: Throwable) = intentions.onError(e)

    override fun onComplete() = intentions.onComplete()

    override fun onSubscribe(d: Disposable) = Unit

    override fun onNext(t: Intention) = intentions.onNext(t)
}

open class Reducer4<in Intention, State>(
        var state: State,
        private val actor: Actor4<Intention, State>
) : (Intention) -> Observable<State> {
    override fun invoke(intention: Intention): Observable<State> = actor(intention, state).map { it(state).also { state = it } }
}

interface Actor4<in Intention, State> : (Intention, State) -> Observable<Effect<State>>

interface Effect<State> : (State) -> State
