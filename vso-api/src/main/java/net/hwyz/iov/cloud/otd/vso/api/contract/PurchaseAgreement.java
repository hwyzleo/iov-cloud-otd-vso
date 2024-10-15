package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购车协议
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseAgreement {

    /**
     * 主键
     */
    private Long id;

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 协议标题
     */
    private String title;

    /**
     * 协议简介
     */
    private String intro;

    /**
     * 协议详情
     */
    private String detail;

}
