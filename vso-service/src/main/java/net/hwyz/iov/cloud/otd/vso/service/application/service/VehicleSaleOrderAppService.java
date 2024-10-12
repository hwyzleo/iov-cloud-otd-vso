package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.Wishlist;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.external.service.ExVehicleModelConfigService;
import net.hwyz.iov.cloud.otd.vso.service.domain.factory.OrderFactory;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderModelConfigDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.ModelConfigCodeNoteExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelConfigTypeCodeNoteExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.tsp.framework.commons.enums.Symbol;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆销售订单相关应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSaleOrderAppService {

    private final OrderDao orderDao;
    private final OrderFactory orderFactory;
    private final OrderRepository orderRepository;
    private final OrderModelConfigDao orderModelConfigDao;
    private final SaleModelAppService saleModelAppService;
    private final ExVehicleModelConfigService exVehicleModelConfigService;

    /**
     * 获取订单列表
     *
     * @param accountId 账号ID
     * @return 订单列表
     */
    public List<Order> getOrderList(String accountId) {
        List<Order> list = new ArrayList<>();
        orderDao.selectPoByExample(OrderPo.builder().orderPersonId(accountId).build()).forEach(orderPo -> {
            String displayName = "";
            OrderState orderState = OrderState.valOf(orderPo.getOrderState());
            switch (orderState) {
                case WISHLIST -> {
                    List<OrderModelConfigPo> orderModelConfigPoList = orderModelConfigDao.selectPoByExample(OrderModelConfigPo.builder()
                            .orderNum(orderPo.getOrderNum())
                            .type(SaleModelConfigType.MODEL.name())
                            .build());
                    if (!orderModelConfigPoList.isEmpty()) {
                        displayName = orderModelConfigPoList.get(0).getTypeName();
                    } else {
                        displayName = orderPo.getModelConfigCode();
                    }
                }
                default -> {
                    displayName = orderPo.getOrderNum();
                }
            }
            list.add(Order.builder()
                    .orderNum(orderPo.getOrderNum())
                    .orderState(orderState.value)
                    .displayName(displayName)
                    .build());
        });
        return list;
    }

    /**
     * 创建用户心愿单
     *
     * @param accountId 账号ID
     * @param wishlist  心愿单
     * @return 订单编号
     */
    public String createUserWishlist(String accountId, Wishlist wishlist) {
        String saleCode = wishlist.getSaleCode();
        String modelConfigCode = getModelConfigCode(wishlist.getSaleModelConfigType());
        OrderDo orderDo = orderFactory.buildFromWishlist(accountId, saleCode);
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(saleCode, wishlist.getSaleModelConfigType()));
        orderRepository.save(orderDo);
        return orderDo.getOrderNum();
    }

    /**
     * 修改用户心愿单
     *
     * @param accountId 账号ID
     * @param wishlist  心愿单
     */
    public void modifyUserWishlist(String accountId, Wishlist wishlist) {
        OrderDo orderDo = orderRepository.get(accountId, wishlist.getOrderNum());
        String modelConfigCode = getModelConfigCode(wishlist.getSaleModelConfigType());
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(wishlist.getSaleCode(), wishlist.getSaleModelConfigType()));
        orderRepository.save(orderDo);
    }

    /**
     * 转换订单车型配置Map
     *
     * @param saleCode               销售编号
     * @param saleModelConfigTypeMap 销售车型配置类型Map
     * @return 订单车型配置Map
     */
    private Map<SaleModelConfigType, OrderModelConfigDo> getOrderModelConfigMap(String saleCode, Map<String, String> saleModelConfigTypeMap) {
        Map<SaleModelConfigType, OrderModelConfigDo> orderModelConfigMap = new HashMap<>();
        Map<String, SaleModelConfigPo> saleModelConfigMap = saleModelAppService.getSaleModelConfigMap(saleCode);
        saleModelConfigTypeMap.forEach((key, value) -> {
            SaleModelConfigType saleModelConfigType = SaleModelConfigType.valOf(key);
            if (saleModelConfigType == null) {
                throw new SaleModelConfigTypeCodeNoteExistException(saleCode, key, value);
            }
            SaleModelConfigPo saleModelConfigPo = saleModelConfigMap.get(key + Symbol.UNDERSCORE.value + value);
            if (saleModelConfigPo == null) {
                throw new SaleModelConfigTypeCodeNoteExistException(saleCode, key, value);
            }
            OrderModelConfigDo orderModelConfigDo = OrderModelConfigDo.builder()
                    .type(saleModelConfigType)
                    .typeCode(saleModelConfigPo.getTypeCode())
                    .typeName(saleModelConfigPo.getTypeName())
                    .typePrice(saleModelConfigPo.getTypePrice())
                    .build();
            orderModelConfigDo.init();
            orderModelConfigMap.put(saleModelConfigType, orderModelConfigDo);
        });
        return orderModelConfigMap;
    }

    /**
     * 获取车型配置代码
     *
     * @param saleModelConfigType 销售车型配置类型
     * @return 车型配置代码
     */
    private String getModelConfigCode(Map<String, String> saleModelConfigType) {
        String modelCode = saleModelConfigType.get(SaleModelConfigType.MODEL.name());
        String exteriorCode = saleModelConfigType.get(SaleModelConfigType.EXTERIOR.name());
        String interiorCode = saleModelConfigType.get(SaleModelConfigType.INTERIOR.name());
        String wheelCode = saleModelConfigType.get(SaleModelConfigType.WHEEL.name());
        String spareTireCode = saleModelConfigType.get(SaleModelConfigType.SPARE_TIRE.name());
        String adasCode = saleModelConfigType.get(SaleModelConfigType.OPTIONAL.name());
        String vehicleModeConfigCode = exVehicleModelConfigService.getVehicleModeConfigCode(modelCode, exteriorCode,
                interiorCode, wheelCode, spareTireCode, adasCode);
        if (vehicleModeConfigCode == null) {
            throw new ModelConfigCodeNoteExistException(modelCode, exteriorCode, interiorCode, wheelCode, spareTireCode, adasCode);
        }
        return vehicleModeConfigCode;
    }

    /**
     * 删除用户心愿单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void deleteUserWishlist(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        orderDo.markDelete();
        orderRepository.save(orderDo);
    }

    /**
     * 获取用户心愿单详情
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     * @return 用户心愿单详情
     */
    public WishlistResponse getUserWishlistResponse(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            return null;
        }
        Map<String, String> saleModelType = new HashMap<>();
        Map<String, String> saleModelName = new HashMap<>();
        Map<String, BigDecimal> saleModelPrice = new HashMap<>();
        boolean isValid = true;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<String, SaleModelConfigPo> saleModelMap = saleModelAppService.getSaleModelConfigMap(orderDo.getSaleCode());
        for (OrderModelConfigDo orderModelConfigDo : orderDo.getModelConfigMap().values()) {
            saleModelType.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeCode());
            saleModelName.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeName());
            saleModelPrice.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypePrice());
            totalPrice = totalPrice.add(orderModelConfigDo.getTypePrice());
            if (isValid) {
                SaleModelConfigPo saleModelConfigPo = saleModelMap.get(orderModelConfigDo.getType().name() +
                        Symbol.UNDERSCORE.value + orderModelConfigDo.getTypeCode());
                if (saleModelConfigPo == null) {
                    isValid = false;
                    continue;
                }
                if (!saleModelConfigPo.getTypeName().equals(orderModelConfigDo.getTypeName())) {
                    isValid = false;
                    continue;
                }
                if (saleModelConfigPo.getTypePrice().compareTo(orderModelConfigDo.getTypePrice()) != 0) {
                    isValid = false;
                }
            }
        }
        return WishlistResponse.builder()
                .saleCode(orderDo.getSaleCode())
                .orderNum(orderDo.getOrderNum())
                .saleModelConfigType(saleModelType)
                .saleModelConfigName(saleModelName)
                .saleModelConfigPrice(saleModelPrice)
                .totalPrice(totalPrice)
                .isValid(isValid)
                .build();
    }

}
