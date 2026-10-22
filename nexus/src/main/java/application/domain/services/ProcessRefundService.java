package application.domain.services;

import application.domain.entities.Refund;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.ports.in.ProcessRefundUseCase;

public class ProcessRefundService implements ProcessRefundUseCase {

    private final AuthorizationService authorization;

    public ProcessRefundService(AuthorizationService authorization) {
        if (authorization == null)
            throw new IllegalArgumentException("Authorization service is required.");
        this.authorization = authorization;
    }

    public void process(User actor, Refund refund) {
        authorization.requirePermission(actor, BusinessProcess.REFUND_MANAGEMENT);
        // The buyer takes part in the refund, but the admin is the one who executes it.
        if (!actor.hasRole(UserRole.ADMIN))
            throw new AccessDeniedException("Only an admin can process refunds.");
        if (refund == null)
            throw new IllegalArgumentException("Refund is required.");

        refund.process();
    }
}