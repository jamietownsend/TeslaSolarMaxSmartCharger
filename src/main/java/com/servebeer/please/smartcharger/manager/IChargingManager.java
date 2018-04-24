package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

public interface IChargingManager {

    /**
     * Returns true if the vehicle should enable charging
     */
    void autoconfigureCharging(TeslaCommunicator tesla, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException;
}
