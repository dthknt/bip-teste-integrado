package br.com.bip.ejb.validation;

import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;

@Stateless
public class TransferenciaValidator {

   public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to)
            throws TransferenciaException {

      // --- 1. Validação de Entrada ---
      if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
         throw new TransferenciaException("O valor da transferência deve ser positivo.");
      }
      if (fromId == null || toId == null) {
         throw new TransferenciaException("IDs de origem e destino não podem ser nulos.");
      }
      if (fromId.equals(toId)) {
         throw new TransferenciaException("A conta de origem e destino não podem ser as mesmas.");
      }

      // --- 2. Validação de Existência ---
      if (from == null) {
         throw new TransferenciaException("Conta de origem não encontrada (ID: " + fromId + ").");
      }
      if (to == null) {
         throw new TransferenciaException("Conta de destino não encontrada (ID: " + toId + ").");
      }

      // --- 3. Validação da Regra de Negócio (Saldo) ---
      if (from.getValor().compareTo(amount) < 0) {
         throw new TransferenciaException("Saldo insuficiente na conta de origem (ID: " + fromId + ").");
      }
   }
}
