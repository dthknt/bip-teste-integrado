package br.com.bip.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BeneficioRequestDTO {

   @NotBlank(message = "Nome não pode ser vazio")
   private String nome;

   private String descricao;

   @NotNull(message = "Valor não pode ser nulo")
   @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
   private BigDecimal valor;

   private Boolean ativo = true;
}
