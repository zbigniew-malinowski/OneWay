package com.zmalinowski.oneway

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class AbstractFeature<State, Message, in Intention : Message, Result : Message>(
        initialState: State,
        private val intentions: Observable<Intention>,
        private val scheduler: Scheduler = Schedulers.trampoline()
) : Disposable {
    private val effects = PublishSubject.create<Result>()
    private val signal = BehaviorSubject.create<Unit>()
    val state: Observable<State> by lazy {
        Observable.merge(intentions, effects)
                .takeUntil(signal)
                .scan(initialState, this::reduce)
                .replay(1)
                .autoConnect(0)
                .distinctUntilChanged()
                .share()
    }

    protected abstract fun onMessage(state: State, message: Message): Pair<State, Command<Result>?>

    private fun reduce(state: State, message: Message): State {
        val (newState, command) = onMessage(state, message)
        return newState.also { command?.execute() }
    }

    private fun Command<Result>.execute() = invoke()
            .observeOn(scheduler) //  ensure processing in separate loops
            .subscribe(effects::onNext) { onCommandError(it) }

    protected open fun onCommandError(e: Throwable) {
        throw IllegalStateException("Unhandled exception in command", e)
    }

    fun effects(): Observable<Result> = effects.takeUntil(signal)

    override fun dispose() = signal.onNext(Unit)

    override fun isDisposed(): Boolean = signal.hasValue()
}

typealias Command<T> = () -> Observable<T>