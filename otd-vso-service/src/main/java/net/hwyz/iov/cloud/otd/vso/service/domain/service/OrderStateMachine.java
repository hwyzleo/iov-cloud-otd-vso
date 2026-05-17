package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订单状态机
 *
 * @author VSO Team
 */
public class OrderStateMachine {

    /**
     * 状态转移规则
     */
    private static final Map<Integer, Set<OrderState>> STATE_TRANSITIONS = new ConcurrentHashMap<>();

    static {
        // 小订单状态转移
        initSmallOrderTransitions();
        
        // 正式订单状态转移
        initFormalOrderTransitions();
    }

    /**
     * 初始化小订单状态转移规则
     */
    private static void initSmallOrderTransitions() {
        // WISHLIST(100) -> EARNEST_MONEY_UNPAID(200)
        Set<OrderState> wishlistTransitions = EnumSet.of(OrderState.EARNEST_MONEY_UNPAID);
        STATE_TRANSITIONS.put(100, wishlistTransitions);
        
        // EARNEST_MONEY_UNPAID(200) -> EARNEST_MONEY_PAID(210), DOWN_PAYMENT_UNPAID(300), CANCEL(950), EXPIRED(960)
        Set<OrderState> earnestUnpaidTransitions = EnumSet.of(
            OrderState.EARNEST_MONEY_PAID,
            OrderState.DOWN_PAYMENT_UNPAID,
            OrderState.CANCEL,
            OrderState.EXPIRED
        );
        STATE_TRANSITIONS.put(200, earnestUnpaidTransitions);
        
        // EARNEST_MONEY_PAID(210) -> DOWN_PAYMENT_UNPAID(300), CANCEL(950)
        Set<OrderState> earnestPaidTransitions = EnumSet.of(OrderState.DOWN_PAYMENT_UNPAID, OrderState.CANCEL);
        STATE_TRANSITIONS.put(210, earnestPaidTransitions);
        
        // DOWN_PAYMENT_UNPAID(300) -> DOWN_PAYMENT_PAID(310), CANCEL(950)
        Set<OrderState> downPaymentUnpaidTransitions = EnumSet.of(OrderState.DOWN_PAYMENT_PAID, OrderState.CANCEL);
        STATE_TRANSITIONS.put(300, downPaymentUnpaidTransitions);
        
        // DOWN_PAYMENT_PAID(310) -> ARRANGE_PRODUCTION(400), CANCEL(950)
        Set<OrderState> downPaymentPaidTransitions = EnumSet.of(OrderState.ARRANGE_PRODUCTION, OrderState.CANCEL);
        STATE_TRANSITIONS.put(310, downPaymentPaidTransitions);
        
        // ARRANGE_PRODUCTION(400) -> ALLOCATION_VEHICLE(450), CANCEL(950)
        Set<OrderState> arrangeProductionTransitions = EnumSet.of(OrderState.ALLOCATION_VEHICLE, OrderState.CANCEL);
        STATE_TRANSITIONS.put(400, arrangeProductionTransitions);
        
        // ALLOCATION_VEHICLE(450) -> APPLY_TRANSPORT(470), CANCEL(950)
        Set<OrderState> allocationVehicleTransitions = EnumSet.of(OrderState.APPLY_TRANSPORT, OrderState.CANCEL);
        STATE_TRANSITIONS.put(450, allocationVehicleTransitions);
        
        // APPLY_TRANSPORT(470) -> PREPARE_TRANSPORT(500), CANCEL(950)
        Set<OrderState> applyTransportTransitions = EnumSet.of(OrderState.PREPARE_TRANSPORT, OrderState.CANCEL);
        STATE_TRANSITIONS.put(470, applyTransportTransitions);
        
        // PREPARE_TRANSPORT(500) -> TRANSPORTING(550), CANCEL(950)
        Set<OrderState> prepareTransportTransitions = EnumSet.of(OrderState.TRANSPORTING, OrderState.CANCEL);
        STATE_TRANSITIONS.put(500, prepareTransportTransitions);
        
        // TRANSPORTING(550) -> PREPARE_DELIVER(600), CANCEL(950)
        Set<OrderState> transportingTransitions = EnumSet.of(OrderState.PREPARE_DELIVER, OrderState.CANCEL);
        STATE_TRANSITIONS.put(550, transportingTransitions);
        
        // PREPARE_DELIVER(600) -> DELIVERED(650), CANCEL(950)
        Set<OrderState> prepareDeliverTransitions = EnumSet.of(OrderState.DELIVERED, OrderState.CANCEL);
        STATE_TRANSITIONS.put(600, prepareDeliverTransitions);
        
        // DELIVERED(650) -> ACTIVATED(700), CANCEL(950)
        Set<OrderState> deliveredTransitions = EnumSet.of(OrderState.ACTIVATED, OrderState.CANCEL);
        STATE_TRANSITIONS.put(650, deliveredTransitions);
        
        // ACTIVATED(700) -> 终态，无转移
        Set<OrderState> activatedTransitions = EnumSet.noneOf(OrderState.class);
        STATE_TRANSITIONS.put(700, activatedTransitions);
        
        // CANCEL(950) -> 终态，无转移
        Set<OrderState> cancelTransitions = EnumSet.noneOf(OrderState.class);
        STATE_TRANSITIONS.put(950, cancelTransitions);
        
        // EXPIRED(960) -> 终态，无转移
        Set<OrderState> expiredTransitions = EnumSet.noneOf(OrderState.class);
        STATE_TRANSITIONS.put(960, expiredTransitions);
    }

