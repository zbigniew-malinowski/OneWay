package com.zmalinowski.oneway

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class Feature<Intention, Action, State>(
     intentions : Observable<Intention>,
     reducer : Reducer<State, Action>,
     actor : Actor<Intention, State, Action>
) : Disposable {

    private val dispose = BehaviorSubject.create<Unit>()
    val state : Observable<State> = intentions
            .takeUntil(dispose)
            .flatMap(actor)
            .scan(actor.state, reducer)
            .doOnNext{ actor.state = it}
            .replay(1)
            .autoConnect(0)
            .distinctUntilChanged()
            .share()

    override fun isDisposed() = dispose.hasValue()
    override fun dispose() = dispose.onNext(Unit)
}

typealias Reducer<State, Action> = (State, Action) -> State
abstract class Actor<in Intention, State, Action>(var state: State) : (Intention) -> Observable<Action>