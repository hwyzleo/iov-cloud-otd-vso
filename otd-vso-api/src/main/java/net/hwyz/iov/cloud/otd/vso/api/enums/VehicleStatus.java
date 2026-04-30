package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 配车状态枚举
 *
 * @author VSO Team
 */
public enum VehicleStatus {

    /**
     * 待配车
     */
    PENDING_ASSIGN,

    /**
     * 配车中
     */
    ASSIGNING,

    /**
     * 已配车
     */
    ASSIGNED,

    /**
     * 待绑定车辆
     */
    PENDING_BIND,

    /**
     * 已绑定车辆
     */
    BOUND,

    /**
     * 改配中
     */
    CHANGING,

    /**
     * 已释放车源
     */
    RELEASED,

    /**
     * 配车失败
     */
    ASSIGN_FAILED,

    /**
     * 已关闭
     */
    CLOSED;

}
