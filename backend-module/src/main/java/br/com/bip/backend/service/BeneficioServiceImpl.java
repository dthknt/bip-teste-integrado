package br.com.bip.backend.service;

import br.com.bip.backend.dto.BeneficioRequestDTO;
import br.com.bip.backend.dto.BeneficioResponseDTO;
import br.com.bip.backend.dto.TransferRequestDTO;
import br.com.bip.ejb.BeneficioEjbServiceLocal;
import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BeneficioServiceImpl implements BeneficioService {

   private final BeneficioEjbServiceLocal beneficioEjbService;

   public BeneficioServiceImpl(BeneficioEjbServiceLocal ejb) {
      this.beneficioEjbService = ejb;
   }

   @Override
   @Transactional(readOnly = true)
   public List<BeneficioResponseDTO> findAll() {
      List<Beneficio> entidades = beneficioEjbService.findAll();
      return entidades.stream()
               .map(this::toResponseDTO)
               .collect(Collectors.toList());
   }

   @Override
   @Transactional(readOnly = true)
   public BeneficioResponseDTO findById(Long id) {
      Beneficio beneficio = beneficioEjbService.findById(id);

      if (beneficio == null) {
         throw new EntityNotFoundException("Benefício não encontrado com ID: " + id);
      }
      return toResponseDTO(beneficio);
   }

   @Override
   public BeneficioResponseDTO create(BeneficioRequestDTO requestDTO) {
      Beneficio novaEntidade = toEntity(requestDTO);
      Beneficio entidadeSalva = beneficioEjbService.create(novaEntidade);
      return toResponseDTO(entidadeSalva);
   }

   @Override
   public BeneficioResponseDTO update(Long id, BeneficioRequestDTO requestDTO) {
      Beneficio entidadeExistente = beneficioEjbService.findById(id);
      if (entidadeExistente == null) {
         throw new EntityNotFoundException("Benefício não encontrado com ID: " + id);
      }

      Beneficio entidadeParaAtualizar = toEntity(requestDTO);
      entidadeParaAtualizar.setId(id);
      entidadeParaAtualizar.setVersion(entidadeExistente.getVersion());

      Beneficio entidadeAtualizada = beneficioEjbService.update(entidadeParaAtualizar);

      return toResponseDTO(entidadeAtualizada);
   }

   @Override
   public void deleteById(Long id) {
      Beneficio beneficio = beneficioEjbService.findById(id);
      if (beneficio == null) {
         throw new EntityNotFoundException("Benefício não encontrado com ID: " + id);
      }

      beneficioEjbService.deleteById(id);
   }

   @Override
   public void transferir(TransferRequestDTO requestDTO) {
      try {
         beneficioEjbService.transfer(
                  requestDTO.getIdBeneficioOrigem(),
                  requestDTO.getIdBeneficioDestino(),
                  requestDTO.getValor()
         );
      } catch (Exception e) {
         throw new TransferenciaException(e.getMessage());
      }
   }

   private BeneficioResponseDTO toResponseDTO(Beneficio beneficio) {
      if (beneficio == null) return null;
      BeneficioResponseDTO dto = new BeneficioResponseDTO();
      dto.setId(beneficio.getId());
      dto.setNome(beneficio.getNome());
      dto.setDescricao(beneficio.getDescricao());
      dto.setValor(beneficio.getValor());
      dto.setAtivo(beneficio.isAtivo());
      return dto;
   }

   private Beneficio toEntity(BeneficioRequestDTO requestDTO) {
      if (requestDTO == null) return null;
      Beneficio beneficio = new Beneficio();
      beneficio.setNome(requestDTO.getNome());
      beneficio.setDescricao(requestDTO.getDescricao());
      beneficio.setValor(requestDTO.getValor());
      beneficio.setAtivo(requestDTO.getAtivo());
      return beneficio;
   }
}

