package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.smartcharger.locator.VehicleLocator;
import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;
import java.time.LocalTime;

import org.slf4j.LoggerFactory;

/**
 * if the car is located near the SolarMax and the SolarMax is generating power,
 * charge at the maximum rate
 */
public class DifferentialChargingManager implements IChargingManager {

    // BKW night tarif is from 21 - 7 Uhr. Set the values to allow for clock variances.
    private static final LocalTime EndNightTarif = LocalTime.of(6, 55);
    private static final LocalTime StartNightTarif = LocalTime.of(21, 05);
    private static final int MinimumOvernightChargePercent = 50;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DifferentialChargingManager.class);

    private boolean wasAtSolarmaxArray = true;

    private DifferentialChargingManagerStatus status = new DifferentialChargingManagerStatus();

    @Override
    public void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException {

        DifferentialChargingManagerStatus newStatus = new DifferentialChargingManagerStatus();

        // figure out how much the SolarMax array is generating
        newStatus.setGeneratedPower(solarmaxArrayCommunicator.getCurrentlyGeneratedPower());

        log.info("  SolarMax is generating: {0}", newStatus.getGeneratedPower());
        log.info("  Power buffer: {0}", newStatus.getPowerBuffer());

        // figure out if the vehicle is at the charging station
        newStatus.setLocatedAtSolarmaxArray(VehicleLocator.isLocatedAtSolarmaxArray(teslaCommunicator));
        log.info("  Tesla is at home: {0}", newStatus.isLocatedAtSolarmaxArray());

        if (newStatus.isLocatedAtSolarmaxArray()) {
            updateTeslaStatus(newStatus, teslaCommunicator);

            if (newStatus.getGeneratedPower() < newStatus.getTeslaPowerDraw() + newStatus.getPowerBuffer()) {
                if (newStatus.isCharging()) {
                    log.info("  Stopping charging...");
                    teslaCommunicator.stopCharging();
                }
            } else {
                // if it's not currently charging
                if (!newStatus.isCharging()) {
                    // if the current battery level is more than 2% unter the charge limit and it's not charging, start
                    if (newStatus.getBatteryLevel() < newStatus.getChargeLimit() + 2) {
                        if (newStatus.getBatteryLevel() < teslaCommunicator.getBatteryLevel() + 5) {
                            log.info("  Starting charging...");
                        }
                        teslaCommunicator.startCharging();
                    } else {
                        log.info("  Allowing for rounding errors, the battery is full.");
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

    public boolean overnightCharging(TeslaCommunicator teslaCommunicator) throws ApiException {

        LocalTime now = LocalTime.now();

        DifferentialChargingManagerStatus newStatus = new DifferentialChargingManagerStatus();

        // figure out if the vehicle is at the charging station
        Boolean scheduledChargePending = teslaCommunicator.getScheduledChargingPending();

        boolean overnightChargingRulesApply = !scheduledChargePending && (now.isAfter(StartNightTarif) || now.isBefore(EndNightTarif)) && VehicleLocator.isLocatedAtSolarmaxArray(teslaCommunicator);

        log.info("  Current Time: {0}", now.toString());
        log.info("  now.isAfter(StartNightTarif) : {0}", now.isAfter(StartNightTarif));
        log.info("  now.isBefore(EndNightTarif): {0}", now.isBefore(EndNightTarif));
        log.info("  Tesla is at home: {0}", VehicleLocator.isLocatedAtSolarmaxArray(teslaCommunicator));
        log.info("  scheduledChargePending: {0}", scheduledChargePending);
        log.info("  overnightChargingRulesApply: {0}", overnightChargingRulesApply);

        if (!overnightChargingRulesApply) {
            return false;
        }

        updateTeslaStatus(newStatus, teslaCommunicator);

        log.info("  isCharging(): {0}", newStatus.isCharging());
        log.info("  MinimumOvernightChargePercent: {0}", MinimumOvernightChargePercent);

        // if it's not currently charging
        if (!newStatus.isCharging()) {
            // if the current battery level is less than the minimumOvernightChargingPercent, start charging
            if (newStatus.getBatteryLevel() < MinimumOvernightChargePercent) {
                log.info("  Starting charging...");
                teslaCommunicator.startCharging();
            }
        } else {
            // it is currently charging, so...
            // if the battery level is at least the minimumOvernightChargingPercent, stop charging
            if (newStatus.getBatteryLevel() >= MinimumOvernightChargePercent) {
                log.info("  Stopping charging...");
                teslaCommunicator.stopCharging();
            }
        }

        return true;
    }

    public DifferentialChargingManagerStatus getStatus() {
        return status;
    }

    private void updateTeslaStatus(DifferentialChargingManagerStatus newStatus, TeslaCommunicator teslaCommunicator) throws ApiException {
        // figure out how much the Tesla is drawing
        newStatus.setConnected(teslaCommunicator.isConnected());
        newStatus.setCharging(teslaCommunicator.isCharging());
        newStatus.setTeslaPowerDraw(teslaCommunicator.getChargerPower());
        newStatus.setBatteryLevel(teslaCommunicator.getBatteryLevel());
        newStatus.setChargeLimit(teslaCommunicator.getChargeLimit());

        log.info("  Tesla is connected: {0}", newStatus.isConnected());
        log.info("  Tesla is charging: {0}", newStatus.isCharging());
        log.info("  Tesla battery level: {0}", newStatus.getBatteryLevel());
        log.info("  Tesla charge limit: {0}", newStatus.getChargeLimit());
        log.info("  Tesla consumption: {0}", newStatus.getTeslaPowerDraw());

    }
}
