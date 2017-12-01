package com.zmalinowski.onewaydemo.v2

import com.zmalinowski.oneway.Feature
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object FeatureHolder {

    val intentions: PublishSubject<Intention> = PublishSubject.create<Intention>()

    val viewModels: Observable<List<Value>> = Feature<Intention, Action, State>(intentions, DemoReducer(), DemoActor(Executor))
            .state.map { it.data }
}