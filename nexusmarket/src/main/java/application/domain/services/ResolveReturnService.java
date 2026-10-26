package application.domain.services;

import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;

public class ResolveReturnService {

    private final AuthorizationService authorization;

    public ResolveReturnService(AuthorizationService authorization) {
        if (authorization == null)
            throw new IllegalArgumentException("Authorization service is required.");
        this.authorization = authorization;
    }

    public void approve(User actor, ReturnRequest returnRequest) {
        validate(actor, returnRequest);
        returnRequest.approve();
    }

    public void reject(User actor, ReturnRequest returnRequest) {
        validate(actor, returnRequest);
        returnRequest.reject();
    }

    private void validate(User actor, ReturnRequest returnRequest) {
        authorization.requirePermission(actor, BusinessProcess.RETURN_MANAGEMENT);
        // The buyer requests, but cannot resolve their own return.
        if (!actor.hasRole(UserRole.ADMIN))
            throw new AccessDeniedException("Only an admin can resolve returns.");
        if (returnRequest == null)
            throw new IllegalArgumentException("Return is required.");
    }
}