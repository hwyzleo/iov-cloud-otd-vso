package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 购车权益
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseBenefits {

    /**
     * 主键
     */
    private Long id;

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 权益开始时间
     */
    private Date startTime;

    /**
     * 权益结束时间
     */
    private Date endTime;

    /**
     * 权益简介
     */
    private String intro;

    /**
     * 权益详情
     */
    private String detail;

}
