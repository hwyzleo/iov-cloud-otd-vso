package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.domain.AbstractRepository;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.factory.OrderFactory;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderModelConfigDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.assembler.OrderModelConfigPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.assembler.OrderPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 车辆销售订单领域仓库接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl extends AbstractRepository<String, OrderDo> implements OrderRepository {

    private final OrderDao orderDao;
    private final OrderFactory orderFactory;
    private final OrderModelConfigDao orderModelConfigDao;

    @Override
    public OrderDo get(String orderNum) {
        List<OrderPo> orderPoList = orderDao.selectPoByExample(OrderPo.builder().orderNum(orderNum).build());
        if (orderPoList.isEmpty()) {
            return null;
        }
        OrderPo orderPo = orderPoList.get(0);
        List<OrderModelConfigPo> orderModelConfigPoList = orderModelConfigDao.selectPoByExample(OrderModelConfigPo.builder().orderNum(orderNum).build());
        Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap = new HashMap<>();
        orderModelConfigPoList.forEach(orderModelConfigPo -> {
            OrderModelConfigDo orderModelConfigDo = OrderModelConfigPoAssembler.INSTANCE.toDo(orderModelConfigPo);
            modelConfigMap.put(SaleModelConfigType.valOf(orderModelConfigPo.getType()), orderModelConfigDo);
        });
        OrderDo orderDo = OrderDo.builder()
                .id(orderPo.getId())
                .orderNum(orderPo.getOrderNum())
                .orderState(OrderState.valOf(orderPo.getOrderState()))
                .orderStateTime(orderPo.getOrderStateTime())
                .orderTime(orderPo.getOrderTime())
                .orderPersonId(orderPo.getOrderPersonId())
                .orderPersonType(orderPo.getOrderPersonType())
                .orderPersonName(orderPo.getOrderPersonName())
                .orderPersonIdType(orderPo.getOrderPersonIdType())
                .orderPersonIdNum(orderPo.getOrderPersonIdNum())
                .purchasePlan(orderPo.getPurchasePlan())
                .licenseCity(orderPo.getLicenseCity())
                .dealership(orderPo.getDealership())
                .deliveryCenter(orderPo.getDeliveryCenter())
                .saleCode(orderPo.getSaleCode())
                .modelConfigCode(orderPo.getModelConfigCode())
                .modelConfigLock(orderPo.getModelConfigLock())
                .modelConfigMap(modelConfigMap)
                .build();
        orderDo.stateLoad();
        return orderDo;
    }

    @Override
    public OrderDo get(String orderPersonId, String orderNum) {
        OrderDo orderDo = get(orderNum);
        if (orderDo != null && orderDo.getOrderPersonId().equals(orderPersonId)) {
            return orderDo;
        }
        return null;
    }

    @Override
    public Optional<OrderDo> getById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean save(OrderDo orderDo) {
        switch (orderDo.getState()) {
            case NEW -> {
                OrderPo orderPo = OrderPoAssembler.INSTANCE.fromDo(orderDo);
                orderDao.insertPo(orderPo);
                List<OrderModelConfigPo> orderModelConfigPoList = new ArrayList<>();
                orderDo.getModelConfigMap().values().forEach(orderModelConfigDo -> {
                    OrderModelConfigPo orderModelConfigPo = OrderModelConfigPoAssembler.INSTANCE.fromDo(orderModelConfigDo);
                    orderModelConfigPo.setOrderNum(orderDo.getOrderNum());
                    orderModelConfigPoList.add(orderModelConfigPo);
                });
                orderModelConfigDao.batchInsertPo(orderModelConfigPoList);
            }
            case CHANGED -> {
                orderDao.updatePo(OrderPoAssembler.INSTANCE.fromDo(orderDo));
                if (!orderDo.getModelConfigLock()) {
                    orderModelConfigDao.batchPhysicalDeletePoByOrderNum(orderDo.getOrderNum());
                    List<OrderModelConfigPo> orderModelConfigPoList = new ArrayList<>();
                    orderDo.getModelConfigMap().values().forEach(orderModelConfigDo -> {
                        OrderModelConfigPo orderModelConfigPo = OrderModelConfigPoAssembler.INSTANCE.fromDo(orderModelConfigDo);
                        orderModelConfigPo.setOrderNum(orderDo.getOrderNum());
                        orderModelConfigPoList.add(orderModelConfigPo);
                    });
                    orderModelConfigDao.batchInsertPo(orderModelConfigPoList);
                }
            }
            case DELETED -> {
                if (orderDo.getOrderState() == OrderState.WISHLIST) {
                    orderModelConfigDao.batchPhysicalDeletePoByOrderNum(orderDo.getOrderNum());
                    orderDao.physicalDeletePoByOrderNum(orderDo.getOrderNum());
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
