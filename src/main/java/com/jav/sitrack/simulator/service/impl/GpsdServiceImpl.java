package com.jav.sitrack.simulator.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.jav.sitrack.simulator.model.PositionInfo;
import com.jav.sitrack.simulator.service.IGpsdService;

public class GpsdServiceImpl implements IGpsdService, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(GpsdServiceImpl.class);

	private BufferedWriter pipeWriter;

	public static final String GPSD_PIPE = "/tmp/gps";

	@Override
	public void updatePosition(PositionInfo position) {

		// an NMEA RMC position sentence (report) is of form:
		// $GPRMC,124426,A,5920.7019,N,02803.2893,E,,,121212,,

		Calendar cal = Calendar.getInstance();
		Integer hour = cal.get(Calendar.HOUR_OF_DAY);
		Integer minute = cal.get(Calendar.MINUTE);
		Integer second = cal.get(Calendar.SECOND);
		Integer date = cal.get(Calendar.DATE);
		Integer month = cal.get(Calendar.MONTH) + 1; // java Calendar month
														// starts at 0
		Integer year = cal.get(Calendar.YEAR) % 100; // convert to 2 digit year

		String sHour = String.format("%02d", hour);
		String sMinute = String.format("%02d", minute);
		String sSecond = String.format("%02d", second);
		String sDate = String.format("%02d", date);
		String sMonth = String.format("%02d", month);
		String sYear = String.format("%02d", year);

		String sentence = null;
		if (position == null) {
			sentence = "$GPRMC," + sHour + sMinute + sSecond + ",A,,,,,,," + sDate + sMonth + sYear + ",,";
		} else {
			double lat = position.getPosition().getLatitude();
			double lon = position.getPosition().getLongitude();

			String latSuffix = "N";
			if (lat < 0) {
				latSuffix = "S";
			}
			lat = Math.abs(lat);
			String lonSuffix = "E";
			if (lon < 0) {
				lonSuffix = "W";
			}
			lon = Math.abs(lon);

			long latDeg = (long) lat; // degree part
			double latMin = (lat - latDeg) * 60; // minute part
			long latMinWhole = (long) latMin; // whole part of minute
			Double latMinFrac = latMin - latMinWhole; // fractional part of
														// minute
			String sLatDeg = String.format("%02d", latDeg);
			String sLatMinWhole = String.format("%02d", latMinWhole);
			String sLatMinFrac = latMinFrac.toString().replace("0.", ".");
			long lonDeg = (long) lon; // degree part
			double lonMin = (lon - lonDeg) * 60; // minute part
			long lonMinWhole = (long) lonMin; // whole part of minute
			Double lonMinFrac = lonMin - lonMinWhole; // fractional part of
														// minute
			String sLonDeg = String.format("%02d", lonDeg);
			String sLonMinWhole = String.format("%02d", lonMinWhole);
			String sLonMinFrac = lonMinFrac.toString().replace("0.", ".");

			sentence = "$GPRMC," + sHour + sMinute + sSecond + ",A," + sLatDeg + sLatMinWhole + sLatMinFrac + ","
					+ latSuffix + "," + sLonDeg + sLonMinWhole + sLonMinFrac + "," + lonSuffix + ",,," + sDate + sMonth
					+ sYear + ",,";
		}

		try {
			pipeWriter.write(sentence + "\r\n");
			pipeWriter.flush();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Initialize gpsd.
	 * @return error message, if any
	 */
	void initGpsd() throws IOException, InterruptedException {
		startProc("killall -9 gpsd", false);
		startProc("rm -f " + GPSD_PIPE, false);
		startProc("mkfifo " + GPSD_PIPE, false);
		startProc("gpsd " + GPSD_PIPE, false);
		//writer for gpsd pipe
		pipeWriter = new BufferedWriter(new FileWriter(GPSD_PIPE));

	}

	/**
	 * Start given process.
	 * @param command
	 * @param wait for process to exit
	 * @return
	 */
	int startProc(String command, Boolean wait) throws IOException, InterruptedException {
		String[] commandArray = command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(commandArray);
		pb.redirectErrorStream(true);   //redirect errorstream and outputstream to single stream
		Process proc = pb.start();
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		//empty the output buff of the proc
		while ((line = in.readLine()) != null) {
			LOGGER.info(line);
		}

		if (wait) {
			return proc.waitFor();
		} else {
			return 0;
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (System.getProperty("os.name").contains("Linux")) {
			try {
				initGpsd();
			} catch (IOException | InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
