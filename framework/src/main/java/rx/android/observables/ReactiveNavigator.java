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
package rx.android.observables;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.exception.CancelledException;
import rx.android.exception.FailedException;

/**
 * Handles the navigation to an Activity for result using an Observable
 * You need to hook the Activity onActivityResult call to this object to make it work.
 */
public class ReactiveNavigator {

    private final ActivityStarter activityStarter;

    private static final SparseArray<Observer<? super Intent>> activityResultObservers = new SparseArray<Observer<? super Intent>>();

    public ReactiveNavigator(ActivityStarter activityStarter) {
        this.activityStarter = activityStarter;
    }

    /**
     * Start an activity and observe the result using an Observable
     * @param intent The request intent for which you intend to start the activity
     * @param requestCode The request code to identify your request
     * @return An observable of the Intent data response
     */
    public Observable<Intent> toActivityForResult(final Intent intent, final int requestCode) {
        return Observable.create(new Observable.OnSubscribe<Intent>() {
            @Override
            public void call(Subscriber<? super Intent> subscriber) {
                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        activityResultObservers.remove(requestCode);
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return activityResultObservers.get(requestCode) == null;
                    }
                });
                activityResultObservers.put(requestCode, subscriber);
                activityStarter.startActivityForResult(intent, requestCode);
            }
        });
    }

    /**
     * /**
     * The hook to forward responses from onActivityResult.
     * This call will retrieve the relevant subscriber and pass the data to it if successful or fail accordingly.
     * @param requestCode The request code from the response
     * @param resultCode The response code from the action
     * @param data The data returned by the action
     * @return True if the result has been handled false otherwise
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Observer<? super Intent> observer = activityResultObservers.get(requestCode);
        if (observer == null) {
            return false;
        }
        switch (resultCode) {
            case Activity.RESULT_OK:
                observer.onNext(data);
                observer.onCompleted();
                activityResultObservers.remove(requestCode);
                return true;
            case Activity.RESULT_CANCELED:
                observer.onError(new CancelledException());
                activityResultObservers.remove(requestCode);
                return true;
            default:
                observer.onError(new FailedException());
                return true;
        }
    }

}
