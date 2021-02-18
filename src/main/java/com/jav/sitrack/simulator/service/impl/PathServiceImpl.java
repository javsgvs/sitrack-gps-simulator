package com.jav.sitrack.simulator.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import com.jav.sitrack.simulator.model.DirectionInput;
import com.jav.sitrack.simulator.model.Point;
import com.jav.sitrack.simulator.service.IPathService;


@Service
public class PathServiceImpl implements IPathService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Environment environment;

	@Autowired
	private Unmarshaller unmarshaller;

	public PathServiceImpl() {
		super();
	}

	@Override
	public List<DirectionInput> loadDirectionInput() {
		final InputStream is = this.getClass().getResourceAsStream("/directions.json");

		try {
			return objectMapper.readValue(is, new TypeReference<List<DirectionInput>>() {
				//Just make Jackson happy
			});
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<Point> getCoordinatesFromGoogle(DirectionInput directionInput) {

		final GeoApiContext context = new GeoApiContext().setApiKey(environment.getRequiredProperty("gpsSimmulator.googleApiKey"));
		final DirectionsApiRequest request =  DirectionsApi.getDirections(
			context,
			directionInput.getFrom(),
			directionInput.getTo());
		List<LatLng> latlongList = null;

		try {
			DirectionsRoute[] routes = request.await();

			for (DirectionsRoute route : routes) {
				latlongList = route.overviewPolyline.decodePath();
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}

		final List<Point> points = new ArrayList<>(latlongList.size());

		for (LatLng latLng : latlongList) {
			points.add(new Point(latLng.lat, latLng.lng));
		}

		return points;
	}

	@Override
	public final List<Point> getCoordinatesFromKmlFile(File kmlFile) {

		final Kml kml;
		try {
			kml = (Kml) unmarshaller.unmarshal(new StreamSource(kmlFile));
		}
		catch (XmlMappingException | IOException e) {
			throw new IllegalStateException(e);
		}

		final Document doc = (Document) kml.getFeature();
		List<Feature> features = doc.getFeature();
		List<Point> pointsToReturn = new ArrayList<Point>();

		for (Feature feature : features) {
			if (feature instanceof Placemark) {
				final Placemark placemark = (Placemark) feature;
				if (placemark.getGeometry() instanceof LineString) {
					final LineString lineString = (LineString) placemark.getGeometry();
					List<Coordinate> coordinates = lineString.getCoordinates();
					for(Coordinate coord : coordinates) {
						Point point2 = new Point(
								coord.getLatitude(),
								coord.getLongitude(),
								coord.getAltitude());
						pointsToReturn.add(point2);
					}
					break;
				}
			}

		}
		return pointsToReturn;

	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void setGoogleApiKey(String googleApiKey) {
		Assert.hasText(googleApiKey, "The googleApiKey must not be empty.");

	}

}
