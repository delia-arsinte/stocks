package com.payconiq.integration;

import com.payconiq.config.Application;
import com.payconiq.data.Stock;
import com.payconiq.pojos.UpdatedPrice;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StocksEndpointIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void findsAll() {
        ResponseEntity<List<Stock>> response =
                restTemplate
                        .withBasicAuth("user", "password")
                        .exchange("/api/stocks", HttpMethod.GET, null, new ParameterizedTypeReference<List<Stock>>() {
        });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Stock> responseBody = response.getBody();
        assertEquals(2, responseBody.size());

        Stock googleStock = responseBody.get(0);
        assertEquals("Google", googleStock.getName());

        Stock appleStock = responseBody.get(1);
        assertEquals("Apple", appleStock.getName());
    }

    @Test
    public void findOne() {
        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("user", "password")
                .exchange("/api/stocks/2", HttpMethod.GET, null, Stock.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Stock stock = response.getBody();
        assertEquals("Apple", stock.getName());
        assertEquals(new BigDecimal(300.5), stock.getCurrentPrice());
    }

    @Test
    public void findOneNotFound() {
        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("user", "password")
                .exchange("/api/stocks/200", HttpMethod.GET, null, Stock.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void saveOne() {
        Stock stock = Stock.builder()
                .name("Facebook")
                .lastUpdateTime(LocalDateTime.of(2018, 1, 14, 15, 34))
                .currentPrice(new BigDecimal(105.5))
                .build();

        HttpEntity<Stock> httpEntity = new HttpEntity<>(stock);

        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks", HttpMethod.POST, httpEntity, Stock.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Stock actualStock = response.getBody();
        assertEquals(stock.getName(), actualStock.getName());
        assertEquals(stock.getCurrentPrice(), actualStock.getCurrentPrice());
        assertEquals(stock.getLastUpdateTime(), actualStock.getLastUpdateTime());
    }

    @Test
    public void saveOneInvalidName() {
        Stock stock = Stock.builder()
                .name("Lorem ipsum dolor sit amet, nonummy ligula volutpat hac integer nonummy. Suspendisse ultricies," +
                        "congue etiam tellus, erat libero, nulla eleifend, mauris pellentesque. Suspendisse integer " +
                        "praesent vel, integer gravida mauris, fringilla vehicula lacinia non lorem ipsum")
                .lastUpdateTime(LocalDateTime.of(2018, 1, 14, 15, 34))
                .currentPrice(new BigDecimal(105.5))
                .build();

        HttpEntity<Stock> httpEntity = new HttpEntity<>(stock);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks", HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveOneEmptyName() {
        Stock stock = Stock.builder()
                .lastUpdateTime(LocalDateTime.of(2018, 1, 14, 15, 34))
                .currentPrice(new BigDecimal(105.5))
                .build();

        HttpEntity<Stock> httpEntity = new HttpEntity<>(stock);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks", HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void saveOneNullPrice() {
        Stock stock = Stock.builder()
                .name("Apple")
                .lastUpdateTime(LocalDateTime.of(2018, 1, 14, 15, 34))
                .build();

        HttpEntity<Stock> httpEntity = new HttpEntity<>(stock);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks", HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void updateOne() {
        UpdatedPrice updatedPrice = new UpdatedPrice();
        updatedPrice.setCurrentPrice(new BigDecimal(500.5));
        HttpEntity<UpdatedPrice> request = new HttpEntity<>(updatedPrice);

        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks/1", HttpMethod.PUT, request,
                Stock.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Stock actualStock = response.getBody();
        assertEquals(updatedPrice.getCurrentPrice(), actualStock.getCurrentPrice());

    }

    @Test
    public void updateOneNotFound() {
        UpdatedPrice updatedPrice = new UpdatedPrice();
        updatedPrice.setCurrentPrice(new BigDecimal(500.5));
        HttpEntity<UpdatedPrice> request = new HttpEntity<>(updatedPrice);

        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks/10", HttpMethod.PUT, request, Stock.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void updateOneInvalidPrice() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{ \"currentPrice\" : \"blah\" }", headers);

        ResponseEntity<Stock> response = restTemplate
                .withBasicAuth("admin", "admin")
                .exchange("/api/stocks/1", HttpMethod.PUT, request, Stock.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
