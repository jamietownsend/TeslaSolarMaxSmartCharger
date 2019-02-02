package com.servebeer.please.smartcharger.manager;

import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

public interface IChargingManager {

    /**
     * Returns true if the vehicle should enable charging
     * @param teslaCommunicator
     * @param solarmaxArrayCommunicator
     * @throws com.servebeer.please.tesla_client.generated.handler.ApiException
     */
    void autoconfigureCharging(TeslaCommunicator teslaCommunicator, SolarmaxArrayCommunicator solarmaxArrayCommunicator) throws ApiException;
}
