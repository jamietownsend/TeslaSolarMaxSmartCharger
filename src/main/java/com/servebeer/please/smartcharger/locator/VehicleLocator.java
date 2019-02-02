package com.servebeer.please.smartcharger.locator;

import com.servebeer.please.smartcharger.manager.SolarmaxArrayCommunicator;
import com.servebeer.please.tesla_client.TeslaCommunicator;
import com.servebeer.please.tesla_client.generated.handler.ApiException;

public class VehicleLocator {

    /**
     * determines if the vehicle is within 100 metres of the SolarMax Array.
     *
     * @param teslaCommunicator
     * @return
     * @throws ApiException
     */
    public static boolean isLocatedAtSolarmaxArray(TeslaCommunicator teslaCommunicator) throws ApiException {

        // if it's less than 100 metres away, assume it's at the Solarmax location
        return DistanceCalculator.distance(teslaCommunicator.getGpsLocation(), SolarmaxArrayCommunicator.getGpsLocation()) < 0.1;
    }
}
