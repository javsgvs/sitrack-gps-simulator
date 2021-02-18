package com.jav.sitrack.simulator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Main entry point for the GpsSimulator application.
 *
 */
@SpringBootApplication
@EnableScheduling
//@EnableIntegration
@ImportResource("classpath:spring-integration-context.xml")
public class SitrackGpsSimulatorApplication {

	@Autowired
	MessageSource messageSource;

	@Autowired
	Environment environment;

	public static void main(String[] args) throws Exception {
		final SpringApplication application = new SpringApplication(SitrackGpsSimulatorApplication.class);
		application.run(args);
	}

	@Bean
	public Jaxb2Marshaller getMarshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setClassesToBeBound(Kml.class);

		final Map<String,Object> map = new HashMap<>();
		map.put("jaxb.formatted.output", true);

		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}
}
