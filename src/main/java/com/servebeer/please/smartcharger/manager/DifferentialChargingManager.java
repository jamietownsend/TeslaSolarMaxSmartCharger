package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.smartcharger.locator.VehicleLocator;
import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

import java.util.logging.Logger;

/**
 * if the car is located near the SolarMax and the SolarMax is generating power, charge at the maximum rate
 */
public class DifferentialChargingManager implements IChargingManager {

    private Logger logger = Logger.getLogger(IChargingManager.class.getName());

    // number of Watts of power to "reserve" for other usage.
    private int powerBuffer = 2000;
    private int teslaPower = 0;

    private boolean wasAtSolarmaxArray = true;


    @Override
    public void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException {

        // figure out how much the SolarMax array is generating
        int currentlyGeneratedPower = solarmaxArrayCommunicator.getCurrentlyGeneratedPower();

        logger.info("  SolarMax is generating: " + currentlyGeneratedPower);
        logger.info("  Power buffer: " + powerBuffer);

        // figure out if the vehicle is at the charging station
        boolean locatedAtSolarmaxArray = VehicleLocator.isLocatedAtSolarmaxArray(teslaCommunicator, solarmaxArrayCommunicator);
        logger.info("  Tesla is at home: " + locatedAtSolarmaxArray);

        if (locatedAtSolarmaxArray) {
            // figure out how much the Tesla is drawing
            teslaPower = teslaCommunicator.getChargerPower();
            boolean isCharging = teslaCommunicator.isCharging();
            Integer batteryLevel = teslaCommunicator.getBatteryLevel();
            Integer chargeLimit = teslaCommunicator.getChargeLimit();

            logger.info("  Tesla battery level: " + batteryLevel);
            logger.info("  Tesla charge limit: " + chargeLimit);
            logger.info("  Tesla is charging: " + isCharging);
            logger.info("  Tesla consumption: " + teslaPower);

            if (currentlyGeneratedPower < teslaPower + powerBuffer) {
                if (isCharging) {
                    logger.info("  Stopping charging...");
                    teslaCommunicator.stopCharging();
                }
            } else {
                // if the current battery level is more than 10% unter the charge limit and it's not charging
                if (batteryLevel < chargeLimit - 10 && !isCharging) {
                    if (batteryLevel < teslaCommunicator.getBatteryLevel() + 5)
                        logger.info("  Starting charging...");
                    teslaCommunicator.startCharging();
                }
            }

        } else {
            // it's not at the Solarmax Array. If it was there, enable charging
            if (wasAtSolarmaxArray) {
                teslaCommunicator.startCharging();
                wasAtSolarmaxArray = false;
            }
        }
    }

}
