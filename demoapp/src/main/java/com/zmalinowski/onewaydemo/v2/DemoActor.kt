package com.zmalinowski.onewaydemo.v2

import com.zmalinowski.oneway.Actor
import io.reactivex.Observable

class DemoActor(private val executor: Executor, state: State = State()) : Actor<Intention, State, Action>(state) {
    override fun invoke(intent: Intention): Observable<Action> = when (intent) {
        is Intention.Get -> executor.get(intent)
        is Intention.Add -> executor.add(intent)
        is Intention.Update -> executor.update(intent)
        is Intention.Remove -> executor.remove(intent)
    }
}