package com.zmalinowski.oneway

import com.zmalinowski.oneway.Message.Intention
import com.zmalinowski.oneway.Message.Result
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AbstractFeatureTest {

    private val intentions = PublishSubject.create<Intention>()
    private lateinit var events: TestObserver<Result>
    private lateinit var states: TestObserver<State>
    private lateinit var feature: FakeFeature
    private lateinit var command: Command<Result>
    private lateinit var result: Observable<Result>

    @Before
    fun setUp() {
        command = { result }
        feature = FakeFeature(intentions, command)
        with(feature) {
            states = state.test()
            events = effects().test()
        }
    }

    @Test
    fun uninitialised_emitsInitialState() {

        with(states) {
            assertValues(State(modified = false))
            assertNotComplete()
        }
        with(events) {
            assertNoValues()
            assertNotComplete()
        }
    }

    @Test
    fun init_emitsModifiedStateAndResult() {
        result = Observable.just(Result)

        intentions.onNext(Intention)

        with(states) {
            assertValues(State(modified = false), State(modified = true))
            assertNotComplete()
        }
        with(events) {
            assertValues(Result)
            assertNotComplete()
        }
    }

    @Test
    fun commandError_doesNotTerminate() {
        val error = RuntimeException()
        result = Observable.error(error)

        intentions.onNext(Intention)

        with(states) {
            assertValues(State(modified = false))
            assertNotComplete()
            assertNotTerminated()
        }
        with(events) {
            assertEmpty()
            assertNotComplete()
            assertNotTerminated()
        }
    }

    @Test
    fun dispose_completesStreams() {
        feature.dispose()

        assertTrue(feature.isDisposed)
        states.assertComplete()
        events.assertComplete()
    }
}

class FakeFeature(
        intentions: Observable<Intention>,
        private val command: Command<Result>
) : AbstractFeature<State, Message, Intention, Result>(State(), intentions) {

    override fun onMessage(state: State, message: Message): Pair<State, Command<Result>?> = when (message) {
        Intention -> state to command
        Result -> state.copy(modified = true) to null
    }
}

data class State(val modified: Boolean = false)

sealed class Message {
    object Intention : Message()
    object Result : Message()
}