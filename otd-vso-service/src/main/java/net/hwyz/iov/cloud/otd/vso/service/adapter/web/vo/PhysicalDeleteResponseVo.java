package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 物理删除响应 VO
 *
 * @author VSO Team
 */
@Data
public class PhysicalDeleteResponseVo {

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 总删除记录数
     */
    private Integer totalDeletedRecords;

    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 每张表删除记录数
     */
    private Map<String, Integer> tableDeleteCount;
}
