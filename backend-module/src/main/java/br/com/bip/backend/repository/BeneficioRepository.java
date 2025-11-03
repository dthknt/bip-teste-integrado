package br.com.bip.backend.repository;

import br.com.bip.ejb.entity.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

}
