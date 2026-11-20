package com.tidewater.freight;

import java.util.EnumMap;
import java.util.Map;

public class InMemoryZoneRateRepository implements ZoneRateRepository {

    private final Map<Zone, ZoneRateRule> rules = new EnumMap<>(Zone.class);

    public InMemoryZoneRateRepository(Map<Zone, ZoneRateRule> rules) {
        this.rules.putAll(rules);
    }

    @Override
    public ZoneRateRule findRule(Zone zone) {
        ZoneRateRule rule = rules.get(zone);
        if (rule == null) {
            throw new UnknownZoneException(zone);
        }
        return rule;
    }
}