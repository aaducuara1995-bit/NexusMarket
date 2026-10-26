package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Order;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;

import java.util.List;

/** Input port: the buyer requests the return of a delivered order. */
public interface RequestReturnUseCase {

    ReturnRequest request(User user, Buyer buyer, Order order,
                        String reason, List<ReturnRequest> existing);
}