package application.adapters.useCases;

import application.domain.ports.in.AdminPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.LogisticOperatorPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.in.SupervisorPort;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePortsTest {

    private static final Map<Class<?>, Class<?>> PORTS = Map.of(
            PublicAccessPort.class, PublicAccessUseCaseImpl.class,
            BuyerPort.class, BuyerUseCaseImpl.class,
            SellerPort.class, SellerUseCaseImpl.class,
            LogisticOperatorPort.class, LogisticOperatorUseCaseImpl.class,
            AdminPort.class, AdminUseCaseImpl.class,
            SupervisorPort.class, SupervisorUseCaseImpl.class);

    @Test
    void eachRoleHasOnePortAndOneUseCase() {
        assertEquals(6, PORTS.size());
        PORTS.forEach((port, useCase) -> {
            assertTrue(port.isInterface());
            assertTrue(port.isAssignableFrom(useCase));
        });
    }

    @Test
    void portsOnlyUseDomainTypes() {
        PORTS.keySet().forEach(port -> {
            for (Method method : port.getDeclaredMethods()) {
                assertTrue(Modifier.isAbstract(method.getModifiers()));
                for (Class<?> type : method.getParameterTypes())
                    assertFalse(type.getName().contains("adapters"));
            }
        });
    }
}