package com.jav.sitrack.simulator.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.LookAt;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.RefreshMode;
import com.jav.sitrack.simulator.model.Point;
import com.jav.sitrack.simulator.model.PositionInfo;
import com.jav.sitrack.simulator.service.IKmlService;

@Service
public class KmlServiceImpl implements IKmlService {

	public static final String KML_POSITION_FILE_NAME = "pos";
	public static final String KML_POSITION_FILE_SUFFIX = ".xml";

	@Autowired
	private Marshaller marshaller;

	@Override
	public final void setupKmlIntegration(Set<Long> intanceIds, Point lookAtPoint) {
		Assert.notEmpty(intanceIds);
		Assert.isTrue(intanceIds.size() >= 1);

		File f = new File("gps.kml");

		Kml kml = KmlFactory.createKml();
		Folder folder = KmlFactory.createFolder();
		folder.setOpen(true);
		folder.setName("Contains GPS Coordinates");
		kml.setFeature(folder);

		final LookAt lookAt = KmlFactory.createLookAt();
		lookAt.setLatitude(lookAtPoint.getLatitude());
		lookAt.setLongitude(lookAtPoint.getLongitude());
		lookAt.setAltitude(8000);
		lookAt.setAltitudeMode(AltitudeMode.ABSOLUTE);;
		folder.setAbstractView(lookAt);

		for (long instanceId : intanceIds) {
			Link link = KmlFactory.createLink();
			link.setHref(KML_POSITION_FILE_NAME + instanceId + KML_POSITION_FILE_SUFFIX);
			link.setRefreshMode(RefreshMode.ON_INTERVAL);
			link.setRefreshInterval(1.0);

			NetworkLink networkLink = KmlFactory.createNetworkLink();
			networkLink.setName("GPS link " + instanceId);
			networkLink.setOpen(true);
			networkLink.setLink(link);
			folder.addToFeature(networkLink);
		}

		final OutputStream out;

		try {
			out = new FileOutputStream(f);
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}

		try {
			marshaller.marshal(kml, new StreamResult(out));
		} catch (XmlMappingException | IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public final void setupKmlIntegration(Set<Long> intanceIds) {
		Point point = new Point(19.715370574287654, -155.94979094376782);
		setupKmlIntegration(intanceIds, point);
	}

	@Override
	public void updatePosition(Long instanceId, PositionInfo position) {
		de.micromata.opengis.kml.v_2_2_0.Point point = KmlFactory.createPoint();
		Integer speedKph = 0;

		if(position != null) {
			Coordinate coordinate = KmlFactory.createCoordinate(position.getPosition().getLongitude(), position.getPosition().getLatitude());
			point.getCoordinates().add(coordinate);
		}
		else {
			Coordinate coordinate = KmlFactory.createCoordinate(0,0);
			point.getCoordinates().add(coordinate);
		}

		Placemark placemark = KmlFactory.createPlacemark();

		if(position != null) {
			Double speed = position.getSpeed();
			speedKph = (int)(speed * 3600 / 1000);
		}
		else {
			speedKph = 0;
		}

		placemark.setName(speedKph.toString() + " kph");
		placemark.setGeometry(point);

		final Kml kml = KmlFactory.createKml();
		kml.setFeature(placemark);

		OutputStream out;
		try {
			out = new FileOutputStream(KML_POSITION_FILE_NAME + instanceId + KML_POSITION_FILE_SUFFIX);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
		try {
			marshaller.marshal(kml, new StreamResult(out));
		} catch (XmlMappingException | IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
