package application.domain.ports.in;

import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;

import java.util.List;

/** Input port: generates the refund of an approved return. */
public interface GenerateRefundUseCase {

    Refund generate(ReturnRequest returnRequest, List<Refund> existing);
}