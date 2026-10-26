package application.domain.ports.in;

import application.domain.entities.Refund;
import application.domain.entities.User;

/** Input port: the admin processes a pending refund. */
public interface ProcessRefundUseCase {

    void process(User actor, Refund refund);
}