package org.mrshoffen.tasktracker.auth.util.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class IpApiClient {

    @Data
    public static class IpInfo {
        private String timeZone = "Europe/Moscow";
        private String country = "undefined";
        private String region = "undefined";
    }


    private final RestClient restClient = RestClient.create("http://ip-api.com/json");

    public IpInfo getIpInfo(String ip) {
        if (ip == null) {
            return new IpInfo();
        }

        try {
            Map<String, String> body = restClient.get()
                    .uri("/{ip}", ip)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            return mapResponseBody(body);
        } catch (Exception e) {
            return new IpInfo();
        }
    }

    private IpInfo mapResponseBody(Map<String, String> body) {
        IpInfo result = new IpInfo();
        if (body.get("timezone") != null) {
            result.setTimeZone(body.get("timezone"));
        }
        if (body.get("country") != null) {
            result.setCountry(body.get("country"));
        }
        if (body.get("regionName") != null) {
            result.setRegion(body.get("regionName"));
        }

        return result;
    }


}
