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

    private boolean wasAtSolarmaxArray = true;

    private DifferentialChargingManagerStatus status = new DifferentialChargingManagerStatus();

    @Override
    public void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException {

        DifferentialChargingManagerStatus newStatus = new DifferentialChargingManagerStatus();

        // figure out how much the SolarMax array is generating
        newStatus.setGeneratedPower(solarmaxArrayCommunicator.getCurrentlyGeneratedPower());

        logger.info("  SolarMax is generating: " + newStatus.getGeneratedPower());
        logger.info("  Power buffer: " + newStatus.getPowerBuffer());

        // figure out if the vehicle is at the charging station
        newStatus.setLocatedAtSolarmaxArray(VehicleLocator.isLocatedAtSolarmaxArray(teslaCommunicator, solarmaxArrayCommunicator));
        logger.info("  Tesla is at home: " + newStatus.isLocatedAtSolarmaxArray());

        if (newStatus.isLocatedAtSolarmaxArray()) {
            // figure out how much the Tesla is drawing
            newStatus.setConnected(teslaCommunicator.isConnected());
            newStatus.setCharging(teslaCommunicator.isCharging());
            newStatus.setTeslaPowerDraw(teslaCommunicator.getChargerPower());
            newStatus.setBatteryLevel(teslaCommunicator.getBatteryLevel());
            newStatus.setChargeLimit(teslaCommunicator.getChargeLimit());

            logger.info("  Tesla is connected: " + newStatus.isConnected());
            logger.info("  Tesla is charging: " + newStatus.isCharging());
            logger.info("  Tesla battery level: " + newStatus.getBatteryLevel());
            logger.info("  Tesla charge limit: " + newStatus.getChargeLimit());
            logger.info("  Tesla consumption: " + newStatus.getTeslaPowerDraw());

            if (newStatus.getGeneratedPower() < newStatus.getTeslaPowerDraw() + newStatus.getPowerBuffer()) {
                if (newStatus.isCharging()) {
                    logger.info("  Stopping charging...");
                    teslaCommunicator.stopCharging();
                }
            } else {
                // if it's not currently charging
                if (!newStatus.isCharging()) {
                    // if the current battery level is more than 2% unter the charge limit and it's not charging, start
                    if (newStatus.getBatteryLevel() < newStatus.getChargeLimit() + 2) {
                        if (newStatus.getBatteryLevel() < teslaCommunicator.getBatteryLevel() + 5)
                            logger.info("  Starting charging...");
                        teslaCommunicator.startCharging();
                    } else {
                        logger.info("  Allowing for rounding errors, the battery is full.");
                    }
                }
            }

        } else {
            // it's not at the Solarmax Array. If it was there, enable charging
            if (wasAtSolarmaxArray) {
                teslaCommunicator.startCharging();
                wasAtSolarmaxArray = false;
            }
        }

        status = newStatus;
    }

    public DifferentialChargingManagerStatus getStatus() {
        return status;
    }
}
