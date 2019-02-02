package com.servebeer.please.smartcharger;

import com.servebeer.please.smartcharger.manager.*;
import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TeslaSolarmaxSmartCharger {

    private static final Logger log = LoggerFactory.getLogger(TeslaSolarmaxSmartCharger.class);
    //private static IChargingManager chargingManager = new DifferentialChargingManager();
    private static final DifferentialChargingManager chargingManager = new DifferentialChargingManager();

    @Value("${teslaUserEmailAddress}")
    private String teslaUserEmailAddress;

    @Value("${teslaUserPassword}")
    private String teslaUserPassword;

    private TeslaCommunicator teslaCommunicator;

    /**
     * every 5 minutes, switch on or off charging as required
     */
    @Scheduled(fixedRate = 300000)
    public void configureCharging() {
        try {
            if (teslaCommunicator == null) {
                teslaCommunicator = new TeslaCommunicator(teslaUserEmailAddress, teslaUserPassword);
            }

            SolarmaxArrayCommunicator solarmaxArrayCommunicator = new SolarmaxArrayCommunicator();

            log.info("Configuring charging...");

            // if overnight Charging doesn't apply, call autoconfigureCharging
            if (!chargingManager.overnightCharging(teslaCommunicator)) {
                chargingManager.autoconfigureCharging(teslaCommunicator, solarmaxArrayCommunicator);
            }

        } catch (ApiException e) {
            log.error(e.getMessage());
        }

    }

    public static DifferentialChargingManager getDifferentialChargingManager() {
        return chargingManager;
    }

}
