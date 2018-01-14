package com.payconiq.endpoints;

import com.payconiq.data.Stock;
import com.payconiq.data.StocksRepository;
import com.payconiq.exception.StockNotFoundException;
import com.payconiq.pojos.UpdatedPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StocksEndpoint {

    private final StocksRepository stocksRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Stock> findAll() {
        return stocksRepository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Stock findOne(@PathVariable("id") Long id) {
        return Optional.ofNullable(stocksRepository.findOne(id))
                .orElseThrow(() -> new StockNotFoundException(String.format("No stock was found for id [%s]", id)));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Stock update(@PathVariable("id") Long id, @RequestBody UpdatedPrice updatedPrice) {
        Stock stock = Optional.ofNullable(stocksRepository.findOne(id)).orElseThrow(()
                -> new StockNotFoundException(String.format("No stock was found for id [%s]", id)));

        stock.setCurrentPrice(updatedPrice.getCurrentPrice());
        stock.setLastUpdateTime(LocalDateTime.now());

        return stocksRepository.save(stock);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Stock> create(@RequestBody Stock stock) {
        return new ResponseEntity<>(stocksRepository.save(stock), HttpStatus.CREATED);
    }
}
