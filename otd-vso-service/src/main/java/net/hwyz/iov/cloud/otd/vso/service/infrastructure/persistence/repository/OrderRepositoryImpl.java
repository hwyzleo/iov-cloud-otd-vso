package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.OrderPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderAmountRepository orderAmountRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order save(Order order) {
        OrderPo orderPo = OrderPoConverter.INSTANCE.fromDomain(order);
        OrderPo existingPo = orderMapper.selectByOrderId(order.getId());
        if (existingPo == null) {
            orderMapper.insertPo(orderPo);
        } else {
            orderPo.setId(existingPo.getId());
            orderMapper.updatePo(orderPo);
        }
        if (order.getOrderAmount() != null) {
            saveOrderAmount(order.getOrderAmount(), order.getId());
        }
        return OrderPoConverter.INSTANCE.toDomain(orderPo);
    }

    private void saveOrderAmount(OrderAmount orderAmount, String orderId) {
        OrderAmountPo po = convertAmountToPo(orderAmount, orderId);
        orderAmountRepository.findByOrderId(orderId)
                .ifPresent(existing -> po.setId(existing.getId()));
        orderAmountRepository.save(po);
    }

    private OrderAmountPo convertAmountToPo(OrderAmount orderAmount, String orderId) {
        OrderAmountPo po = new OrderAmountPo();
        po.setAmountId(orderAmount.getAmountId());
        po.setOrderId(orderId);
        po.setGuidePrice(orderAmount.getGuidePrice().getAmount());
        po.setVehiclePrice(orderAmount.getVehiclePrice().getAmount());
        po.setOptionPrice(orderAmount.getOptionPrice().getAmount());
        po.setColorMarkup(orderAmount.getColorMarkup().getAmount());
        po.setServiceFee(orderAmount.getServiceFee().getAmount());
        po.setPlateServiceFee(orderAmount.getPlateServiceFee().getAmount());
        po.setInsuranceFee(orderAmount.getInsuranceFee().getAmount());
        po.setDiscountTotal(orderAmount.getDiscountTotal().getAmount());
        po.setSubsidyTotal(orderAmount.getSubsidyTotal().getAmount());
        po.setFinanceDiscountTotal(orderAmount.getFinanceDiscountTotal().getAmount());
        po.setDealPriceTotal(orderAmount.getDealPriceTotal().getAmount());
        po.setDepositAmount(orderAmount.getDepositAmount().getAmount());
        po.setDownPaymentAmount(orderAmount.getDownPaymentAmount().getAmount());
        po.setTailPaymentAmount(orderAmount.getTailPaymentAmount().getAmount());
        po.setPaidTotal(orderAmount.getPaidTotal().getAmount());
        po.setRefundTotal(orderAmount.getRefundTotal().getAmount());
        po.setReceivableTotal(orderAmount.getReceivableTotal().getAmount());
        po.setNetReceivableTotal(orderAmount.getNetReceivableTotal().getAmount());
        po.setUnpaidTotal(orderAmount.getUnpaidTotal().getAmount());
        po.setInvoiceAmount(orderAmount.getInvoiceAmount().getAmount());
        po.setCalculationVersion(orderAmount.getCalculationVersion());
        po.setRowValid(1);
        return po;
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return Optional.ofNullable(orderMapper.selectByOrderId(orderId))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orderMapper.selectByOrderNo(orderNo))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOrderNo(String orderNo) {
        orderMapper.logicalDeletePoByOrderNo(orderNo);
    }

    @Override
    public List<Order> search(String orderNo, OrderState orderState, List<OrderState> orderStateRange,
                                Boolean hasDeliveryPerson, Date beginTime, Date endTime) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        
        if (orderNo != null && !orderNo.isEmpty()) {
            params.put("orderNo", orderNo);
        }
        
        if (orderState != null) {
            params.put("orderState", orderState.getValue());
        }
        
        if (orderStateRange != null && !orderStateRange.isEmpty()) {
            params.put("orderStateRange", orderStateRange.stream()
                    .map(OrderState::getValue)
                    .toList());
        }
        
        if (hasDeliveryPerson != null) {
            params.put("hasDeliveryPerson", hasDeliveryPerson);
        }
        
        if (beginTime != null) {
            params.put("beginTime", beginTime);
        }
        
        if (endTime != null) {
            params.put("endTime", endTime);
        }
        
        return orderMapper.selectPoByMap(params).stream()
                .map(OrderPoConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Integer count(String deliveryPersonId, Boolean delivered) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        if (deliveryPersonId != null && !deliveryPersonId.isEmpty()) {
            params.put("deliveryPersonId", deliveryPersonId);
        }
        if (delivered != null) {
            params.put("delivered", delivered);
        }
        
        return orderMapper.countPoByMap(params);
    }

    @Override
    public Optional<Order> findByOrderNoAndAccountId(String orderNo, String accountId) {
        return Optional.ofNullable(orderMapper.selectByOrderNoAndAccountId(orderNo, accountId))
                .map(OrderPoConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Order> findByAccountId(String accountId, String type) {
        throw new UnsupportedOperationException("待通过 vso_order_party 表关联查询实现");
    }

    @Override
    public boolean existsUnpaidOrderByUserId(String userId) {
        return orderMapper.existsUnpaidOrderByUserId(userId) > 0;
    }

    @Override
    public boolean existsUnpaidOrderByMobileHash(String mobileHash) {
        return orderMapper.existsUnpaidOrderByMobileHash(mobileHash) > 0;
    }

    @Override
    public boolean existsActiveOrdersBySaleModelCode(String saleModelCode) {
        if (saleModelCode == null || saleModelCode.isEmpty()) {
            return false;
        }
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("saleModel", saleModelCode);
        params.put("orderStateRange", java.util.List.of(
            net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.EARNEST_MONEY_PAID.getValue(),
            net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.DOWN_PAYMENT_UNPAID.getValue(),
            net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.DOWN_PAYMENT_PAID.getValue(),
            net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.ARRANGE_PRODUCTION.getValue(),
            net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState.ALLOCATION_VEHICLE.getValue()));
        return orderMapper.countPoByMap(params) > 0;
    }

}
