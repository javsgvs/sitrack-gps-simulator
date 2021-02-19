package com.jav.sitrack.simulator.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

import com.jav.sitrack.simulator.model.GpsSimulatorInstance;
import com.jav.sitrack.simulator.model.TrackRequest;
import com.jav.sitrack.simulator.service.ISentStatusService;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
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

            HttpHeaders headers = createHeaders();
            LOGGER.info("headers::" + headers);

            HttpEntity<TrackRequest> request = new HttpEntity<>(trackRequest,headers);
            try {
                ResponseEntity<TrackRequest> response = restTemplate.postForEntity(REPORT_URI, request, TrackRequest.class);
                LOGGER.info("STATUS_CODE::"+response.getStatusCode());
                LOGGER.info("BODY::"+response.getBody());
            } catch(HttpStatusCodeException e) {
                LOGGER.error("MESSAGE::" + e.getMessage());
                LOGGER.error("STATUS_CODE:: " + e.getStatusCode());
                LOGGER.error("HEADERS::" + e.getResponseHeaders());
                //TODO retry
            }

        }

    }

    private HttpHeaders createHeaders() {
        String applicationString = "ReportGeneratorTest";
        String secretKeyString = "ccd517a1-d39d-4cf6-af65-28d65e192149";
        Instant timestampInstant = Instant.now();
        String timestampInstantString = String.valueOf(timestampInstant.getEpochSecond());

        String signatureHash = applicationString + secretKeyString + timestampInstantString;
        LOGGER.info("signatureHash::" + signatureHash);
        String md5String = getMd5(signatureHash);

        String application = "SWSAuth application=" + "\""+applicationString+"\"";
        String signature = "signature=" + "\""+md5String+"\"";
        String timestamp = "timestamp=" + "\""+timestampInstantString+"\"";
        //String signature = application + " signature " + new String(signatureByte) + " " + timestamp;
        String auth = application+"," + signature+"," + timestamp;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",auth);
        return headers;
    }

    public final String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            //md.update(input.getBytes(StandardCharsets.UTF_8));

            String md5 = Base64.encodeBase64String(md.digest(input.getBytes()));
            LOGGER.info("MD5::"+md5);
            return md5;

            //md.update(input.getBytes(StandardCharsets.UTF_8));
            //md5 = String.format("%032x", new BigInteger(1, md.digest()));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage());
            return "ERROR";
        }
    }
}
