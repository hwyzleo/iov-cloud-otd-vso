package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 结束语义枚举
 *
 * @author VSO Team
 */
public enum EndType {

    /**
     * 取消：用户或业务方主动终止订单
     */
    CANCEL,

    /**
     * 关闭：因超时、异常、审核不通过或人工处理等原因结束流程
     */
    CLOSE,

    /**
     * 作废：误创建、重复创建、错误单据等无效订单记录
     */
    VOID;

}
