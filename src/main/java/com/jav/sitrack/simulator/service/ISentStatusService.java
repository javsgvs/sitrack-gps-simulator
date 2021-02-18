package com.jav.sitrack.simulator.service;

import java.util.Collection;

import com.jav.sitrack.simulator.model.GpsSimulatorInstance;

public interface ISentStatusService {
    
    void sentStatus(Collection<GpsSimulatorInstance> instanceCollection);
    
}
