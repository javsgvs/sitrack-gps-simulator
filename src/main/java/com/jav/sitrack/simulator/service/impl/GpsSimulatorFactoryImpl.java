package com.jav.sitrack.simulator.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jav.sitrack.simulator.GpsSimulator;
import com.jav.sitrack.simulator.model.Leg;
import com.jav.sitrack.simulator.model.Point;
import com.jav.sitrack.simulator.service.GpsSimulatorFactory;
import com.jav.sitrack.simulator.service.IPathService;
import com.jav.sitrack.simulator.utils.NavUtils;

@Service
public class GpsSimulatorFactoryImpl implements GpsSimulatorFactory {

	@Autowired
	private IPathService pathService;

	@Override
	public GpsSimulator prepareGpsSimulator(GpsSimulator gpsSimulator, File kmlFile) {

		final List<Point> points;

		if (kmlFile == null) {
			points = this.pathService.getCoordinatesFromGoogle(this.pathService.loadDirectionInput().get(0));
		}
		else {
			points = this.pathService.getCoordinatesFromKmlFile(kmlFile);
		}

		return prepareGpsSimulator(gpsSimulator, points);
	}

	@Override
	public GpsSimulator prepareGpsSimulator(GpsSimulator gpsSimulator, List<Point> points) {
		gpsSimulator.setCurrentPosition(null);;
		final List<Leg>legs = createLegsList(points);
		gpsSimulator.setLegs(legs);
		gpsSimulator.setStartPosition();
		return gpsSimulator;
	}

	/**
	 * Creates list of legs in the path
	 *
	 * @param points
	 */
	private List<Leg> createLegsList(List<Point> points) {
		final List<Leg>legs = new ArrayList<Leg>();
		for (int i = 0; i < (points.size() - 1); i++) {
			Leg leg = new Leg();
			leg.setId(i);
			leg.setStartPosition(points.get(i));
			leg.setEndPosition(points.get(i + 1));
			Double length = NavUtils.getDistance(points.get(i), points.get(i + 1));
			leg.setLength(length);
			Double heading = NavUtils.getBearing(points.get(i), points.get(i + 1));
			leg.setHeading(heading);
			legs.add(leg);
		}
		return legs;
	}
}
