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

import org.junit.Test;

import rx.resumable.operators.ObserverOperator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ForwardingSubjectTest {

    @Test
    public void itForwardsEventsToTheOperator() {
        ObserverOperator<String>  mockOperator = mock(ObserverOperator.class);
        ForwardingSubject<String> forwardingSubject = new ForwardingSubject<String>(mockOperator);

        forwardingSubject.onNext("this");
        forwardingSubject.onCompleted();

        verify(mockOperator).onNext("this");
        verify(mockOperator).onCompleted();
    }

    @Test
    public void itForwardsErrorsToTheOperator() {
        ObserverOperator<String> mockOperator = mock(ObserverOperator.class);
        ForwardingSubject<String> forwardingSubject = new ForwardingSubject<String>(mockOperator);

        Throwable throwable = new Throwable();
        forwardingSubject.onError(throwable);

        verify(mockOperator).onError(throwable);
    }
}
