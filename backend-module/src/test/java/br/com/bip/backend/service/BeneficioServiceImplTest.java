package br.com.bip.backend.service;

import br.com.bip.backend.dto.BeneficioRequestDTO;
import br.com.bip.backend.dto.BeneficioResponseDTO;
import br.com.bip.backend.dto.TransferRequestDTO;
import br.com.bip.ejb.BeneficioEjbServiceLocal;
import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceImplTest {

   @Mock
   private BeneficioEjbServiceLocal beneficioEjbServiceMock;

   private BeneficioService beneficioService;

   private Beneficio beneficio;
   private BeneficioRequestDTO requestDTO;

   @BeforeEach
   void setUp() {
      beneficioService = new BeneficioServiceImpl(beneficioEjbServiceMock);

      beneficio = new Beneficio();
      beneficio.setId(1L);
      beneficio.setNome("Vale Refeição");
      beneficio.setDescricao("Crédito mensal");
      beneficio.setValor(new BigDecimal("500.00"));
      beneficio.setAtivo(true);
      beneficio.setVersion(0L);

      requestDTO = new BeneficioRequestDTO();
      requestDTO.setNome("Vale Refeição Novo");
      requestDTO.setDescricao("Nova Descrição");
      requestDTO.setValor(new BigDecimal("600.00"));
      requestDTO.setAtivo(true);
   }

   @Test
   void findAll_ShouldReturnDTOList() {
      when(beneficioEjbServiceMock.findAll()).thenReturn(List.of(beneficio));

      List<BeneficioResponseDTO> result = beneficioService.findAll();

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Vale Refeição", result.get(0).getNome());
      verify(beneficioEjbServiceMock).findAll();
   }

   @Test
   void findById_ShouldReturnDTO_WhenIdExists() {
      when(beneficioEjbServiceMock.findById(1L)).thenReturn(beneficio);

      BeneficioResponseDTO result = beneficioService.findById(1L);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("Vale Refeição", result.getNome());
      verify(beneficioEjbServiceMock).findById(1L);
   }

   @Test
   void findById_ShouldThrowEntityNotFound_WhenIdDoesNotExist() {
      when(beneficioEjbServiceMock.findById(99L)).thenReturn(null);

      EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
         beneficioService.findById(99L);
      });

      assertEquals("Benefício não encontrado com ID: 99", ex.getMessage());
      verify(beneficioEjbServiceMock).findById(99L);
   }

   @Test
   void create_ShouldReturnNewDTO() {
      ArgumentCaptor<Beneficio> beneficioCaptor = ArgumentCaptor.forClass(Beneficio.class);
      Beneficio beneficioSalvo = new Beneficio();
      beneficioSalvo.setId(1L);
      beneficioSalvo.setNome(requestDTO.getNome());
      beneficioSalvo.setValor(requestDTO.getValor());

      when(beneficioEjbServiceMock.create(beneficioCaptor.capture())).thenReturn(beneficioSalvo);

      BeneficioResponseDTO result = beneficioService.create(requestDTO);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("Vale Refeição Novo", result.getNome());
      assertEquals("Vale Refeição Novo", beneficioCaptor.getValue().getNome());
      verify(beneficioEjbServiceMock).create(any(Beneficio.class));
   }

   @Test
   void update_ShouldReturnUpdatedDTO_WhenIdExists() {
      when(beneficioEjbServiceMock.findById(1L)).thenReturn(beneficio);

      Beneficio beneficioAtualizado = new Beneficio();
      beneficioAtualizado.setId(1L);
      beneficioAtualizado.setNome(requestDTO.getNome());
      beneficioAtualizado.setValor(requestDTO.getValor());
      beneficioAtualizado.setVersion(1L);

      ArgumentCaptor<Beneficio> beneficioCaptor = ArgumentCaptor.forClass(Beneficio.class);
      when(beneficioEjbServiceMock.update(beneficioCaptor.capture())).thenReturn(beneficioAtualizado);

      BeneficioResponseDTO result = beneficioService.update(1L, requestDTO);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("Vale Refeição Novo", result.getNome());

      Beneficio capturado = beneficioCaptor.getValue();
      assertEquals(1L, capturado.getId());
      assertEquals("Vale Refeição Novo", capturado.getNome());
      assertEquals(0L, capturado.getVersion());

      verify(beneficioEjbServiceMock).findById(1L);
      verify(beneficioEjbServiceMock).update(any(Beneficio.class));
   }

   @Test
   void update_ShouldThrowEntityNotFound_WhenIdDoesNotExist() {
      when(beneficioEjbServiceMock.findById(99L)).thenReturn(null);

      EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
         beneficioService.update(99L, requestDTO);
      });

      assertEquals("Benefício não encontrado com ID: 99", ex.getMessage());
      verify(beneficioEjbServiceMock).findById(99L);
      verify(beneficioEjbServiceMock, never()).update(any());
   }

   @Test
   void deleteById_ShouldCallEjbDelete_WhenIdExists() {
      when(beneficioEjbServiceMock.findById(1L)).thenReturn(beneficio);
      doNothing().when(beneficioEjbServiceMock).deleteById(1L);

      beneficioService.deleteById(1L);

      verify(beneficioEjbServiceMock).findById(1L);
      verify(beneficioEjbServiceMock).deleteById(1L);
   }

   @Test
   void deleteById_ShouldThrowEntityNotFound_WhenIdDoesNotExist() {
      when(beneficioEjbServiceMock.findById(99L)).thenReturn(null);

      EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
         beneficioService.deleteById(99L);
      });

      assertEquals("Benefício não encontrado com ID: 99", ex.getMessage());
      verify(beneficioEjbServiceMock).findById(99L);
      verify(beneficioEjbServiceMock, never()).deleteById(anyLong());
   }

   @Test
   void transferir_ShouldCallEjbTransfer() {
      TransferRequestDTO transferDTO = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

      doNothing().when(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));

      beneficioService.transferir(transferDTO);

      verify(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));
   }

   @Test
   void transferir_ShouldThrowTransferenciaException_WhenEjbFails() {
      TransferRequestDTO transferDTO = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));
      String errorMsg = "Saldo insuficiente";

      doThrow(new TransferenciaException(errorMsg)).when(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         beneficioService.transferir(transferDTO);
      });

      assertEquals(errorMsg, ex.getMessage());
      verify(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));
   }

   @Test
   void transferir_ShouldWrapGenericExceptionInTransferenciaException() {
      TransferRequestDTO transferDTO = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));
      String errorMsg = "Erro genérico do EJB";

      doThrow(new RuntimeException(errorMsg)).when(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         beneficioService.transferir(transferDTO);
      });

      assertEquals(errorMsg, ex.getMessage());
      verify(beneficioEjbServiceMock).transfer(1L, 2L, new BigDecimal("100.00"));
   }
}
