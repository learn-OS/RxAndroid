/**
 * Copyright 2014 Novoda, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.resumable.subject;

import rx.resumable.operators.ObserverOperator;

/**
 * A simple Subject that forwards events to it's ObserverOperator
 * @param <T> Type of the Observable
 */
public class ForwardingSubject<T> extends rx.subjects.Subject<T, T> {

    private final ObserverOperator<T> subscribeFunc;

    public ForwardingSubject(ObserverOperator<T> subscribeFunc) {
        super(subscribeFunc);
        this.subscribeFunc = subscribeFunc;
    }

    @Override
    public void onCompleted() {
        subscribeFunc.onCompleted();
    }

    @Override
    public void onError(Throwable throwable) {
        subscribeFunc.onError(throwable);
    }

    @Override
    public void onNext(T t) {
        subscribeFunc.onNext(t);
    }

    @Override
    public boolean hasObservers() {
        return subscribeFunc.hasObservers();
    }
}
