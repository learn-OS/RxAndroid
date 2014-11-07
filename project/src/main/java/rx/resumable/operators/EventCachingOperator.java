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

import rx.Subscriber;

/**
 * An Operator that forwards events when a subscriber is attached and caches all events received between subscriptions
 * Upon subscription the cached events will be forwarded to the new subscriber.
 *
 * @param <T> Type of the subscriber
 */
public class EventCachingOperator<T> extends DropEventOperator<T> {

    private final List<Event> cachedEvents = new ArrayList<Event>();

    public EventCachingOperator(EventForwardingListener listener) {
        super(listener);
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        super.call(subscriber);
        sendCachedEvents();
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
        if (hasObservers()) {
            super.onCompleted();
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    onCompleted();
                }
            });
        }
    }

    @Override
    public final void onError(final Throwable throwable) {
        if (hasObservers()) {
            super.onError(throwable);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    onError(throwable);
                }
            });
        }
    }

    @Override
    public final void onNext(final T t) {
        if (hasObservers()) {
            super.onNext(t);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    onNext(t);
                }
            });
        }
    }

    private interface Event {
        void send();
    }
}
