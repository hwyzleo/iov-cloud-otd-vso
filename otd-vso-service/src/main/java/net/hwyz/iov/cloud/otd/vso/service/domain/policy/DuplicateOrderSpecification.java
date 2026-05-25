package net.hwyz.iov.cloud.otd.vso.service.domain.policy;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DuplicateUnpaidOrderException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DuplicateOrderSpecification implements Specification<Order> {

    private final OrderRepository orderRepository;

    private String userId;
    private String mobileHash;

    public DuplicateOrderSpecification withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DuplicateOrderSpecification withMobileHash(String mobileHash) {
        this.mobileHash = mobileHash;
        return this;
    }

    @Override
    public boolean isSatisfiedBy(Order candidate) {
        if (userId != null && !userId.isEmpty()) {
            if (orderRepository.existsUnpaidOrderByUserId(userId)) {
                throw new DuplicateUnpaidOrderException(userId);
            }
        }

        if (mobileHash != null && !mobileHash.isEmpty()) {
            if (orderRepository.existsUnpaidOrderByMobileHash(mobileHash)) {
                throw new DuplicateUnpaidOrderException(mobileHash);
            }
        }

        return true;
    }

    public void check(String userId, String mobileHash) {
        this.userId = userId;
        this.mobileHash = mobileHash;
        isSatisfiedBy(null);
    }
}