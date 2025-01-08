import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Created by Greg Harley on 5/30/2017.
 * Simple event bus using rxJava
 */

object EventBus {
    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun <T : Any> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}