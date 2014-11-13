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
package rx.resumable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.resumable.observer.ResumableObserver;
import rx.resumable.operators.DropEventOperator;
import rx.resumable.operators.EventCachingOperator;
import rx.resumable.operators.EventForwardingListener;
import rx.resumable.subject.ForwardingSubject;

/**
 * (Might need a new name since it is confusing now that RxJava uses Subscriber for the wrapper around Observer)
 *
 * This Object is performing the subscription to Observables as a proxy
 * The aim is to handle storage of Observables and recreate and reattach Observers upon reference lifecycle changes
 *
 * On Android this object would sit at Activity/Fragment level
 */
public class ResumableSubscriber {

    private final List<SubscriptionWithId> subscriptions = new ArrayList<SubscriptionWithId>();
    private final ResumableReference resumableReference;
    private final ObservableVault observableVault;
    private final ObserverFactory factory;


    public ResumableSubscriber(ResumableReference resumableReference, ObserverFactory factory, ObservableVault observableVault) {
        this.resumableReference = resumableReference;
        this.observableVault = observableVault;
        this.factory = factory;
    }

    /**
     * Recreate and reattach Observers for the current resumableReference
     * On Android would be call in onResume
     */
    public void resume() {
        Map<Integer, Observable> observableMap = observableVault.getImmutableObservablesFor(resumableReference);
        for (Map.Entry<Integer, Observable> observableEntry : observableMap.entrySet()) {
            int observerId = observableEntry.getKey();
            Observable observable = observableEntry.getValue();
            final Subscription subscription = observable.subscribe(factory.createObserver(observerId));
            subscriptions.add(new SubscriptionWithId(subscription, observerId));
        }
    }

    /**
     * Unsubscribe all current Observers to avoid leaking the current reference
     * On Android would be called in onPause
     */
    public void pause() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }

    /**
     * Subscribes the given observer to the observable and cache events emitted by the observables between calls to pause and resume
     * @param observable The observable to subscribe to
     * @param observer The Observer we want to subscribe
     * @param <T> The type of the Observable
     */
    public <T> void subscribe(Observable<? extends T> observable, ResumableObserver<T> observer) {
        ForwardingSubject<T> proxy = new ForwardingSubject<T>(new EventCachingOperator<T>(new CleanObservable(observer.getId())));
        observable.subscribe(proxy);
        subscribe(observer, proxy);
    }

    /**
     * Subscribes the given observer to the observable and replay all events upon resume
     *
     * @param observable The observable to subscribe to
     * @param observer   The Observer we want to subscribe
     * @param <T>        The type of the Observable
     */
    public <T> void subscribeWithReplay(Observable<? extends T> observable, ResumableObserver<T> observer) {
        Observable<? extends T> replayEnabled = observable.cache();
        subscribe(observer, replayEnabled);
    }

    /**
     * Subscribes the given observer to the observable and drop events emitted by the observables between calls to pause and resume
     *
     * @param observable The observable to subscribe to
     * @param observer   The Observer we want to subscribe
     * @param <T>        The type of the Observable
     */
    public <T> void subscribeWithDrop(Observable<? extends T> observable, ResumableObserver<T> observer) {
        ForwardingSubject<T> proxy = new ForwardingSubject<T>(new DropEventOperator<T>(new CleanObservable(observer.getId())));
        observable.subscribe(proxy);
        subscribe(observer, proxy);
    }

    private <T> void subscribe(final ResumableObserver<T> observer, final Observable<? extends T> observable) {
        observableVault.put(resumableReference, observer.getId(), observable);
        final SubscriptionWithId subscription = new SubscriptionWithId(observable.subscribe(observer), observer.getId());
        subscriptions.add(subscription);
    }

    /**
     * Unsubscribe the given observer
     * @param observerId id of the observer to unsubscribe.
     */
    public void unsubscribe(final int observerId) {
        for (Iterator<SubscriptionWithId> iterator = subscriptions.iterator(); iterator.hasNext(); ) {
            SubscriptionWithId subscription = iterator.next();
            if (subscription.getId() == observerId) {
                subscription.unsubscribe();
                iterator.remove();
            }
        }
        observableVault.remove(resumableReference, observerId);
    }

    private class CleanObservable implements EventForwardingListener {

        private final int id;

        public CleanObservable(int id) {
            this.id = id;
        }

        @Override
        public void allEventsForwarded() {
            observableVault.remove(resumableReference, id);
        }
    }

    private static final class SubscriptionWithId implements Subscription {
        private final Subscription subscription;
        private final int id;

        private SubscriptionWithId(final Subscription subscription, final int id) {
            this.subscription = subscription;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public void unsubscribe() {
            subscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return subscription.isUnsubscribed();
        }
    }

}
