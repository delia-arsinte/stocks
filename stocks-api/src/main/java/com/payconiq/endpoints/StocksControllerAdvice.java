package com.payconiq.endpoints;

import com.payconiq.exception.StockNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class StocksControllerAdvice {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such Stock")
    @ExceptionHandler(value = StockNotFoundException.class)
    public void handleStockNotFound(StockNotFoundException ex) {
        log.error("Stock not found", ex);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid data")
    @ExceptionHandler(value = ConstraintViolationException.class)
    public void constraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation", ex);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unable to read request")
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public void httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Unable to parse request", ex);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal server error")
    @ExceptionHandler(value = Exception.class)
    public void defaultExceptionHandler(Exception ex) {
        log.error("Unexpected exception occurred, returning 500", ex);
    }

}
