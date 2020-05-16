package go.universaladloader.extension

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposeTo(composite: CompositeDisposable) {
    composite.add(this)
}
