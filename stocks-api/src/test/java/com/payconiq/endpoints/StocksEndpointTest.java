package com.payconiq.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payconiq.data.Stock;
import com.payconiq.data.StocksRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StocksEndpointTest {

    @Mock
    private StocksRepository stocksRepository;

    @InjectMocks
    private StocksEndpoint stocksEndpoint;

    private MockMvc mvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mvc = MockMvcBuilders
                .standaloneSetup(stocksEndpoint)
                .setHandlerExceptionResolvers(withExceptionControllerAdvice())
                .build();
    }

    @Test
    public void findsAll() throws Exception {
        Stock stock = Stock.builder()
                .currentPrice(new BigDecimal(200))
                .lastUpdateTime(LocalDateTime.of(2018, 1, 13, 20, 1))
                .name("A Company")
                .build();

        List<Stock> stocks = Arrays.asList(stock);
        when(stocksRepository.findAll()).thenReturn(stocks);

        ResultActions resultActions = mvc.perform(get("/api/stocks"));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name", is("A Company")))
                .andExpect(jsonPath("$.[0].currentPrice", is(200)));
    }

    @Test
    public void findsAllReturnsEmpty() throws Exception {
        when(stocksRepository.findAll()).thenReturn(Collections.emptyList());

        ResultActions resultActions = mvc.perform(get("/api/stocks"));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    public void findsAllExceptionFromRepository() throws Exception {
        when(stocksRepository.findAll()).thenThrow(new RuntimeException("Something bad happened."));

        ResultActions resultActions = mvc.perform(get("/api/stocks"));

        resultActions.andExpect(status().is5xxServerError());
    }

    @Test
    public void findsOne() throws Exception {
        Stock stock = Stock.builder()
                .currentPrice(new BigDecimal(200))
                .lastUpdateTime(LocalDateTime.of(2018, 1, 13, 20, 1))
                .name("A Company")
                .build();

        when(stocksRepository.findOne(Mockito.anyLong())).thenReturn(stock);

        ResultActions resultActions = mvc.perform(get("/api/stocks/1"));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("A Company")))
                .andExpect(jsonPath("$.currentPrice", is(200)));
    }

    @Test
    public void findsOneNotFound() throws Exception {
        when(stocksRepository.findOne(Mockito.anyLong())).thenReturn(null);

        ResultActions resultActions = mvc.perform(get("/api/stocks/1"));

        resultActions.andExpect(status().isNotFound());
    }


    @Test
    public void savesOne() throws Exception {
        Stock stock = Stock.builder()
                .currentPrice(new BigDecimal(200))
                .lastUpdateTime(LocalDateTime.of(2018, 1, 13, 20, 1))
                .name("A Company")
                .build();
        ResultActions resultActions = mvc.perform(post("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertStockToJson(stock)));

        verify(stocksRepository, times(1)).save(stock);

        resultActions.andExpect(status().is2xxSuccessful());

    }

    @Test
    public void savesOneException() throws Exception {
        Stock stock = Stock.builder()
                .currentPrice(new BigDecimal(200))
                .lastUpdateTime(LocalDateTime.of(2018, 1, 13, 20, 1))
                .name("A Company")
                .build();
        when(stocksRepository.save(Mockito.any(Stock.class))).thenThrow(new RuntimeException("Something bad"));

        ResultActions resultActions = mvc.perform(post("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertStockToJson(stock)));

        resultActions.andExpect(status().isInternalServerError());
    }

    @Test
    public void updatesOne() throws Exception {
        Stock stock = Stock.builder()
                .currentPrice(new BigDecimal(200))
                .lastUpdateTime(LocalDateTime.of(2018, 1, 13, 20, 1))
                .name("A Company")
                .build();

        when(stocksRepository.findOne(Mockito.anyLong())).thenReturn(stock);

        ResultActions resultActions = mvc.perform(put("/api/stocks/1").param("price", "300"));
        resultActions.andExpect(status().is2xxSuccessful());
        verify(stocksRepository, times(1)).save(stock);
    }

    @Test
    public void updatesOneNotFound() throws Exception {
        when(stocksRepository.findOne(Mockito.anyLong())).thenReturn(null);

        ResultActions resultActions = mvc.perform(put("/api/stocks/1").param("price", "300"));
        resultActions.andExpect(status().isNotFound());
    }

    private byte[] convertStockToJson(Stock stock) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ObjectMapper().writeValue(outputStream, stock);
        return outputStream.toByteArray();
    }

    /**
     * since mockMvc is initialized in standalone mode, we need a custom exception handler to make sure
     * the controller advice is loaded
     *
     * @return
     */
    private ExceptionHandlerExceptionResolver withExceptionControllerAdvice() {
        final ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            @Override
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(final HandlerMethod handlerMethod, final Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(StocksControllerAdvice.class).resolveMethod(exception);
                if (method != null) {
                    return new ServletInvocableHandlerMethod(new StocksControllerAdvice(), method);
                }
                return super.getExceptionHandlerMethod(handlerMethod, exception);
            }
        };
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }
}
