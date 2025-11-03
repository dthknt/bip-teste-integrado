package br.com.bip.backend.handler;

import br.com.bip.ejb.exception.TransferenciaException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

   private Map<String, Object> createErrorBody(HttpStatus status, String message, String path) {
      Map<String, Object> body = new HashMap<>();
      body.put("timestamp", Instant.now().toString());
      body.put("status", status.value());
      body.put("error", status.getReasonPhrase());
      body.put("message", message);
      body.put("path", path.replace("uri=", ""));
      return body;
   }

   @ExceptionHandler(EntityNotFoundException.class)
   public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {

      Map<String, Object> body = createErrorBody(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false));
      return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
   }

   @ExceptionHandler(TransferenciaException.class)
   public ResponseEntity<Map<String, Object>> handleTransferenciaException(
            TransferenciaException ex, WebRequest request) {

      Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(false));
      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

      String errors = ex.getBindingResult().getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.joining(", "));

      Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Erro de validação: " + errors, request.getDescription(false));
      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

      // É uma boa prática logar a exceção original no console
      // ex.printStackTrace();

      Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor.", request.getDescription(false));
      return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
   }
}
