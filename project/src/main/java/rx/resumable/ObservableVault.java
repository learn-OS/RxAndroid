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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * This is a Storage Object that will retain Observables based on two keys:
 * - ResumableReference with the resumableId: identifying the object starting observables (ie: HomeActivity, SomeSpecificFragment, etc)
 * - id: identifying a specific Observable in this reference (ie: fetchData, someObservable, etc)
 *
 * This Object being the holder will usually sit at Application level.
 */
public class ObservableVault {

    private final Map<Integer, Map<Integer, Observable>> arraysOfObservables = new HashMap<Integer, Map<Integer, Observable>>();

    /**
     * Store the given Observable using given reference and id
     * @param reference Reference of the class storing this observable
     * @param id id of the Observable to store
     * @param observable the observable to store
     * @return previous observable if one was already stored with this id and reference
     */
    public synchronized Observable put(ResumableReference reference, int id, Observable observable) {
        return getObservablesFor(reference).put(id, observable);
    }

    /**
     * Remove the given Observable using given reference and id
     * @param reference Reference of the class storing this observable
     * @param id id of the Observable to remove
     * @return Observable that was removed from the Vault
     */
    public synchronized Observable remove(ResumableReference reference, int id) {
        return getObservablesFor(reference).remove(id);
    }

    /**
     * Get all the Observables stored for a given reference
     * @param reference Reference of the class storing the observables
     * @return A map of Key/Observables stored for the given reference
     */
    public synchronized Map<Integer, Observable> getImmutableObservablesFor(ResumableReference reference) {
        return Collections.unmodifiableMap(new HashMap<Integer, Observable>(getObservablesFor(reference)));
    }

    private synchronized Map<Integer, Observable> getObservablesFor(ResumableReference reference) {
        int referenceId = reference.getResumableId();
        if (arraysOfObservables.containsKey(referenceId)) {
            return arraysOfObservables.get(referenceId);
        }
        Map<Integer, Observable> observableMap = new HashMap<Integer, Observable>();
        arraysOfObservables.put(referenceId, observableMap);
        return observableMap;
    }
}
