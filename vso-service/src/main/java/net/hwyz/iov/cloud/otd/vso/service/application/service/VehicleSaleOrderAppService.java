package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.EarnestMoneyOrderRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.factory.OrderFactory;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderModelConfigDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelConfigTypeCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderModelConfigPo;
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

    /**
     * 获取订单列表
     *
     * @param type      订单类型
     * @param accountId 账号ID
     * @return 订单列表
     */
    public List<Order> getOrderList(String type, String accountId) {
        List<Order> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("orderPersonId", accountId);
        if ("valid".equals(type)) {
            map.put("stateValid", 1);
        }
        orderDao.selectPoByMap(map).forEach(orderPo -> {
            String displayName = "";
            OrderState orderState = OrderState.valOf(orderPo.getOrderState());
            if (orderState == OrderState.WISHLIST) {
                List<OrderModelConfigPo> orderModelConfigPoList = orderModelConfigDao.selectPoByExample(OrderModelConfigPo.builder()
                        .orderNum(orderPo.getOrderNum())
                        .type(SaleModelConfigType.MODEL.name())
                        .build());
                if (!orderModelConfigPoList.isEmpty()) {
                    displayName = orderModelConfigPoList.get(0).getTypeName();
                } else {
                    displayName = orderPo.getModelConfigCode();
                }
            } else {
                displayName = orderPo.getOrderNum();
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
     * @param request   车型选择请求
     * @return 订单编号
     */
    public String createUserWishlist(String accountId, SelectedSaleModelRequest request) {
        String saleCode = request.getSaleCode();
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        OrderDo orderDo = orderFactory.buildFromWishlist(accountId, saleCode);
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(saleCode, request.getSaleModelConfigType()));
        orderRepository.save(orderDo);
        return orderDo.getOrderNum();
    }

    /**
     * 修改用户心愿单
     *
     * @param accountId 账号ID
     * @param request   车型选择请求
     */
    public void modifyUserWishlist(String accountId, SelectedSaleModelRequest request) {
        OrderDo orderDo = orderRepository.get(accountId, request.getOrderNum());
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(request.getSaleCode(), request.getSaleModelConfigType()));
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
                throw new SaleModelConfigTypeCodeNotExistException(saleCode, key, value);
            }
            SaleModelConfigPo saleModelConfigPo = saleModelConfigMap.get(key + Symbol.UNDERSCORE.value + value);
            if (saleModelConfigPo == null) {
                throw new SaleModelConfigTypeCodeNotExistException(saleCode, key, value);
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
        List<String> saleModelImages = new ArrayList<>();
        Map<String, BigDecimal> saleModelPrice = new HashMap<>();
        boolean isValid = true;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<String, SaleModelConfigPo> saleModelMap = saleModelAppService.getSaleModelConfigMap(orderDo.getSaleCode());
        for (OrderModelConfigDo orderModelConfigDo : orderDo.getModelConfigMap().values()) {
            saleModelType.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeCode());
            saleModelName.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeName());
            saleModelPrice.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypePrice());
            totalPrice = totalPrice.add(orderModelConfigDo.getTypePrice());
            SaleModelConfigPo saleModelConfigPo = saleModelMap.get(orderModelConfigDo.getType().name() +
                    Symbol.UNDERSCORE.value + orderModelConfigDo.getTypeCode());
            if (saleModelConfigPo == null) {
                isValid = false;
                continue;
            }
            List<String> images = JSONUtil.toBean(saleModelConfigPo.getTypeImage(), new TypeReference<>() {
            }, true);
            if (!images.isEmpty() && (orderModelConfigDo.getType() == SaleModelConfigType.EXTERIOR
                    || orderModelConfigDo.getType() == SaleModelConfigType.INTERIOR)) {
                saleModelImages.add(images.get(0));
            }
            if (isValid) {
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
                .saleModelImages(saleModelImages)
                .totalPrice(totalPrice)
                .isValid(isValid)
                .build();
    }

    /**
     * 意向金（小定）下订单
     *
     * @param accountId 账号ID
     * @param request   意向金下单请求
     * @return 订单编号
     */
    public String earnestMoneyOrder(String accountId, EarnestMoneyOrderRequest request) {
        OrderDo orderDo;
        if (request.getOrderNum() != null) {
            // 由心愿单转小定
            orderDo = orderRepository.get(accountId, request.getOrderNum());
        } else {
            // 直接小定
            orderDo = orderFactory.buildFromEarnestMoney(accountId, request.getSaleCode());
        }
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(request.getSaleCode(), request.getSaleModelConfigType()));
        orderDo.saveLicenseCity(request.getLicenseCity());
        orderRepository.save(orderDo);
        return orderDo.getOrderNum();
    }

    /**
     * 获取用户订单详情
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     * @return 用户心愿单详情
     */
    public OrderResponse getUserOrderResponse(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            return null;
        }
        Map<String, String> saleModelType = new HashMap<>();
        Map<String, String> saleModelName = new HashMap<>();
        List<String> saleModelImages = new ArrayList<>();
        Map<String, BigDecimal> saleModelPrice = new HashMap<>();
        boolean isValid = true;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<String, SaleModelConfigPo> saleModelMap = saleModelAppService.getSaleModelConfigMap(orderDo.getSaleCode());
        for (OrderModelConfigDo orderModelConfigDo : orderDo.getModelConfigMap().values()) {
            saleModelType.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeCode());
            saleModelName.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypeName());
            saleModelPrice.put(orderModelConfigDo.getType().name(), orderModelConfigDo.getTypePrice());
            totalPrice = totalPrice.add(orderModelConfigDo.getTypePrice());
            SaleModelConfigPo saleModelConfigPo = saleModelMap.get(orderModelConfigDo.getType().name() +
                    Symbol.UNDERSCORE.value + orderModelConfigDo.getTypeCode());
            if (saleModelConfigPo == null) {
                isValid = false;
                continue;
            }
            List<String> images = JSONUtil.toBean(saleModelConfigPo.getTypeImage(), new TypeReference<>() {
            }, true);
            if (!images.isEmpty() && (orderModelConfigDo.getType() == SaleModelConfigType.EXTERIOR
                    || orderModelConfigDo.getType() == SaleModelConfigType.INTERIOR)) {
                saleModelImages.add(images.get(0));
            }
            if (isValid) {
                if (!saleModelConfigPo.getTypeName().equals(orderModelConfigDo.getTypeName())) {
                    isValid = false;
                    continue;
                }
                if (saleModelConfigPo.getTypePrice().compareTo(orderModelConfigDo.getTypePrice()) != 0) {
                    isValid = false;
                }
            }
        }
        return OrderResponse.builder()
                .saleCode(orderDo.getSaleCode())
                .orderNum(orderDo.getOrderNum())
                .saleModelConfigType(saleModelType)
                .saleModelConfigName(saleModelName)
                .saleModelConfigPrice(saleModelPrice)
                .saleModelImages(saleModelImages)
                .totalPrice(totalPrice)
                .isValid(isValid)
                .build();
    }

    /**
     * 取消订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void cancelOrder(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.cancel();
        orderRepository.save(orderDo);
    }

}
