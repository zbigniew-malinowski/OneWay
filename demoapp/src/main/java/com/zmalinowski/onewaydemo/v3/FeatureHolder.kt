package com.zmalinowski.onewaydemo.v3

import com.zmalinowski.oneway.Feature3
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object FeatureHolder {

    val intentions: PublishSubject<Intention> = PublishSubject.create<Intention>()

    val viewModels: Observable<List<Value>> = Feature3(intentions, Actor, State()).state.map { it.data }
}