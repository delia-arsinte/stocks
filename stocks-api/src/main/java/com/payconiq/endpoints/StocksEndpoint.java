package com.payconiq.endpoints;

import com.payconiq.data.Stock;
import com.payconiq.data.StocksRepository;
import com.payconiq.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
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
    public void update(@PathVariable("id") Long id, BigDecimal price) {
        Stock stock = Optional.ofNullable(stocksRepository.findOne(id)).orElseThrow(()
                -> new StockNotFoundException(String.format("No stock was found for id [%s]", id)));

        stock.setCurrentPrice(price);
        stock.setLastUpdateTime(LocalDateTime.now());

        stocksRepository.save(stock);

        log.info("Updated stock with id {} successfully", id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody Stock stock) {
        stocksRepository.save(stock);
    }
}
