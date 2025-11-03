package br.com.bip.backend;

import br.com.bip.backend.dto.BeneficioRequestDTO;
import br.com.bip.backend.dto.BeneficioResponseDTO;
import br.com.bip.backend.dto.TransferRequestDTO;
import br.com.bip.backend.service.BeneficioService;
import br.com.bip.ejb.exception.TransferenciaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeneficioController.class)
public class BeneficioControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private BeneficioService beneficioService;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private WebApplicationContext webApplicationContext;

   @Configuration
   @Import(BeneficioController.class)
   static class TestConfig {

      @ControllerAdvice
      static class TestExceptionConfiguration {
         @ExceptionHandler(EntityNotFoundException.class)
         public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
         }

         @ExceptionHandler(TransferenciaException.class)
         public ResponseEntity<Map<String, String>> handleTransferenciaException(TransferenciaException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
         }

         @ExceptionHandler(MethodArgumentNotValidException.class)
         public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getAllErrors().forEach((error) -> {
               String fieldName = ((FieldError) error).getField();
               String errorMessage = error.getDefaultMessage();
               errors.put(fieldName, errorMessage);
            });
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
         }
      }
   }

   @BeforeEach
   void setUp() {
      mockMvc = MockMvcBuilders
               .webAppContextSetup(webApplicationContext)
               .build();
   }

   private BeneficioResponseDTO createBeneficioResponseDTO(Long id, String nome) {
      BeneficioResponseDTO dto = new BeneficioResponseDTO();
      dto.setId(id);
      dto.setNome(nome);
      dto.setValor(new BigDecimal("100.00"));
      dto.setAtivo(true);
      return dto;
   }

   private BeneficioRequestDTO createBeneficioRequestDTO(String nome) {
      BeneficioRequestDTO dto = new BeneficioRequestDTO();
      dto.setNome(nome);
      dto.setDescricao("Desc");
      dto.setValor(new BigDecimal("100.00"));
      dto.setAtivo(true);
      return dto;
   }

   @Test
   void list_ShouldReturnListOfBeneficios() throws Exception {
      BeneficioResponseDTO dto = createBeneficioResponseDTO(1L, "Vale Refeição");
      when(beneficioService.findAll()).thenReturn(List.of(dto));

      mockMvc.perform(get("/api/v1/beneficios"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].nome", is("Vale Refeição")));
   }

   @Test
   void list_ShouldReturnEmptyList() throws Exception {
      when(beneficioService.findAll()).thenReturn(Collections.emptyList());

      mockMvc.perform(get("/api/v1/beneficios"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
   }

   @Test
   void getById_ShouldReturnBeneficio_WhenIdExists() throws Exception {
      BeneficioResponseDTO dto = createBeneficioResponseDTO(1L, "Vale Refeição");
      when(beneficioService.findById(1L)).thenReturn(dto);

      mockMvc.perform(get("/api/v1/beneficios/{id}", 1L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.nome", is("Vale Refeição")));
   }

   @Test
   void getById_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
      Long id = 99L;
      String errorMsg = "Benefício não encontrado com ID: " + id;
      when(beneficioService.findById(id)).thenThrow(new EntityNotFoundException(errorMsg));

      mockMvc.perform(get("/api/v1/beneficios/{id}", id))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error", is(errorMsg)));
   }

   @Test
   void create_ShouldReturnCreated_WhenDataIsValid() throws Exception {
      BeneficioRequestDTO requestDTO = createBeneficioRequestDTO("Novo Benefício");
      BeneficioResponseDTO responseDTO = createBeneficioResponseDTO(1L, "Novo Benefício");

      when(beneficioService.create(any(BeneficioRequestDTO.class))).thenReturn(responseDTO);

      mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.nome", is("Novo Benefício")));
   }

   @Test
   void create_ShouldReturnBadRequest_WhenDataIsInvalid() throws Exception {
      BeneficioRequestDTO requestDTO = createBeneficioRequestDTO(null);

      mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isBadRequest());
   }

   @Test
   void update_ShouldReturnOk_WhenDataIsValid() throws Exception {
      Long id = 1L;
      BeneficioRequestDTO requestDTO = createBeneficioRequestDTO("Nome Atualizado");
      BeneficioResponseDTO responseDTO = createBeneficioResponseDTO(id, "Nome Atualizado");

      when(beneficioService.update(eq(id), any(BeneficioRequestDTO.class))).thenReturn(responseDTO);

      mockMvc.perform(put("/api/v1/beneficios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.nome", is("Nome Atualizado")));
   }

   @Test
   void update_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
      Long id = 99L;
      BeneficioRequestDTO requestDTO = createBeneficioRequestDTO("Nome Atualizado");
      String errorMsg = "Benefício não encontrado com ID: " + id;

      when(beneficioService.update(eq(id), any(BeneficioRequestDTO.class)))
               .thenThrow(new EntityNotFoundException(errorMsg));

      mockMvc.perform(put("/api/v1/beneficios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error", is(errorMsg)));
   }

   @Test
   void update_ShouldReturnBadRequest_WhenDataIsInvalid() throws Exception {
      Long id = 1L;
      BeneficioRequestDTO requestDTO = createBeneficioRequestDTO(null);

      mockMvc.perform(put("/api/v1/beneficios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isBadRequest());
   }


   @Test
   void delete_ShouldReturnNoContent_WhenIdExists() throws Exception {
      Long id = 1L;
      doNothing().when(beneficioService).deleteById(id);

      mockMvc.perform(delete("/api/v1/beneficios/{id}", id))
               .andExpect(status().isNoContent());
   }

   @Test
   void delete_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
      Long id = 99L;
      String errorMsg = "Benefício não encontrado com ID: " + id;
      doThrow(new EntityNotFoundException(errorMsg)).when(beneficioService).deleteById(id);

      mockMvc.perform(delete("/api/v1/beneficios/{id}", id))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error", is(errorMsg)));
   }

   @Test
   void transferir_ShouldReturnOk_WhenTransferIsSuccessful() throws Exception {
      TransferRequestDTO requestDTO = new TransferRequestDTO();
      requestDTO.setIdBeneficioOrigem(1L);
      requestDTO.setIdBeneficioDestino(2L);
      requestDTO.setValor(new BigDecimal("50.00"));

      doNothing().when(beneficioService).transferir(any(TransferRequestDTO.class));

      mockMvc.perform(post("/api/v1/beneficios/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk());
   }

   @Test
   void transferir_ShouldReturnBadRequest_WhenTransferFails() throws Exception {
      TransferRequestDTO requestDTO = new TransferRequestDTO();
      requestDTO.setIdBeneficioOrigem(1L);
      requestDTO.setIdBeneficioDestino(2L);
      requestDTO.setValor(new BigDecimal("999.00"));

      String errorMsg = "Saldo insuficiente na conta de origem (ID: 1).";
      doThrow(new TransferenciaException(errorMsg))
               .when(beneficioService).transferir(any(TransferRequestDTO.class));

      mockMvc.perform(post("/api/v1/beneficios/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error", is(errorMsg)));
   }

   @Test
   void transferir_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
      TransferRequestDTO requestDTO = new TransferRequestDTO();
      requestDTO.setIdBeneficioOrigem(null);
      requestDTO.setIdBeneficioDestino(2L);
      requestDTO.setValor(new BigDecimal("50.00"));

      mockMvc.perform(post("/api/v1/beneficios/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isBadRequest());
   }
}

