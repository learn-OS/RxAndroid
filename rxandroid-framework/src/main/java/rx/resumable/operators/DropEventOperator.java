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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;

/**
 * An Operator that forwards events only when a subscriber is attached and drops all events received between subscriptions
 *
 * @param <T> Type of the subscriber
 */
public class DropEventOperator<T> implements ObserverOperator<T> {

    private final List<Subscriber<? super T>> subscribers = Collections.synchronizedList(new ArrayList<Subscriber<? super T>>());

    private final EventForwardingListener listener;

    public DropEventOperator(EventForwardingListener listener) {
        this.listener = listener;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        subscriber.add(new CachedSubscription(subscriber));
        subscribers.add(subscriber);
    }

    @Override
    public void onCompleted() {
        synchronized (subscribers) {
            while (hasObservers()) {
                subscribers.remove(0).onCompleted();
            }
        }
        listener.allEventsForwarded();
    }

    @Override
    public void onError(Throwable e) {
        synchronized (subscribers) {
            while (hasObservers()) {
                subscribers.remove(0).onError(e);
            }
        }
        listener.allEventsForwarded();
    }

    @Override
    public void onNext(T newValue) {
        synchronized (subscribers) {
            for (Subscriber<? super T> subscriber : subscribers) {
                subscriber.onNext(newValue);
            }
        }
    }

    @Override
    public boolean hasObservers() {
        return !subscribers.isEmpty();
    }

    private class CachedSubscription implements Subscription {
        private final Subscriber<? super T> subscriber;

        public CachedSubscription(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void unsubscribe() {
            subscribers.remove(subscriber);
        }

        @Override
        public boolean isUnsubscribed() {
            return !subscribers.contains(subscriber);
        }
    }
}
