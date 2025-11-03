package br.com.bip.backend.service;

import br.com.bip.backend.dto.BeneficioRequestDTO;
import br.com.bip.backend.dto.BeneficioResponseDTO;
import br.com.bip.backend.dto.TransferRequestDTO;

import java.util.List;

public interface BeneficioService {

   List<BeneficioResponseDTO> findAll();

   BeneficioResponseDTO findById(Long id);

   BeneficioResponseDTO create(BeneficioRequestDTO requestDTO);

   BeneficioResponseDTO update(Long id, BeneficioRequestDTO requestDTO);

   void deleteById(Long id);

   void transferir(TransferRequestDTO requestDTO);
}
