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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Observer;
import rx.android.exception.CancelledException;
import rx.android.exception.FailedException;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class ReactiveNavigatorTest {

    @Mock
    private ActivityStarter mockActivityStarter;
    @Mock
    private Intent mockIntent;
    @Mock
    private Intent mockData;
    @Mock
    private Observer<Intent> mockObserver;


    private ReactiveNavigator reactiveNavigator;

    @Before
    public void setUp() {
        initMocks(this);
        reactiveNavigator = new ReactiveNavigator(mockActivityStarter);
    }

    @Test
    public void itReturnsIntentDataIfSuccess() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        boolean result = reactiveNavigator.onActivityResult(42, Activity.RESULT_OK, mockData);

        verify(mockObserver).onNext(mockData);
        assertTrue(result);
    }

    @Test
    public void itFailsIfUserCancelled() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        boolean result = reactiveNavigator.onActivityResult(42, Activity.RESULT_CANCELED, mockData);

        verify(mockObserver).onError(any(CancelledException.class));
        assertTrue(result);
    }

    @Test
    public void itFailsIfUnkownResponseCode() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        boolean result = reactiveNavigator.onActivityResult(42, 24, mockData);

        verify(mockObserver).onError(any(FailedException.class));
        assertTrue(result);
    }

    @Test
    public void returnsFalseIfUnkownRequestCode() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        boolean result = reactiveNavigator.onActivityResult(44, 24, mockData);

        assertFalse(result);
    }
}