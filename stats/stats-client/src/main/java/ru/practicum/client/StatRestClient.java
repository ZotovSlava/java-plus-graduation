package ru.practicum.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.util.List;

@Component
public class StatRestClient {

    private final RestClient restClient = RestClient.builder().build();
    private final DiscoveryClient discoveryClient;
    private final String statsServiceId = "stats-server";

    public StatRestClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    private String getStatsBaseUrl() {
        ServiceInstance instance = discoveryClient.getInstances(statsServiceId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Сервис статистики недоступен: " + statsServiceId));
        return instance.getUri().toString();
    }

    public void saveHit(HitDto hitDto) {
        String baseUrl = getStatsBaseUrl();

        restClient.post()
                .uri(baseUrl +"/hit")
                .body(hitDto)
                .retrieve()
                .onStatus(
                        status -> status != HttpStatus.CREATED,
                        (request, response) -> {
                            throw new RuntimeException("Не удалось сохранить Hit: " + response.getStatusCode());
                        }
                )
                .toBodilessEntity();
    }


    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String baseUrl = getStatsBaseUrl();

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris != null && !uris.isEmpty() ? String.join(",", uris) : null)
                .queryParam("unique", unique)
                .build()
                .toUriString();

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StatsDto>>() {
                });
    }
}
