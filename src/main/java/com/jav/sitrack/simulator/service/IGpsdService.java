package com.jav.sitrack.simulator.service;

import com.jav.sitrack.simulator.model.PositionInfo;

public interface IGpsdService {

	/**
	 * Sends NMEA RMC report to linux gps daemon, gpsd via predetermined pipe.
	 *
	 */
	void updatePosition(PositionInfo position);

}