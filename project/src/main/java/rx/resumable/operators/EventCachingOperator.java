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
import java.util.Iterator;
import java.util.List;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

/**
 * An Operator that forwards events when a subscriber is attached and caches all events received between subscriptions
 * Upon subscription the cached events will be forwarded to the new subscriber.
 *
 * @param <T> Type of the subscriber
 */
public class EventCachingOperator<T> implements ObserverOperator<T> {

    private final List<Event> cachedEvents = new ArrayList<Event>();
    private final EventForwardingListener listener;

    private Observer<? super T> observer;

    public EventCachingOperator(EventForwardingListener listener) {
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
        sendCachedEvents();
    }

    public Observer<? super T> getObserver() {
        return observer;
    }

    public boolean hasObserver() {
        return observer != null;
    }

    private void sendCachedEvents() {
        Iterator<Event> iterator = cachedEvents.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            iterator.remove();
            event.send();
        }
    }

    @Override
    public final void onCompleted() {
        if (hasObserver()) {
            listener.allEventsForwarded();
            observer.onCompleted();
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    listener.allEventsForwarded();
                    observer.onCompleted();
                }
            });
        }
    }

    @Override
    public final void onError(final Throwable throwable) {
        if (hasObserver()) {
            listener.allEventsForwarded();
            observer.onError(throwable);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    listener.allEventsForwarded();
                    observer.onError(throwable);
                }
            });
        }
    }

    @Override
    public final void onNext(final T t) {
        if (hasObserver()) {
            observer.onNext(t);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    observer.onNext(t);
                }
            });
        }
    }

    private interface Event {
        void send();
    }
}
