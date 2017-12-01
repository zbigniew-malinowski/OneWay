package com.zmalinowski.onewaydemo.v2

import com.zmalinowski.onewaydemo.v2.Action.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

object Executor {

    private val list = mutableListOf<Value>()

    fun get(message: Intention.Get): Observable<Action> = Observable.just(Loaded(list))
            .fakeDelay()
            .randomError()
            .onErrorReturn { Failed(message) }
            .startWith(Loading)

    fun add(message: Intention.Add): Observable<Action> = Observable.just(Added(message.content))
            .fakeDelay()
            .randomError()
            .doOnNext { list.add(Value(message.content)) }
            .onErrorReturn { Failed(message) }
            .startWith(Adding(message.content))

    fun remove(message: Intention.Remove): Observable<Action> = Observable.just(Removed(message.content))
            .fakeDelay()
            .randomError()
            .doOnNext { list.removeAll { it.content == message.content } }
            .onErrorReturn { Failed(message) }
            .startWith(Removing(message.content))

    fun update(message: Intention.Update): Observable<Action> = Observable.empty<Action>()
            .fakeDelay()
            .randomError()
            .doOnNext { list.update(message.content, message.checked) }
            .onErrorReturn { Failed(message) }
            .startWith(Update(message.content, message.checked))
}


private fun Observable<out Action>.randomError(): Observable<Action> {
    return map {
        if (random(p = 0.2F)) throw RuntimeException()
        else it
    }
}

private fun random(p: Float): Boolean = Random().nextFloat() <= p

private fun <T> Observable<T>.fakeDelay(): Observable<T> = delay(1000L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
