package com.jav.sitrack.simulator.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jav.sitrack.simulator.GpsSimulator;
import com.jav.sitrack.simulator.model.DirectionInput;
import com.jav.sitrack.simulator.model.GpsSimulatorInstance;
import com.jav.sitrack.simulator.model.Point;
import com.jav.sitrack.simulator.service.GpsSimulatorFactory;
import com.jav.sitrack.simulator.service.IKmlService;
import com.jav.sitrack.simulator.service.IPathService;
import com.jav.sitrack.simulator.service.ISentStatusService;


@RestController
@RequestMapping("/api")
@EnableScheduling
public class RestApi {

	@Autowired
	private ISentStatusService sentStatusService;

	@Autowired
	private IPathService pathService;

	@Autowired
	private IKmlService kmlService;

	@Autowired
	private GpsSimulatorFactory gpsSimulatorFactory;

	@Autowired
	@Qualifier("sendPosition")
	private MessageChannel messageChannel;

	@Autowired
	private AsyncTaskExecutor taskExecutor;

	private Map<Long, GpsSimulatorInstance> taskFutures = new HashMap<>();

	long instanceCounter = 1;

	//private static Logger LOG = LoggerFactory.getLogger(RestApi.class);

	@RequestMapping("/initDrive")
	public List<GpsSimulatorInstance>dc() {
		List<DirectionInput> inputs = this.pathService.loadDirectionInput();
		List<GpsSimulatorInstance> instances = new ArrayList<>();
		Point lookAtPoint = null;

		final Set<Long> instanceIds = new HashSet<>(taskFutures.keySet());

		for (DirectionInput directionInput : inputs) {
			List<Point> points = this.pathService.getCoordinatesFromGoogle(directionInput);

			if (lookAtPoint == null) {
				lookAtPoint = points.get(0);
			}

			final GpsSimulator gpsSimulator = new GpsSimulator();
			gpsSimulator.setMessageChannel(messageChannel);
			gpsSimulator.setKmlService(kmlService);
			gpsSimulator.setShouldMove(true);
			gpsSimulator.setExportPositionsToKml(true);
			gpsSimulator.setSpeedInKph(40d);
			gpsSimulator.setId(instanceCounter);

			instanceIds.add(instanceCounter);
			gpsSimulatorFactory.prepareGpsSimulator(gpsSimulator, points);

			final Future<?> future = taskExecutor.submit(gpsSimulator);
			final GpsSimulatorInstance instance = new GpsSimulatorInstance(instanceCounter, gpsSimulator, future);
			taskFutures.put(instanceCounter, instance);
			instanceCounter++;
			instances.add(instance);
		}

		kmlService.setupKmlIntegration(instanceIds, lookAtPoint);

		return instances;
	}


	@RequestMapping("/start")
	public GpsSimulatorInstance start() {
		final GpsSimulator gpsSimulator = new GpsSimulator();
		gpsSimulator.setMessageChannel(messageChannel);
		gpsSimulator.setKmlService(kmlService);
		gpsSimulator.setShouldMove(true);
		gpsSimulator.setExportPositionsToKml(true);
		gpsSimulator.setSpeedInKph(40d);
		gpsSimulator.setId(instanceCounter);
		final Set<Long> instanceIds = new HashSet<>(taskFutures.keySet());
		instanceIds.add(instanceCounter);
		kmlService.setupKmlIntegration(instanceIds);

		gpsSimulatorFactory.prepareGpsSimulator(gpsSimulator, new File("src/data/test-route-1.kml"));

		final Future<?> future = taskExecutor.submit(gpsSimulator);
		final GpsSimulatorInstance instance = new GpsSimulatorInstance(instanceCounter, gpsSimulator, future);
		taskFutures.put(instanceCounter, instance);
		instanceCounter++;
		return instance;
	}

	@RequestMapping("/status")
	public Collection<GpsSimulatorInstance> status() {
		return taskFutures.values();
	}
	
	@Scheduled(fixedRate = 60000)
	@RequestMapping("/sentStatus")
	public void sentStatus() {
		sentStatusService.sentStatus(taskFutures.values());
	}

	@RequestMapping("/cancel")
	public int cancel() {
		int numberOfCancelledTasks = 0;
		for (Map.Entry<Long, GpsSimulatorInstance> entry : taskFutures.entrySet()) {
			GpsSimulatorInstance instance = entry.getValue();
			instance.getGpsSimulator().cancel();
			boolean wasCancelled = instance.getGpsSimulatorTask().cancel(true);
			if (wasCancelled) {
				numberOfCancelledTasks++;
			}
		}
		taskFutures.clear();
		return numberOfCancelledTasks;
	}

	@RequestMapping("/directions")
	public List<DirectionInput> directions() {
		return pathService.loadDirectionInput();
	}

}