package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AuthorizationService {

    private static final Map<BusinessProcess, Set<UserRole>> PERMISSIONS =
            new EnumMap<>(BusinessProcess.class);

    static {
        // Responsibility matrix (section 12 of the functional specification)
        PERMISSIONS.put(BusinessProcess.SELLER_REGISTRATION,
                EnumSet.of(UserRole.ADMIN));
        PERMISSIONS.put(BusinessProcess.PRODUCT_REGISTRATION,
                EnumSet.of(UserRole.SELLER));
        PERMISSIONS.put(BusinessProcess.INVENTORY_MANAGEMENT,
                EnumSet.of(UserRole.SELLER, UserRole.LOGISTIC_OPERATOR));
        PERMISSIONS.put(BusinessProcess.ORDER_MANAGEMENT,
                EnumSet.of(UserRole.BUYER, UserRole.SELLER, UserRole.LOGISTIC_OPERATOR));
        PERMISSIONS.put(BusinessProcess.REFUND_MANAGEMENT,
                EnumSet.of(UserRole.BUYER, UserRole.ADMIN));

        // Described in the participants table (section 5)
        PERMISSIONS.put(BusinessProcess.WAREHOUSE_MANAGEMENT,
                EnumSet.of(UserRole.ADMIN));
        PERMISSIONS.put(BusinessProcess.SHIPPING_OPERATIONS,
                EnumSet.of(UserRole.LOGISTIC_OPERATOR));

        // Assumption: handled like refunds (the specification does not define it)
        PERMISSIONS.put(BusinessProcess.RETURN_MANAGEMENT,
                EnumSet.of(UserRole.BUYER, UserRole.ADMIN));
    }

    public boolean canExecute(User user, BusinessProcess process) {
        validateInputs(user, process);
        return user.isActive() && PERMISSIONS.get(process).contains(user.getRole());
    }

    public void requirePermission(User user, BusinessProcess process) {
        validateInputs(user, process);

        if (!user.isActive())
            throw new AccessDeniedException(
                    "The user is blocked and cannot perform operations.");

        if (!PERMISSIONS.get(process).contains(user.getRole()))
            throw new AccessDeniedException(
                    "Role " + user.getRole() + " cannot execute " + process + ".");
    }

    private static void validateInputs(User user, BusinessProcess process) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (process == null)
            throw new IllegalArgumentException("Process is required.");
    }
}