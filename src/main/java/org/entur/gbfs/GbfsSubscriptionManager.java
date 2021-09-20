/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.gbfs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GbfsSubscriptionManager {
    private final Map<String, GbfsSubscription> subscriptions = new HashMap<>();

    /**
     * Start a subscription on a GBFS feed delivery
     *
     * @param options Options
     * @param consumer A consumer that will handle receiving updates from the loader
     * @return A string identifier
     */
    public String subscribe(GbfsSubscriptionOptions options, Consumer<GbfsDelivery> consumer) {
        String id = UUID.randomUUID().toString();
        var subscription = new GbfsSubscription(options, consumer);
        subscriptions.put(id, subscription);
        subscription.init();
        return id;
    }

    /**
     * Update all subscriptions
     */
    public void update() {
        subscriptions.values().forEach(GbfsSubscription::update);
    }

    /**
     * Stop a subscription on a GBFS feed delivery
     *
     * @param identifier An identifier returned by subscribe method.
     */
    public void unsubscribe(String identifier) {
        subscriptions.remove(identifier);
    }
}