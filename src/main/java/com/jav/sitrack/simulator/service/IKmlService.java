package com.jav.sitrack.simulator.service;

import java.util.Set;

import com.jav.sitrack.simulator.model.Point;
import com.jav.sitrack.simulator.model.PositionInfo;

public interface IKmlService {

	/**
	 * Creates a kml object containing a network link and writes it a file,
	 * This file is read by google earth. The file points to another file 'pos.kml' which has
	 * the latest position and is read periodically by google earth.
	 */
	void setupKmlIntegration(Set<Long> intanceIds);
	void setupKmlIntegration(Set<Long> intanceIds, Point lookAtPoint);

	void updatePosition(Long id, PositionInfo position);

}