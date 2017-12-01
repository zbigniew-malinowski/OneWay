package com.zmalinowski.onewaydemo.v1

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object FeatureHolder {

    val intentions: PublishSubject<Message.Intention> = PublishSubject.create<Message.Intention>()
    val viewModels: Observable<List<Value>> = DemoFeature(intentions, Executor).state.map { it.data }
}