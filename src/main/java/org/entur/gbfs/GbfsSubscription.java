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

import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_3.gbfs_versions.GBFSGbfsVersions;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v2_3.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_3.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_3.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_3.system_calendar.GBFSSystemCalendar;
import org.entur.gbfs.v2_3.system_hours.GBFSSystemHours;
import org.entur.gbfs.v2_3.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_3.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.entur.gbfs.validation.GbfsValidator;
import org.entur.gbfs.validation.GbfsValidatorFactory;
import org.entur.gbfs.validation.model.ValidationResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class to represent a subscription to GBFS feeds for a single system
 */
public class GbfsSubscription {
    private final GbfsSubscriptionOptions subscriptionOptions;
    private final Consumer<GbfsDelivery> consumer;
    private GbfsLoader loader;

    public GbfsSubscription(GbfsSubscriptionOptions subscriptionOptions, Consumer<GbfsDelivery> consumer) {
        this.subscriptionOptions = subscriptionOptions;
        this.consumer = consumer;
    }

    /**
     * Initialize the subscription by creating a loader
     */
    public void init() {
        loader = new GbfsLoader(
                subscriptionOptions.getDiscoveryURI().toString(),
                subscriptionOptions.getHeaders(),
                subscriptionOptions.getLanguageCode(),
                subscriptionOptions.getRequestAuthenticator(), //
                subscriptionOptions.getTimeout()
        );
    }

    /**
     * Check if the subscription is ready to use
     * @return True if the subscription setup is complete
     */
    public boolean getSetupComplete() {
        return loader.getSetupComplete().get();
    }

    /**
     * Update the subscription by updating the loader and push a new delivery
     * to the consumer if the update had changes
     */
    public void update() {
        if (loader.update()) {
            GbfsDelivery delivery = new GbfsDelivery();
            delivery.setDiscovery(loader.getDiscoveryFeed());
            delivery.setVersion(loader.getFeed(GBFSGbfsVersions.class));
            delivery.setSystemInformation(loader.getFeed(GBFSSystemInformation.class));
            delivery.setVehicleTypes(loader.getFeed(GBFSVehicleTypes.class));
            delivery.setSystemRegions(loader.getFeed(GBFSSystemRegions.class));
            delivery.setStationInformation(loader.getFeed(GBFSStationInformation.class));
            delivery.setStationStatus(loader.getFeed(GBFSStationStatus.class));
            delivery.setFreeBikeStatus(loader.getFeed(GBFSFreeBikeStatus.class));
            delivery.setSystemAlerts(loader.getFeed(GBFSSystemAlerts.class));
            delivery.setSystemCalendar(loader.getFeed(GBFSSystemCalendar.class));
            delivery.setSystemHours(loader.getFeed(GBFSSystemHours.class));
            delivery.setSystemPricingPlans(loader.getFeed(GBFSSystemPricingPlans.class));
            delivery.setGeofencingZones(loader.getFeed(GBFSGeofencingZones.class));

            if (subscriptionOptions.isEnableValidation()) {
                delivery.setValidationResult(validateFeeds());
            }

            consumer.accept(delivery);
        }
    }

    private ValidationResult validateFeeds() {
        Map<String, InputStream> feeds = new HashMap<>();
        Arrays.stream(GBFSFeedName.values()).forEach(feedName -> {
            byte[] rawFeed = loader.getRawFeed(feedName);
            if (rawFeed != null) {
                feeds.put(feedName.value(), new ByteArrayInputStream(loader.getRawFeed(feedName)));
            }
        });
        GbfsValidator validator = GbfsValidatorFactory.getGbfsJsonValidator();
        return validator.validate(feeds);
    }
}
