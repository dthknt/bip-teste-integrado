package br.com.bip.validation;

import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import br.com.bip.ejb.validation.TransferenciaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferenciaValidatorTest {

   private TransferenciaValidator validator;
   private Beneficio beneficioOrigem;
   private Beneficio beneficioDestino;
   private final Long idBeneficioOrigem = 1L;
   private final Long idBeneficioDestino = 2L;
   private final BigDecimal valor = new BigDecimal("100.00");

   @BeforeEach
   void setUp() {
      validator = new TransferenciaValidator();

      beneficioOrigem = new Beneficio();
      beneficioOrigem.setId(idBeneficioOrigem);
      beneficioOrigem.setValor(new BigDecimal("1000.00"));

      beneficioDestino = new Beneficio();
      beneficioDestino.setId(idBeneficioDestino);
      beneficioDestino.setValor(new BigDecimal("500.00"));
   }

   @Test
   void validar_ShouldNotThrow_WhenValid() {
      assertDoesNotThrow(() -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, valor, beneficioOrigem, beneficioDestino);
      });
   }

   @Test
   void validar_ShouldThrowException_WhenAmountIsNull() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, null, beneficioOrigem, beneficioDestino);
      });
      assertEquals("O valor da transferência deve ser positivo.", ex.getMessage());
   }

   @ParameterizedTest
   @ValueSource(strings = {"0.00", "-100.00"})
   void validar_ShouldThrowException_WhenAmountIsZeroOrNegative(String value) {
      BigDecimal invalidAmount = new BigDecimal(value);

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, invalidAmount, beneficioOrigem, beneficioDestino);
      });

      assertEquals("O valor da transferência deve ser positivo.", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenFromIdIsNull() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(null, idBeneficioDestino, valor, beneficioOrigem, beneficioDestino);
      });
      assertEquals("IDs de origem e destino não podem ser nulos.", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenToIdIsNull() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, null, valor, beneficioOrigem, beneficioDestino);
      });
      assertEquals("IDs de origem e destino não podem ser nulos.", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenIdsAreEqual() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioOrigem, valor, beneficioOrigem, beneficioOrigem);
      });
      assertEquals("A conta de origem e destino não podem ser as mesmas.", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenFromBeneficioIsNull() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, valor, null, beneficioDestino);
      });
      // Mensagem de erro atualizada para usar a variável
      assertEquals("Conta de origem não encontrada (ID: " + idBeneficioOrigem + ").", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenToBeneficioIsNull() {
      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, valor, beneficioOrigem, null);
      });
      // Mensagem de erro atualizada para usar a variável
      assertEquals("Conta de destino não encontrada (ID: " + idBeneficioDestino + ").", ex.getMessage());
   }

   @Test
   void validar_ShouldThrowException_WhenInsufficientFunds() {
      BigDecimal largeAmount = new BigDecimal("1000.01"); // Origem só tem 1000.00

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         validator.validar(idBeneficioOrigem, idBeneficioDestino, largeAmount, beneficioOrigem, beneficioDestino);
      });

      // Mensagem de erro atualizada para usar a variável
      assertEquals("Saldo insuficiente na conta de origem (ID: " + idBeneficioOrigem + ").", ex.getMessage());
   }
}

