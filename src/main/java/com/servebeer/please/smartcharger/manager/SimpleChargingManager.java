package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

import java.util.logging.Logger;

/**
 * if the car is located near the SolarMax and the SolarMax is generating power, charge at the maximum rate
 */
public class SimpleChargingManager implements IChargingManager {

    Logger logger = Logger.getLogger(IChargingManager.class.getName());

    Boolean isCharging = null;

    int minimumPowerToCharge = 8500;


    @Override
    public void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException {

        // if located at SolarMax
        // if (teslaCommunicator.connectedTo(solarmaxArrayCommunicator))
        // {
        // if SolarMax is not generating
        int currentlyGeneratedPower = solarmaxArrayCommunicator.getCurrentlyGeneratedPower();
        logger.info("  SolarMax is currently generating: " + currentlyGeneratedPower);
        logger.info("  Required to charge: " + minimumPowerToCharge);
        if (currentlyGeneratedPower < minimumPowerToCharge) {
            if (isCharging == null || isCharging) {
                logger.info("  Stopping charging...");
                teslaCommunicator.stopCharging();
                isCharging = false;
            }
        } else {
            if (isCharging == null || !isCharging) {
                logger.info("Starting charging...");
                teslaCommunicator.startCharging();
                isCharging = true;
            }
        }
    }

}
