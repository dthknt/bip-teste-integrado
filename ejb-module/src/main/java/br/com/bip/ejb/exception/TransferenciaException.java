package br.com.bip.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TransferenciaException extends RuntimeException {

   public TransferenciaException(String message) {
      super(message);
   }

   public TransferenciaException(String message, Throwable cause) {
      super(message, cause);
   }
}
