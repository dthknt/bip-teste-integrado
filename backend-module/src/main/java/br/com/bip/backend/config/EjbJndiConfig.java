package br.com.bip.backend.config;

import br.com.bip.ejb.BeneficioEjbServiceLocal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

@Configuration
public class EjbJndiConfig {

   @Bean
   public JndiObjectFactoryBean beneficioEjbService() {
      JndiObjectFactoryBean jndi = new JndiObjectFactoryBean();
      jndi.setJndiName(
               "java:global/ear-module-1.0.0/br.com.bip-ejb-module-1.0.0/BeneficioEjbService!br.com.bip.ejb.BeneficioEjbServiceLocal"
      );
      jndi.setProxyInterface(BeneficioEjbServiceLocal.class);
      jndi.setLookupOnStartup(false); // inicialização lazy para evitar falha
      return jndi;
   }
}
