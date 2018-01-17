package com.payconiq.endpoints;

import com.payconiq.data.Stock;
import com.payconiq.data.StocksRepository;
import com.payconiq.exception.StockNotFoundException;
import com.payconiq.pojos.UpdatedPrice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StocksEndpoint {

    private final StocksRepository stocksRepository;

    @PreAuthorize("hasRole('ROLE_VIEW')")
    @RequestMapping(method = RequestMethod.GET)
    public List<Stock> findAll() {
        return stocksRepository.findAll();
    }

    @PreAuthorize("hasRole('ROLE_VIEW')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Stock findOne(@PathVariable("id") Long id) {
        return Optional.ofNullable(stocksRepository.findOne(id))
                .orElseThrow(() -> new StockNotFoundException(String.format("No stock was found for id [%s]", id)));
    }

    @PreAuthorize("hasRole('ROLE_UPDATE')")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Stock update(@PathVariable("id") Long id, @RequestBody UpdatedPrice updatedPrice) {
        Stock stock = Optional.ofNullable(stocksRepository.findOne(id)).orElseThrow(()
                -> new StockNotFoundException(String.format("No stock was found for id [%s]", id)));

        stock.setCurrentPrice(updatedPrice.getCurrentPrice());
        stock.setLastUpdateTime(LocalDateTime.now());

        return stocksRepository.save(stock);
    }

    @PreAuthorize("hasRole('ROLE_UPDATE')")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Stock> create(@RequestBody Stock stock) {
        return new ResponseEntity<>(stocksRepository.save(stock), HttpStatus.CREATED);
    }
}
