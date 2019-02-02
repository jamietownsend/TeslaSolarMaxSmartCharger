package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

import org.slf4j.LoggerFactory;

/**
 * if the car is located near the SolarMax and the SolarMax is generating power,
 * charge at the maximum rate
 */
public class SimpleChargingManager implements IChargingManager {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleChargingManager.class);

    Boolean isCharging = null;

    int minimumPowerToCharge = 8500;

    @Override
    public void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException {

        // if located at SolarMax
        // if (teslaCommunicator.connectedTo(solarmaxArrayCommunicator))
        // {
        // if SolarMax is not generating
        int currentlyGeneratedPower = solarmaxArrayCommunicator.getCurrentlyGeneratedPower();
        log.info("  SolarMax is currently generating: " + currentlyGeneratedPower);
        log.info("  Required to charge: " + minimumPowerToCharge);
        if (currentlyGeneratedPower < minimumPowerToCharge) {
            if (isCharging == null || isCharging) {
                log.info("  Stopping charging...");
                teslaCommunicator.stopCharging();
                isCharging = false;
            }
        } else {
            if (isCharging == null || !isCharging) {
                log.info("Starting charging...");
                teslaCommunicator.startCharging();
                isCharging = true;
            }
        }
    }

}
