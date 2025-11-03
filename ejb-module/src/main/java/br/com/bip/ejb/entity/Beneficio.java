package br.com.bip.ejb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "BENEFICIO")
@SequenceGenerator(name = "beneficio_seq", sequenceName = "BENEFICIO_ID_SEQ", allocationSize = 1)
public class Beneficio implements Serializable {

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficio_seq")
   @Column(name = "ID")
   private Long id;

   @Column(name = "NOME", length = 100, nullable = false)
   private String nome;

   @Column(name = "DESCRICAO", length = 255)
   private String descricao;

   @Column(name = "VALOR", precision = 15, scale = 2, nullable = false)
   private BigDecimal valor;

   @Column(name = "ATIVO")
   private boolean ativo = true;

   @Version
   @Column(name = "VERSION")
   private Long version;

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Beneficio beneficio = (Beneficio) o;
      return id != null && Objects.equals(id, beneficio.id);
   }

   @Override
   public int hashCode() {
      // Usa a classe para garantir que proxies não conflitem
      return getClass().hashCode();
   }

   @Override
   public String toString() {
      return "Beneficio{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", valor=" + valor +
               ", ativo=" + ativo +
               ", version=" + version +
               '}';
   }
}
