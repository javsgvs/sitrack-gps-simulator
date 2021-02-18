package com.jav.sitrack.simulator.service.impl;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jav.sitrack.simulator.model.GpsSimulatorInstance;
import com.jav.sitrack.simulator.model.TrackRequest;
import com.jav.sitrack.simulator.service.ISentStatusService;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SentStatusServiceImpl implements ISentStatusService {

    private static Logger LOGGER = LoggerFactory.getLogger(SentStatusServiceImpl.class);

    private final String REPORT_URI = "https://test-externalrgw.ar.sitrack.com/frame";
    private final String REPORT_TYPE = "2";
    private final Double GPS_DOP = 1.0;
    private final String LOGIN_CODE = "98173";
    private final String TEXT = "JAVIER GALLARDO";
    private final String TEXT_LABEL = "TAG";

    @Override
    public void sentStatus(Collection<GpsSimulatorInstance> instanceCollection) {
        LOGGER.info("***init track");
        // LOGGER.info("track::"+instanceCollection);

        for (GpsSimulatorInstance gpsSimulatorInstance : instanceCollection) {
            TrackRequest trackRequest = new TrackRequest();

            //trackRequest.setLoginCode(String.valueOf(gpsSimulatorInstance.getGpsSimulator().getId()));
            trackRequest.setLoginCode(LOGIN_CODE);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            trackRequest.setReportDate(sdf.format(new Date()));
            trackRequest.setReportType(REPORT_TYPE);
            trackRequest.setLatitude(gpsSimulatorInstance.getGpsSimulator().getCurrentPosition().getPosition().getLatitude());
            trackRequest.setLongitude(gpsSimulatorInstance.getGpsSimulator().getCurrentPosition().getPosition().getLongitude());
            trackRequest.setGpsDop(GPS_DOP);

            trackRequest.setSpeed(gpsSimulatorInstance.getGpsSimulator().getSpeedInKph());
            trackRequest.setText(TEXT);
            trackRequest.setTextLabel(TEXT_LABEL);

            LOGGER.info("request=>" + trackRequest);

            RestTemplate restTemplate = new RestTemplate();
            // restTemplate.put(REPORT_URI, trackRequest);
            // String result = restTemplate.getForObject(REPORT_URI, String.class);

            ObjectMapper mapper = new ObjectMapper();
            String trackRequestString = "";
            try {
                trackRequestString = mapper.writeValueAsString(trackRequest);
            } catch (JsonProcessingException e) {
                LOGGER.error(e.toString());
            }

            HttpHeaders headers = createHeaders();
            LOGGER.info("headers::" + headers);
            HttpEntity<String> request = new HttpEntity<>(trackRequestString,headers);
            //HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(REPORT_URI, HttpMethod.PUT, request, String.class);

            LOGGER.info("response=>"+response.getBody());
            //restTemplate.put(REPORT_URI, entity);

        }

    }
    private HttpHeaders createHeaders() {

        String applicationString = "ReportGeneratorTest";
        String secretKeyString = "ccd517a1-d39d-4cf6-af65-28d65e192149";
        Instant timestampObject = Instant.now();

        String signatureHash = applicationString + secretKeyString + timestampObject.getEpochSecond();

        return new HttpHeaders() {
                private static final long serialVersionUID = 1L;
            {
              try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] md5Result = md.digest(signatureHash.getBytes(Charset.forName("US-ASCII")));
                byte[] signatureByte = Base64.encodeBase64(md5Result);
                
                //Authorization: SWSAuth application="ID",signature="HASH",timestamp="SECONDS"
                String application = "SWSAuth application=" + "\""+applicationString+"\"";
                String signature = "signature=" + "\""+new String(signatureByte)+"\"";
                String timestamp = "timestamp=" + "\""+timestampObject+"\"";
                //String signature = application + " signature " + new String(signatureByte) + " " + timestamp;
                String auth = application+"," + signature+"," + timestamp;

                set( "Authorization: ", auth );
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e.toString());
            }
           }};
    }
}
