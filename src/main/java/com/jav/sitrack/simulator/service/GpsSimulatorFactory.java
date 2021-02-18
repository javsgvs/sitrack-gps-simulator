package com.jav.sitrack.simulator.service;

import java.io.File;
import java.util.List;

import com.jav.sitrack.simulator.GpsSimulator;
import com.jav.sitrack.simulator.model.Point;


public interface GpsSimulatorFactory {

	GpsSimulator prepareGpsSimulator(GpsSimulator gpsSimulator, File kmlFile);
	GpsSimulator prepareGpsSimulator(GpsSimulator gpsSimulator, List<Point> points);

}
