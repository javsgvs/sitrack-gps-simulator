package com.jav.sitrack.simulator.service;

import java.io.File;
import java.util.List;

import com.jav.sitrack.simulator.model.DirectionInput;
import com.jav.sitrack.simulator.model.Point;


public interface IPathService {

	/**
	 *
	 * @return
	 */
	List<DirectionInput> loadDirectionInput();

	/**
	 *
	 * @param directionInput
	 * @return
	 */
	List<Point> getCoordinatesFromGoogle(DirectionInput directionInput);

	/**
	 * Returns list of points contained in the path kml file.
	 * @param kmlFile path kml file
	 * @return
	 */
	List<Point> getCoordinatesFromKmlFile(File kmlFile);
}