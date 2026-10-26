package application.domain.ports.in;

import application.domain.entities.ReturnRequest;
import application.domain.entities.User;

/** Input port: the admin approves or rejects a return. */
public interface ResolveReturnUseCase {

    void approve(User actor, ReturnRequest returnRequest);

    void reject(User actor, ReturnRequest returnRequest);
}