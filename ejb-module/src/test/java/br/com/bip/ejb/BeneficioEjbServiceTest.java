package br.com.bip.ejb;

import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import br.com.bip.ejb.validation.TransferenciaValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
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
class BeneficioEjbServiceTest {

   @Mock
   private EntityManager em;

   @Mock
   private TransferenciaValidator validator;

   @Mock
   private TypedQuery<Beneficio> typedQueryMock;

   private BeneficioEjbService beneficioEjbService;

   private Beneficio beneficioOrigem;
   private Beneficio beneficioDestino;

   @BeforeEach
   void setUp() {
      beneficioEjbService = new BeneficioEjbService();

      // Injeção manual dos mocks (simulando @PersistenceContext e @EJB)
      try {
         var emField = BeneficioEjbService.class.getDeclaredField("em");
         emField.setAccessible(true);
         emField.set(beneficioEjbService, em);

         var validatorField = BeneficioEjbService.class.getDeclaredField("validator");
         validatorField.setAccessible(true);
         validatorField.set(beneficioEjbService, validator);
      } catch (Exception e) {
         fail("Falha ao injetar mocks via reflection", e);
      }

      beneficioOrigem = new Beneficio();
      beneficioOrigem.setId(1L);
      beneficioOrigem.setNome("Origem");
      beneficioOrigem.setValor(new BigDecimal("1000.00"));

      beneficioDestino = new Beneficio();
      beneficioDestino.setId(2L);
      beneficioDestino.setNome("Destino");
      beneficioDestino.setValor(new BigDecimal("500.00"));
   }

   // --- Testes de Transferência ---
   @Test
   void transfer_ShouldSucceed_WhenValid() {
      when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioOrigem);
      when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioDestino);

      BigDecimal valor = new BigDecimal("100.00");

      beneficioEjbService.transfer(1L, 2L, valor);

      verify(validator).validar(1L, 2L, valor, beneficioOrigem, beneficioDestino);

      assertEquals(new BigDecimal("900.00"), beneficioOrigem.getValor());
      assertEquals(new BigDecimal("600.00"), beneficioDestino.getValor());

      verify(em).merge(beneficioOrigem);
      verify(em).merge(beneficioDestino);
   }

   @Test
   void transfer_ShouldThrowTransferenciaException_WhenValidatorFails() {
      when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioOrigem);
      when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioDestino);

      BigDecimal valor = new BigDecimal("2000.00"); // Saldo insuficiente
      String errorMsg = "Saldo insuficiente na conta de origem (ID: 1).";

      doThrow(new TransferenciaException(errorMsg))
               .when(validator).validar(1L, 2L, valor, beneficioOrigem, beneficioDestino);

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         beneficioEjbService.transfer(1L, 2L, valor);
      });

      assertEquals(errorMsg, ex.getMessage());
      assertEquals(new BigDecimal("1000.00"), beneficioOrigem.getValor());
      verify(em, never()).merge(any());
   }

   @Test
   void transfer_ShouldThrowTransferenciaException_WhenAccountNotFound() {
      when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null); // Origem não existe
      when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioDestino);

      BigDecimal valor = new BigDecimal("100.00");
      String errorMsg = "Conta de origem não encontrada (ID: 1).";

      doThrow(new TransferenciaException(errorMsg))
               .when(validator).validar(1L, 2L, valor, null, beneficioDestino);

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         beneficioEjbService.transfer(1L, 2L, valor);
      });

      assertEquals(errorMsg, ex.getMessage());
      verify(em, never()).merge(any());
   }

   @Test
   void transfer_ShouldWrapGenericException_WhenMergeFails() {
      when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioOrigem);
      when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(beneficioDestino);

      BigDecimal valor = new BigDecimal("100.00");
      String errorMsg = "Erro de persistência";

      when(em.merge(beneficioOrigem)).thenThrow(new RuntimeException(errorMsg));

      TransferenciaException ex = assertThrows(TransferenciaException.class, () -> {
         beneficioEjbService.transfer(1L, 2L, valor);
      });

      assertTrue(ex.getMessage().contains(errorMsg));
      assertEquals(new BigDecimal("900.00"), beneficioOrigem.getValor());
   }

   // --- Testes CRUD ---

   @Test
   void findAll_ShouldReturnList() {
      when(em.createQuery("SELECT b FROM Beneficio b", Beneficio.class)).thenReturn(typedQueryMock);
      when(typedQueryMock.getResultList()).thenReturn(List.of(beneficioOrigem));

      List<Beneficio> result = beneficioEjbService.findAll();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(em).createQuery("SELECT b FROM Beneficio b", Beneficio.class);
   }

   @Test
   void findById_ShouldReturnBeneficio_WhenExists() {
      when(em.find(Beneficio.class, 1L)).thenReturn(beneficioOrigem);

      Beneficio result = beneficioEjbService.findById(1L);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      verify(em).find(Beneficio.class, 1L);
   }

   @Test
   void findById_ShouldReturnNull_WhenNotExists() {
      when(em.find(Beneficio.class, 99L)).thenReturn(null);

      Beneficio result = beneficioEjbService.findById(99L);

      assertNull(result);
      verify(em).find(Beneficio.class, 99L);
   }

   @Test
   void create_ShouldPersistEntity() {
      ArgumentCaptor<Beneficio> captor = ArgumentCaptor.forClass(Beneficio.class);

      Beneficio novo = new Beneficio();
      novo.setNome("Novo Beneficio");

      beneficioEjbService.create(novo);

      verify(em).persist(captor.capture());
      assertEquals("Novo Beneficio", captor.getValue().getNome());
   }

   @Test
   void update_ShouldMergeEntity() {
      when(em.merge(beneficioOrigem)).thenReturn(beneficioOrigem);

      beneficioEjbService.update(beneficioOrigem);

      verify(em).merge(beneficioOrigem);
   }

   @Test
   void deleteById_ShouldRemoveEntity_WhenExists() {
      when(em.find(Beneficio.class, 1L)).thenReturn(beneficioOrigem);

      beneficioEjbService.deleteById(1L);

      verify(em).find(Beneficio.class, 1L);
      verify(em).remove(beneficioOrigem);
   }

   @Test
   void deleteById_ShouldDoNothing_WhenNotExists() {
      when(em.find(Beneficio.class, 99L)).thenReturn(null);

      beneficioEjbService.deleteById(99L);

      verify(em).find(Beneficio.class, 99L);
      verify(em, never()).remove(any());
   }
}
