package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.EarnestMoneyOrderRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.OrderPaymentRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.SelectedSaleModelRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderPaymentResponse;
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
        return WishlistResponse.builder()
                .saleCode(orderDo.getSaleCode())
                .orderNum(orderDo.getOrderNum())
                .saleModelConfigType(orderDo.getModelConfigType())
                .saleModelConfigName(orderDo.getModelConfigName())
                .saleModelConfigPrice(orderDo.getModelConfigPrice())
                .totalPrice(orderDo.getTotalPrice())
                .saleModelImages(getSaleModelImages(orderDo.getSaleCode(), orderDo.getModelConfigMap()))
                .saleModelDesc(orderDo.getModelConfigDesc())
                .isValid(!checkSaleModelChange(orderDo.getSaleCode(), orderDo.getModelConfigMap()))
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
        List<String> saleModelImages = new ArrayList<>();
        return OrderResponse.builder()
                .saleCode(orderDo.getSaleCode())
                .orderNum(orderDo.getOrderNum())
                .saleModelConfigType(orderDo.getModelConfigType())
                .saleModelConfigName(orderDo.getModelConfigName())
                .saleModelConfigPrice(orderDo.getModelConfigPrice())
                .saleModelImages(saleModelImages)
                .totalPrice(orderDo.getTotalPrice())
                .build();
    }

    /**
     * 取消订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void cancel(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.cancel();
        orderRepository.save(orderDo);
    }

    /**
     * 支付订单
     *
     * @param accountId 账号ID
     * @param request   支付订单请求
     */
    public OrderPaymentResponse pay(String accountId, OrderPaymentRequest request) {
        OrderDo orderDo = orderRepository.get(accountId, request.getOrderNum());
        if (orderDo == null) {
            throw new OrderNotExistException(request.getOrderNum());
        }
        // TODO 调用外部商户
        orderDo.pay();
        orderRepository.save(orderDo);
        return OrderPaymentResponse.builder()
                .build();
    }

    /**
     * 申请退款订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void requestRefund(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.requestRefund();
        orderRepository.save(orderDo);
    }

    /**
     * 意向金转定金
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void earnestMoneyToDownPayment(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.earnestMoneyToDownPayment();
        orderRepository.save(orderDo);
    }

    /**
     * 锁定订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    public void lock(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.lock();
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
     * 获取用户选择车型配置对应的图片集
     *
     * @param saleCode       销售代码
     * @param modelConfigMap 用户选择的销售车型配置
     * @return 用户选择配置对应的图片集
     */
    private List<String> getSaleModelImages(String saleCode, Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap) {
        List<String> images = new ArrayList<>();
        Map<String, SaleModelConfigPo> saleModelConfigMap = saleModelAppService.getSaleModelConfigMap(saleCode);
        modelConfigMap.forEach((key, value) -> {
            SaleModelConfigPo saleModelConfigPo = saleModelConfigMap.get(key.name() + Symbol.UNDERSCORE.value + value.getTypeCode());
            if (saleModelConfigPo != null && !saleModelConfigPo.getTypeImage().isEmpty() && (
                    SaleModelConfigType.EXTERIOR.name().equals(saleModelConfigPo.getType()) ||
                            SaleModelConfigType.INTERIOR.name().equals(saleModelConfigPo.getType()))) {
                List<String> list = JSONUtil.toBean(saleModelConfigPo.getTypeImage(), new TypeReference<List<String>>() {
                }, true);
                if (!list.isEmpty()) {
                    images.add(list.get(0));
                }
            }
        });
        return images;
    }

    /**
     * 检查用户选择车型配置与当前对应的销售车型是否已经发生的变更
     *
     * @param saleCode       销售代码
     * @param modelConfigMap 用户选择的销售车型配置
     * @return 用户选择配置与当前对应的销售车型是否已经发生的变更
     */
    private Boolean checkSaleModelChange(String saleCode, Map<SaleModelConfigType, OrderModelConfigDo> modelConfigMap) {
        Map<String, SaleModelConfigPo> saleModelMap = saleModelAppService.getSaleModelConfigMap(saleCode);
        for (OrderModelConfigDo orderModelConfigDo : modelConfigMap.values()) {
            SaleModelConfigPo saleModelConfigPo = saleModelMap.get(orderModelConfigDo.getType().name() +
                    Symbol.UNDERSCORE.value + orderModelConfigDo.getTypeCode());
            if (saleModelConfigPo == null) {
                return true;
            }
            if (!saleModelConfigPo.getTypeName().equals(orderModelConfigDo.getTypeName())) {
                return true;
            }
            if (saleModelConfigPo.getTypePrice().compareTo(orderModelConfigDo.getTypePrice()) != 0) {
                return true;
            }
        }
        return false;
    }

}
