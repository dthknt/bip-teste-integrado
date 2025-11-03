package br.com.bip.backend.controller;

import br.com.bip.backend.dto.BeneficioRequestDTO;
import br.com.bip.backend.dto.BeneficioResponseDTO;
import br.com.bip.backend.dto.TransferRequestDTO;
import br.com.bip.backend.service.BeneficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService beneficioService;

    public BeneficioController(BeneficioService beneficioService) {
        this.beneficioService = beneficioService;
    }

    @Operation(summary = "Lista todos os benefícios", description = "Retorna uma lista com todos os benefícios cadastrados.")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Lista de benefícios retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<BeneficioResponseDTO>> list() {
        List<BeneficioResponseDTO> beneficios = beneficioService.findAll();
        return ResponseEntity.ok(beneficios);
    }

    @Operation(summary = "Busca um benefício por ID", description = "Retorna um benefício específico baseado no seu ID.")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Benefício encontrado"),
             @ApiResponse(responseCode = "500", description = "Benefício não encontrado com o ID informado", content = @Content),
             @ApiResponse(responseCode = "404", description = "Benefício não encontrado com o ID informado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BeneficioResponseDTO> getById(
             @Parameter(description = "ID do benefício a ser buscado", required = true, example = "1")
             @PathVariable Long id) {
        BeneficioResponseDTO beneficio = beneficioService.findById(id);
        return ResponseEntity.ok(beneficio);
    }

    @Operation(summary = "Cria um novo benefício", description = "Cadastra um novo benefício no sistema.")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "201", description = "Benefício criado com sucesso",
                      content = @Content(schema = @Schema(implementation = BeneficioResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<BeneficioResponseDTO> create(
             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                      description = "Dados do benefício para criação",
                      required = true,
                      content = @Content(schema = @Schema(implementation = BeneficioRequestDTO.class))
             )
             @Valid @RequestBody BeneficioRequestDTO requestDTO) {
        BeneficioResponseDTO novoBeneficio = beneficioService.create(requestDTO);
        return new ResponseEntity<>(novoBeneficio, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza um benefício existente", description = "Atualiza os dados de um benefício baseado no seu ID.")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Benefício atualizado com sucesso"),
             @ApiResponse(responseCode = "404", description = "Benefício não encontrado com o ID informado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BeneficioResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BeneficioRequestDTO requestDTO) {
        BeneficioResponseDTO beneficioAtualizado = beneficioService.update(id, requestDTO);
        return ResponseEntity.ok(beneficioAtualizado);
    }

    @Operation(summary = "Exclui um benefício por ID", description = "Remove um benefício do sistema baseado no seu ID")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "204", description = "Benefício deletado com sucesso", content = @Content),
             @ApiResponse(responseCode = "404", description = "Benefício não encontrado com o ID informado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
             @Parameter(description = "ID do benefício a ser deletado", required = true, example = "1")
             @PathVariable Long id) {
        beneficioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transfere valores entre benefícios", description = "Realiza a transferência de saldo de um benefício para outro")
    @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
             @ApiResponse(responseCode = "400", description = "Erro na validação da transferência (ex: saldo insuficiente)", content = @Content)
    })
    @PostMapping("/transferir")
    public ResponseEntity<Void> transferir(
             @io.swagger.v3.oas.annotations.parameters.RequestBody(
                      description = "Dados da transferência (ID de origem, ID de destino, valor do saldo para transferir)",
                      required = true,
                      content = @Content(schema = @Schema(implementation = TransferRequestDTO.class))
             )
             @Valid @RequestBody TransferRequestDTO requestDTO) {
        beneficioService.transferir(requestDTO);
        return ResponseEntity.ok().build();
    }
}
