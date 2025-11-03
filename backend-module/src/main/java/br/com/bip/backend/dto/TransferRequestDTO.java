package br.com.bip.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDTO {

   public TransferRequestDTO(){}

   public TransferRequestDTO(Long idBeneficioOrigem, Long idBeneficioDestino, BigDecimal valor) {
      this.idBeneficioOrigem = idBeneficioOrigem;
      this.idBeneficioDestino = idBeneficioDestino;
      this.valor = valor;
   }

   @NotNull(message = "ID de origem não pode ser nulo")
   private Long idBeneficioOrigem;

   @NotNull(message = "ID de destino não pode ser nulo")
   private Long idBeneficioDestino;

   @NotNull(message = "Valor não pode ser nulo")
   @DecimalMin(value = "0.01", message = "O valor da transferência deve ser positivo")
   private BigDecimal valor;

}
