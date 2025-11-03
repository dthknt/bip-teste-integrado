package br.com.bip.ejb;

import br.com.bip.ejb.entity.Beneficio;
import br.com.bip.ejb.exception.TransferenciaException;
import br.com.bip.ejb.validation.TransferenciaValidator;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class BeneficioEjbService implements BeneficioEjbServiceLocal{

    @PersistenceContext(unitName = "bip-pu")
    private EntityManager em;

    @EJB
    private TransferenciaValidator validator;

    /**
     * Corrigido bug de transferência aplicando:
     * 1. Validações de Negócio (valor positivo, saldo suficiente, contas diferentes).
     * 2. Locking Pessimista (PESSIMISTIC_WRITE) para garantir consistência
     * 3. Tratamento de exceção customizada para garantir o rollback.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {

        try {
            // --- 1. Aquisição com Lock Pessimista ---
            Beneficio from = em.find(Beneficio.class, fromId, LockModeType.PESSIMISTIC_WRITE);
            Beneficio to = em.find(Beneficio.class, toId, LockModeType.PESSIMISTIC_WRITE);

            // --- 2. Validação ---
            validator.validar(fromId, toId, amount, from, to);

            // --- 3. Execução da Lógica de Negócio ---
            from.setValor(from.getValor().subtract(amount));
            to.setValor(to.getValor().add(amount));

            em.merge(from);
            em.merge(to);

        } catch (Exception e) {
            if (e instanceof TransferenciaException) {
                throw e;
            }
            // Encapsula outras exceções de persistência
            throw new TransferenciaException("Erro inesperado durante a transferência: " + e.getMessage(), e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Beneficio> findAll() {
        return em.createQuery("SELECT b FROM Beneficio b", Beneficio.class)
                 .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Beneficio findById(Long id) {
        return em.find(Beneficio.class, id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Beneficio create(Beneficio beneficio) {
        em.persist(beneficio);
        return beneficio;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Beneficio update(Beneficio beneficio) {
        return em.merge(beneficio);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteById(Long id) {
        Beneficio beneficio = findById(id);
        if (beneficio != null) {
            em.remove(beneficio);
        }
    }
}
