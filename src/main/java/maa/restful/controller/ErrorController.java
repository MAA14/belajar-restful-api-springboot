package maa.restful.controller;

import jakarta.validation.ConstraintViolationException;
import maa.restful.model.WebResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// Untuk menangkap error kita butuh @RestControllerAdvice
// Disini isinya buat nangkep semua type Exception
@RestControllerAdvice
public class ErrorController {

    // Nangkep ContraintViolationException || Error di Validation
    // ResponseEntity adalah class yang digunakan untuk mengirim response HTTP
    // ResponseEntity<TypeDataOrClass>
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<String>> constraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.<String>builder().errors(exception.getMessage()).build());
    };

    // Nangkep Error di API yang make throw ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> apiException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(WebResponse.<String>builder().errors(exception.getMessage()).build());
    }
}
