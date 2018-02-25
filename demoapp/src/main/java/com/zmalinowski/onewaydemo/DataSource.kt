package com.zmalinowski.onewaydemo

import com.zmalinowski.onewaydemo.v3.Intention
import com.zmalinowski.onewaydemo.v3.Value
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.cast
import java.util.*
import java.util.concurrent.TimeUnit

class DataSource {
    private val list = mutableListOf<Value>()
    fun get(): Observable<List<Value>> =
            Observable.just(list)
                    .fakeDelay().cast()

    fun add(value: Value): Observable<Unit> =
            list.add(value)
                    .let { Observable.just(Unit) }
                    .fakeDelay().cast()

    fun remove(content: String): Observable<Unit> =
            list.removeAll { it.content == content }
                    .let { Observable.just(Unit) }

    fun update(message: Intention.Update): Observable<Unit> =
            list.indexOfFirst { it.content == message.content }
                    .let { index -> list.removeAt(index).copy(checked = message.checked).let { list.add(index, it) } }
                    .let { Observable.just(Unit) }


}

private fun <T> Observable<out T>.randomError(): Observable<T> = map {
    if (random(p = 0.2F)) throw RuntimeException()
    else it
}

private fun random(p: Float): Boolean = Random().nextFloat() <= p

private fun <T> Observable<T>.fakeDelay(): Observable<T> = delay(1000L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
