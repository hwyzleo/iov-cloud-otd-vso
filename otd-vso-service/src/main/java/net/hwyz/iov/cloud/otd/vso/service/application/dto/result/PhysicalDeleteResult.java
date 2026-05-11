package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 物理删除结果
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalDeleteResult {

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
