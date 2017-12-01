package com.zmalinowski.onewaydemo.v1

import com.zmalinowski.onewaydemo.v1.Message.Result.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

object Executor {

    private val list = mutableListOf<Value>()

    fun get(message: Message.Intention.Get): Observable<Message.Result> = Observable.just(Loaded(list))
            .fakeDelay()
            .randomError()
            .onErrorReturn { Failed(message) }

    fun add(message: Message.Intention.Add): Observable<Message.Result> = Observable.just(Added(message.content))
            .fakeDelay()
            .randomError()
            .doOnNext { list.add(Value(message.content)) }
            .onErrorReturn { Failed(message) }

    fun remove(message: Message.Intention.Remove): Observable<Message.Result> = Observable.just(Removed(message.content))
            .fakeDelay()
            .randomError()
            .doOnNext { list.removeAll { it.content == message.content } }
            .onErrorReturn { Failed(message) }

    fun update(message: Message.Intention.Update): Observable<Message.Result> = Observable.just(Updated(message.content, message.checked))
            .fakeDelay()
            .randomError()
            .doOnNext { list.update(message.content, message.checked) }
            .onErrorReturn { Failed(message) }
}


private fun Observable<out Message.Result>.randomError(): Observable<Message.Result> {
    return map {
        if (random(p = 0.2F)) throw RuntimeException()
        else it
    }
}

private fun random(p: Float): Boolean = Random().nextFloat() <= p

private fun <T> Observable<T>.fakeDelay(): Observable<T> = delay(1000L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
