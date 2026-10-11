package com.eprtracker.service;

import com.eprtracker.dto.request.EcSimulationRequest;
import com.eprtracker.dto.response.EcSimulationResponse;

public interface EcSimulatorService {
    EcSimulationResponse simulateLiability(String companyId, EcSimulationRequest request);
}
