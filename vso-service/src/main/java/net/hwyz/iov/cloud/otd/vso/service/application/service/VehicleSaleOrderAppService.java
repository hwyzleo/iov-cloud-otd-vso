package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.enums.ClientType;
import net.hwyz.iov.cloud.framework.common.enums.Symbol;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.otd.vso.api.contract.Order;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.*;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderPaymentResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.OrderResponse;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderOperate;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.factory.OrderFactory;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.model.OrderModelConfigDo;
import net.hwyz.iov.cloud.otd.vso.service.domain.order.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.AccountNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelConfigTypeCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderLogDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.OrderModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.tsp.account.api.contract.Account;
import net.hwyz.iov.cloud.tsp.account.api.feign.service.ExAccountService;
import net.hwyz.iov.cloud.tsp.vmd.api.contract.VehicleOrderExService;
import net.hwyz.iov.cloud.tsp.vmd.api.feign.service.ExVehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final OrderLogDao orderLogDao;
    private final OrderFactory orderFactory;
    private final ExAccountService accountService;
    private final ExVehicleService vehicleService;
    private final OrderRepository orderRepository;
    private final OrderModelConfigDao orderModelConfigDao;
    private final SaleModelAppService saleModelAppService;

    /**
     * 查询车辆销售订单信息
     *
     * @param orderNum          订单号
     * @param orderState        订单状态
     * @param orderStateRange   订单状态范围
     * @param hasDeliveryPerson 是否有交付人员
     * @param beginTime         开始时间
     * @param endTime           结束时间
     * @return 销售车型列表
     */
    public List<OrderPo> search(String orderNum, Integer orderState, List<OrderState> orderStateRange, Boolean hasDeliveryPerson,
                                Date beginTime, Date endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNum", orderNum);
        map.put("orderState", orderState);
        if (orderStateRange != null && !orderStateRange.isEmpty()) {
            List<Integer> list = orderStateRange.stream().map(state -> state.value).toList();
            map.put("orderStateRange", list);
        }
        map.put("hasDeliveryPerson", hasDeliveryPerson);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return orderDao.selectPoByMap(map);
    }

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
            if (StrUtil.isNotBlank(orderPo.getDeliveryVin())) {
                displayName = orderPo.getDeliveryVin();
            } else {
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
            list.add(Order.builder()
                    .orderNum(orderPo.getOrderNum())
                    .orderState(orderPo.getOrderState())
                    .displayName(displayName)
                    .build());
        });
        return list;
    }

    /**
     * 统计车辆销售订单数量
     *
     * @param deliveryPersonId 交付人员ID
     * @param delivered        是否交付
     * @return 统计数量
     */
    public Integer count(String deliveryPersonId, Boolean delivered) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryPersonId", deliveryPersonId);
        map.put("delivered", delivered);
        return orderDao.countPoByMap(map);
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
        Account accountInfo = accountService.getAccountInfo(accountId);
        if (ObjUtil.isNull(accountInfo)) {
            throw new AccountNotExistException(accountId);
        }
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        OrderDo orderDo = orderFactory.buildFromWishlist(accountId, accountInfo.getMobile(), saleCode);
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
     * 意向金下订单
     *
     * @param accountId 账号ID
     * @param request   意向金下单请求
     * @return 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String earnestMoneyOrder(String accountId, EarnestMoneyOrderRequest request) {
        OrderDo orderDo = null;
        if (StrUtil.isNotBlank(request.getOrderNum())) {
            // 由心愿单转意向金
            orderDo = orderRepository.get(accountId, request.getOrderNum());
        }
        if (orderDo != null) {
            orderDo.earnestMoneyOrder();
        } else {
            // 直接意向金
            Account accountInfo = accountService.getAccountInfo(accountId);
            if (ObjUtil.isNull(accountInfo)) {
                throw new AccountNotExistException(accountId);
            }
            orderDo = orderFactory.buildFromEarnestMoney(accountId, accountInfo.getMobile(), request.getSaleCode());
        }
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(request.getSaleCode(), request.getSaleModelConfigType()));
        orderDo.saveLicenseCity(request.getLicenseCityCode());
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.ORDER_CREATE, "新增订单：" + orderDo.getOrderNum());
        return orderDo.getOrderNum();
    }

    /**
     * 定金下订单
     *
     * @param accountId 账号ID
     * @param request   定金下单请求
     * @return 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String downPaymentOrder(String accountId, DownPaymentOrderRequest request) {
        OrderDo orderDo = null;
        if (StrUtil.isNotBlank(request.getOrderNum())) {
            // 由心愿单转定金
            orderDo = orderRepository.get(accountId, request.getOrderNum());
        }
        if (orderDo != null) {
            orderDo.downPaymentOrder();
        } else {
            // 直接定金
            Account accountInfo = accountService.getAccountInfo(accountId);
            if (ObjUtil.isNull(accountInfo)) {
                throw new AccountNotExistException(accountId);
            }
            orderDo = orderFactory.buildFromDownPayment(accountId, accountInfo.getMobile(), request.getSaleCode());
        }
        String modelConfigCode = saleModelAppService.getModelConfigCode(request.getSaleModelConfigType());
        orderDo.saveModelConfig(modelConfigCode, getOrderModelConfigMap(request.getSaleCode(), request.getSaleModelConfigType()));
        orderDo.saveOrderPerson(accountId, request.getOrderPersonType(), request.getOrderPersonName(),
                request.getOrderPersonIdType(), request.getOrderPersonIdNum());
        orderDo.savePurchasePlan(request.getPurchasePlan());
        orderDo.saveLicenseCity(request.getLicenseCityCode());
        orderDo.saveDealership(request.getDealership());
        orderDo.saveDeliveryCenter(request.getDeliveryCenter());
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.ORDER_CREATE, "新增订单：" + orderDo.getOrderNum());
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
        return OrderResponse.builder()
                .saleCode(orderDo.getSaleCode())
                .orderNum(orderDo.getOrderNum())
                .orderState(orderDo.getOrderState().value)
                .orderTime(orderDo.getOrderTime())
                .orderPersonType(orderDo.getOrderPersonType())
                .purchasePlan(orderDo.getPurchasePlan())
                .orderPersonName(orderDo.getOrderPersonName())
                .orderPersonIdType(orderDo.getOrderPersonIdType())
                .orderPersonIdNum(orderDo.getOrderPersonIdNum())
                .licenseCityCode(orderDo.getLicenseCity())
                .dealershipCode(orderDo.getDealership())
                .deliveryCenterCode(orderDo.getDeliveryCenter())
                .saleModelConfigType(orderDo.getModelConfigType())
                .saleModelConfigName(orderDo.getModelConfigName())
                .saleModelConfigPrice(orderDo.getModelConfigPrice())
                .saleModelImages(getSaleModelImages(orderDo.getSaleCode(), orderDo.getModelConfigMap()))
                .saleModelDesc(orderDo.getModelConfigDesc())
                .totalPrice(orderDo.getTotalPrice())
                .build();
    }

    /**
     * 取消订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cancel(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.cancel();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.ORDER_CANCEL);
    }

    /**
     * 支付订单
     *
     * @param accountId 账号ID
     * @param request   支付订单请求
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderPaymentResponse pay(String accountId, OrderPaymentRequest request) {
        OrderDo orderDo = orderRepository.get(accountId, request.getOrderNum());
        if (orderDo == null) {
            throw new OrderNotExistException(request.getOrderNum());
        }
        // TODO 调用外部商户
        orderDo.pay(request.getPaymentAmount());
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.ORDER_PAY);
        return OrderPaymentResponse.builder()
                .orderNum(orderDo.getOrderNum())
                .paymentMerchant("TEST")
                .paymentReference("REFERENCE")
                .paymentAmount(request.getPaymentAmount())
                .paymentDateType(1)
                .paymentData("DATA")
                .build();
    }

    /**
     * 申请退款订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void requestRefund(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.requestRefund();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.APPLY_REFUND);
    }

    /**
     * 意向金转定金
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void earnestMoneyToDownPayment(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.earnestMoneyToDownPayment();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.EARNEST_MONEY_TO_DOWN_PAYMENT);
    }

    /**
     * 锁定订单
     *
     * @param accountId 账号ID
     * @param orderNum  订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void lock(String accountId, String orderNum) {
        OrderDo orderDo = orderRepository.get(accountId, orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.lock();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MP, accountId, OrderOperate.ORDER_LOCK);
    }

    /**
     * 分配交付人员
     *
     * @param orderNum 订单编号
     * @param request  分配交付人员请求
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void assignDeliveryPerson(String orderNum, AssignDeliveryPersonRequest request) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.saveDeliveryPerson(request.getDeliveryPersonId(), request.getDeliveryPersonName());
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.ASSIGN_DELIVERY_PERSON, "分配交付人员：" + request.getDeliveryPersonName());
    }

    /**
     * 分配车辆
     *
     * @param orderNum 订单编号
     * @param request  分配车辆请求
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void assignVehicle(String orderNum, AssignVehicleRequest request) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.saveDeliveryVehicle(request.getVin());
        orderRepository.save(orderDo);
        vehicleService.bindOrder(request.getVin(),
                VehicleOrderExService.builder()
                        .vin(request.getVin())
                        .orderNum(request.getOrderNum())
                        .build());
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.ASSIGN_VEHICLE, "分配车辆：" + request.getVin());
    }

    /**
     * 准备运输
     *
     * @param orderNum 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void prepareTransport(String orderNum) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.prepareTransport();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.PREPARE_TRANSPORT);
    }

    /**
     * 运输中
     *
     * @param orderNum 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void transporting(String orderNum) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.transporting();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.TRANSPORTING);
    }

    /**
     * 待交付
     *
     * @param orderNum 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void prepareDelivery(String orderNum) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.prepareDelivery();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.PREPARE_DELIVERY);
    }

    /**
     * 已交付
     *
     * @param orderNum 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void delivered(String orderNum) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.delivered();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.DELIVERED);
    }

    /**
     * 激活车辆
     *
     * @param orderNum 订单编号
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void activate(String orderNum) {
        OrderDo orderDo = orderRepository.get(orderNum);
        if (orderDo == null) {
            throw new OrderNotExistException(orderNum);
        }
        orderDo.activate();
        orderRepository.save(orderDo);
        recordOrderLog(orderDo.getOrderNum(), ClientType.MPT, OrderOperate.VEHICLE_ACTIVATE);
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

    /**
     * 记录订单日志
     *
     * @param orderNum     订单号
     * @param clientType   客户端类型
     * @param orderOperate 操作类型
     */
    private void recordOrderLog(String orderNum, ClientType clientType, OrderOperate orderOperate) {
        recordOrderLog(orderNum, clientType, null, null, orderOperate, null);
    }

    /**
     * 记录订单日志
     *
     * @param orderNum     订单号
     * @param clientType   客户端类型
     * @param orderOperate 操作类型
     * @param operateDesc  操作描述
     */
    private void recordOrderLog(String orderNum, ClientType clientType, OrderOperate orderOperate, String operateDesc) {
        recordOrderLog(orderNum, clientType, null, null, orderOperate, operateDesc);
    }

    /**
     * 记录订单日志
     *
     * @param orderNum     订单号
     * @param clientType   客户端类型
     * @param accountId    账号ID
     * @param orderOperate 操作类型
     */
    private void recordOrderLog(String orderNum, ClientType clientType, String accountId, OrderOperate orderOperate) {
        recordOrderLog(orderNum, clientType, accountId, null, orderOperate, null);
    }

    /**
     * 记录订单日志
     *
     * @param orderNum     订单号
     * @param clientType   客户端类型
     * @param accountId    账号ID
     * @param orderOperate 操作类型
     * @param operateDesc  操作描述
     */
    private void recordOrderLog(String orderNum, ClientType clientType, String accountId, OrderOperate orderOperate,
                                String operateDesc) {
        recordOrderLog(orderNum, clientType, accountId, null, orderOperate, operateDesc);
    }

    /**
     * 记录订单日志
     *
     * @param orderNum     订单号
     * @param clientType   客户端类型
     * @param accountId    账号ID
     * @param operator     操作者
     * @param orderOperate 操作类型
     * @param operateDesc  操作描述
     */
    private void recordOrderLog(String orderNum, ClientType clientType, String accountId, String operator,
                                OrderOperate orderOperate, String operateDesc) {
        if (clientType == ClientType.MPT) {
            accountId = SecurityUtils.getUserId().toString();
            operator = SecurityUtils.getUsername();
        }
        OrderLogPo orderLogPo = OrderLogPo.builder()
                .orderNum(orderNum)
                .operateClient(clientType.name())
                .accountId(accountId)
                .operator(operator)
                .operateType(orderOperate.name())
                .operateDesc(operateDesc)
                .operateTime(new Date())
                .build();
        orderLogDao.insertPo(orderLogPo);
    }

}
