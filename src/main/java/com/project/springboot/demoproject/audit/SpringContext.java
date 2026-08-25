package com.project.springboot.demoproject.audit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Los EntityListener de JPA (@EntityListeners) son instanciados por Hibernate,
 * no por Spring, por lo que no se les puede inyectar dependencias con @Autowired.
 * Este componente guarda una referencia estatica al ApplicationContext para que
 * AuditoriaEntityListener pueda obtener los beans que necesita (repositorios, etc).
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            return null;
        }
        return context.getBean(beanClass);
    }
}
