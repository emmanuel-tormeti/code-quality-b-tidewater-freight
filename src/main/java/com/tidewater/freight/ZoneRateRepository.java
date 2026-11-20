package com.tidewater.freight;

public interface ZoneRateRepository {
    ZoneRateRule findRule(Zone zone) throws UnknownZoneException;
}