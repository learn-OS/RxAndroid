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
package rx.resumable.operators;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

/**
 * An Operator that forwards events only when a subscriber is attached and drops all events received between subscriptions
 *
 * @param <T> Type of the subscriber
 */
public class DropEventOperator<T> implements ObserverOperator<T> {

    private final EventForwardingListener listener;
    private Observer<? super T> observer;

    public DropEventOperator(EventForwardingListener listener) {
        this.listener = listener;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        this.observer = subscriber;
        subscriber.add(new Subscription() {
            @Override
            public void unsubscribe() {
                observer = null;
            }

            @Override
            public boolean isUnsubscribed() {
                return observer == null;
            }
        });
    }

    public Observer<? super T> getObserver() {
        return observer;
    }

    public boolean hasObserver() {
        return observer != null;
    }
    @Override
    public final void onCompleted() {
        listener.allEventsForwarded();
        if (hasObserver()) {
            observer.onCompleted();
        }
    }

    @Override
    public final void onError(final Throwable throwable) {
        listener.allEventsForwarded();
        if (hasObserver()) {
            observer.onError(throwable);
        }
    }

    @Override
    public final void onNext(final T t) {
        if (hasObserver()) {
            observer.onNext(t);
        }
    }
}
