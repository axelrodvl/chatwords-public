package co.axelrod.chatwords.image;

import co.axelrod.chatwords.dictionary.model.Language;
import co.axelrod.chatwords.image.model.ShutterstockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShutterstockClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public String getImageUrl(String word, Language language) {
        try {
            log.info("Calling Shutterstock for word: " + word);

            String url = "https://api.shutterstock.com/v2/images/search"
                    + "?query=" + word
                    + "&image_type=photo"
                    + "&orientation=horizontal"
                    + "&people_number=0"
                    + "&page=1"
                    + "&per_page=1"
                    + "&fields=data(assets/huge_thumb/url)";

            // cs, da, de, el, en, es, fi, fr, hu, id, it, ja, ko, nb, nl, pl, pt, ro, ru, sv, th, tr, vi, zh, zh-Hant
            if (!language.equals(Language.UKRAINIAN)) {
                url += "&language=" + language.getValue();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer v2/SET_VALUE");

            HttpEntity httpEntity = new HttpEntity(headers);

            ShutterstockResponse response = null;
            Instant start = Instant.now();
            response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ShutterstockResponse.class).getBody();
            Instant end = Instant.now();
            log.info("Shutterstock call processed in: " + Duration.between(start, end).toString()); // prints PT1M3.553S

            if (response != null && !response.getData().isEmpty()) {
                String imageUrl = response.getData().get(0).getAssets().getHugeThumb().getUrl();
                log.info("Chosen image: " + imageUrl);
                return imageUrl;
            } else {
                log.info("No image chosen or not found");
                return null;
            }
        } catch (Exception ex) {
            log.error("Error occured when calling Shutterstock");
            return null;
        }
    }
}