    /**
     * 初始化正式订单状态转移规则（与小订单类似）
     */
    private static void initFormalOrderTransitions() {
        // 正式订单状态转移规则与小订单相同
    }

    /**
     * 检查状态转移是否合法
     *
     * @param fromStateValue 当前状态值
     * @param toStateValue 目标状态值
     * @param orderType 订单类型
     * @return 是否合法
     */
    public static boolean canTransition(Integer fromStateValue, Integer toStateValue, OrderType orderType) {
        Set<OrderState> allowedTransitionsSet = STATE_TRANSITIONS.get(fromStateValue);
        if (allowedTransitionsSet == null) {
            return false;
        }
        
        OrderState toState = OrderState.fromValue(toStateValue);
        if (toState == null) {
            return false;
        }
        
        return allowedTransitionsSet.contains(toState);
    }

    /**
     * 验证状态转移
     *
     * @param fromStateValue 当前状态值
     * @param toStateValue 目标状态值
     * @param orderType 订单类型
     * @throws IllegalStateException 如果转移不合法
     */
    public static void validateTransition(Integer fromStateValue, Integer toStateValue, OrderType orderType) {
        if (!canTransition(fromStateValue, toStateValue, orderType)) {
            throw new IllegalStateException(
                String.format("订单状态转移不合法：从 [%d] 无法转移到 [%d]", fromStateValue, toStateValue)
            );
        }
    }

    /**
     * 获取所有允许的下一状态
     *
     * @param currentStateValue 当前状态值
     * @return 允许的下一状态值集合
     */
    public static Set<Integer> getAllowedNextStates(Integer currentStateValue) {
        Set<OrderState> transitions = STATE_TRANSITIONS.get(currentStateValue);
        if (transitions == null) {
            return Collections.emptySet();
        }
        return transitions.stream().map(OrderState::getValue).collect(Collectors.toSet());
    }

    /**
     * 检查是否是结束状态
     *
     * @param stateValue 状态值
     * @return 是否是结束状态
     */
    public static boolean isEndState(Integer stateValue) {
        Set<OrderState> endStates = Set.of(OrderState.ACTIVATED, OrderState.CANCEL, OrderState.EXPIRED);
        OrderState state = OrderState.fromValue(stateValue);
        return state != null && endStates.contains(state);
    }

    /**
     * 检查是否是中间状态
     *
     * @param stateValue 状态值
     * @return 是否是中间状态
     */
    public static boolean isIntermediateState(Integer stateValue) {
        return !isEndState(stateValue);
    }
}