package com.zmalinowski.onewaydemo.v3

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.zmalinowski.onewaydemo.R
import com.zmalinowski.onewaydemo.utils.bindView
import com.zmalinowski.onewaydemo.v3.Value.Status.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val adapter = Adapter()
        disposable = FeatureHolder.viewModels.subscribe(adapter::onData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        Observable.merge(
                editText.adds(),
                adapter.events
        ).mapNotNull {
            when (it) {
                is Add -> Intention.Add(it.content)
                is Update -> Intention.Update(it.value.content, it.checked)
                is Delete -> Intention.Remove(it.value.content)
                else -> null
            }
        }.subscribe(FeatureHolder.intentions::onNext)

    }

    private fun EditText.adds() = RxTextView.editorActionEvents(this)
            .filter { it.actionId() == EditorInfo.IME_ACTION_DONE }
            .map { it -> Add(it.view().text.toString()) }
            .doOnNext { text = null }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}

private fun <T, R> Observable<T>.mapNotNull(transform: (T) -> R?): Observable<R> =
        flatMap { transform(it).let { Observable.just(it) ?: Observable.empty() } }

class Adapter : RecyclerView.Adapter<ViewHolder>() {

    private val data = mutableListOf<Value>()
    val events: PublishSubject<Any> = PublishSubject.create<Any>()

    fun onData(newData: List<Value>) {
        data.apply {
            clear()
            addAll(newData)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent).apply {
        checks.subscribe(events)
        delete.subscribe(events)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])
}

class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.item)) {

    private val checkBox by bindView<CheckBox>(R.id.text)
    private val deleteBtn by bindView<ImageView>(R.id.delete)
    private lateinit var value: Value
    val checks: Observable<Update> = RxView.clicks(checkBox).map { Update(value, checkBox.isChecked) }
    val delete: Observable<Delete> = RxView.clicks(deleteBtn).map { Delete(value) }

    fun bind(value: Value) {
        this.value = value
        checkBox.text = value.content
        when (value.status) {
            NORMAL -> true
            SAVING -> false
            DELETING -> false
        }.let { itemView.isEnabled = it }
    }
}

data class Add(val content: String)
data class Update(val value: Value, val checked: Boolean)
data class Delete(val value: Value)

private fun ViewGroup.inflate(layoutId: Int): View = LayoutInflater.from(context).inflate(layoutId, this, false)
