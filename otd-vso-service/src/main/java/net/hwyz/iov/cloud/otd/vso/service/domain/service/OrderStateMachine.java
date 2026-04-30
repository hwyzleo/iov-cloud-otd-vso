package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.MainStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单状态机
 *
 * @author VSO Team
 */
public class OrderStateMachine {

    /**
     * 状态转移规则
     */
    private static final Map<String, Set<MainStatus>> STATE_TRANSITIONS = new ConcurrentHashMap<>();

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
        Set<MainStatus> pendingCreateTransitions = EnumSet.of(
            MainStatus.PENDING_SUBMIT
        );
        STATE_TRANSITIONS.put("PENDING_CREATE", pendingCreateTransitions);

        Set<MainStatus> pendingSubmitTransitions = EnumSet.of(
            MainStatus.PENDING_AUDIT,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_SUBMIT", pendingSubmitTransitions);

        Set<MainStatus> pendingAuditTransitions = EnumSet.of(
            MainStatus.PENDING_LOCK,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_AUDIT", pendingAuditTransitions);

        Set<MainStatus> pendingLockTransitions = EnumSet.of(
            MainStatus.LOCKED,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_LOCK", pendingLockTransitions);

        Set<MainStatus> lockedTransitions = EnumSet.of(
            MainStatus.VEHICLE_ASSIGNED,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("LOCKED", lockedTransitions);

        Set<MainStatus> vehicleAssignedTransitions = EnumSet.of(
            MainStatus.PENDING_CONTRACT,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("VEHICLE_ASSIGNED", vehicleAssignedTransitions);

        Set<MainStatus> contractTransitions = EnumSet.of(
            MainStatus.PENDING_PAYMENT,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_CONTRACT", contractTransitions);

        Set<MainStatus> pendingPaymentTransitions = EnumSet.of(
            MainStatus.PENDING_PAYMENT,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_PAYMENT", pendingPaymentTransitions);

        Set<MainStatus> paidTransitions = EnumSet.of(
            MainStatus.PENDING_DELIVERY,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PAID", paidTransitions);

        Set<MainStatus> pendingDeliveryTransitions = EnumSet.of(
            MainStatus.DELIVERED,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("PENDING_DELIVERY", pendingDeliveryTransitions);

        Set<MainStatus> deliveredTransitions = EnumSet.of(
            MainStatus.COMPLETED,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        STATE_TRANSITIONS.put("DELIVERED", deliveredTransitions);

        Set<MainStatus> completedTransitions = EnumSet.noneOf(MainStatus.class);
        STATE_TRANSITIONS.put("COMPLETED", completedTransitions);

        Set<MainStatus> cancelledTransitions = EnumSet.noneOf(MainStatus.class);
        STATE_TRANSITIONS.put("CANCELLED", cancelledTransitions);

        Set<MainStatus> closedTransitions = EnumSet.noneOf(MainStatus.class);
        STATE_TRANSITIONS.put("CLOSED", closedTransitions);
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
     * @param fromState 当前状态（字符串）
     * @param toState 目标状态（字符串或MainStatus枚举）
     * @param orderType 订单类型
     * @return 是否合法
     */
    public static boolean canTransition(String fromState, Object toState, String orderType) {
        Set<MainStatus> allowedTransitionsSet = STATE_TRANSITIONS.get(fromState);
        if (allowedTransitionsSet == null) {
            return false;
        }
        
        String toStateStr = null;
        if (toState instanceof MainStatus) {
            toStateStr = ((MainStatus) toState).name();
        } else if (toState instanceof String) {
            toStateStr = (String) toState;
        }
        
        if (toStateStr == null) {
            return false; // 如果输入参数类型既不是String也不是MainStatus，则非法
        }
        
        // 遵循枚举实例为标准的判断逻辑
        return allowedTransitionsSet.contains(MainStatus.valueOf(toStateStr));
    }

    /**
     * 验证状态转移
     *
     * @param fromState 当前状态
     * @param toState 目标状态
     * @param orderType 订单类型
     * @throws IllegalStateException 如果转移不合法
     */
    public static void validateTransition(String fromState, Object toState, String orderType) {
        if (!canTransition(fromState, toState, orderType)) {
            throw new IllegalStateException(
                String.format("订单状态转移不合法：从 [%s] 无法转移到 [%s]", fromState, toState.toString())
            );
        }
    }

    /**
     * 获取所有允许的下一状态
     *
     * @param currentState 当前状态
     * @return 允许的下一状态集合（字符串形式）
     */
    public static Set<String> getAllowedNextStates(String currentState) {
        Set<MainStatus> transitions = STATE_TRANSITIONS.get(currentState);
        if (transitions == null) {
            return java.util.Collections.emptySet();
        }
        return transitions.stream().map(MainStatus::name).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 检查是否是结束状态
     *
     * @param state 状态
     * @return 是否是结束状态
     */
    public static boolean isEndState(Object state) {
        Set<MainStatus> endStates = Set.of(
            MainStatus.COMPLETED,
            MainStatus.CANCELLED,
            MainStatus.CLOSED
        );
        
        if (state instanceof MainStatus) {
            return endStates.contains(state);
        } else if (state instanceof String) {
            return endStates.contains(MainStatus.valueOf((String) state));
        }
        return false; // 若输入参数既非MainStatus也不匹配其name，则不是结束状态
    }

    /**
     * 检查是否是中间状态
     *
     * @param state 状态
     * @return 是否是中间状态
     */
    public static boolean isIntermediateState(Object state) {
        return !isEndState(state);
    }
}
