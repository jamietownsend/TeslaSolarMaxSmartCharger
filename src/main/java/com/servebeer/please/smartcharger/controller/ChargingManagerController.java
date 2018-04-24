package com.servebeer.please.smartcharger.controller;

import com.servebeer.please.smartcharger.TeslaSolarmaxSmartCharger;
import com.servebeer.please.smartcharger.manager.DifferentialChargingManagerStatus;
import com.servebeer.please.solarmax.connector.exception.SolarmaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rest Controller to talk to a SolarMax MT Power Inverter
 *
 * @author Jamie Townsend
 * @since 1.0, 2016.
 */
@RestController
@RequestMapping(value = "/chargingManager")
class ChargingManagerController {

    private static final Logger logger = LoggerFactory.getLogger(ChargingManagerController.class);

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping(final HttpServletRequest request, final HttpServletResponse response) {

        return "pong";
    }

    /**
     * gets the power generation status from the SolarMax device addressable under host:port
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json")
    public DifferentialChargingManagerStatus getStatus(final HttpServletRequest request, final HttpServletResponse response) throws SolarmaxException {

        return TeslaSolarmaxSmartCharger.getDifferentialChargingManager().getStatus();

    }
}
