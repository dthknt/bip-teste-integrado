package br.com.bip.ejb;

import br.com.bip.ejb.entity.Beneficio;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface BeneficioEjbServiceLocal {
   List<Beneficio> findAll();

   Beneficio findById(Long id);

   Beneficio create(Beneficio beneficio);

   Beneficio update(Beneficio beneficio);

   void deleteById(Long id);
   void transfer(Long fromId, Long toId, BigDecimal amount);
}