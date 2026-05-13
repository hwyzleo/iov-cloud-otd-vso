package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Map;

/**
 * 已选择销售车型请求 Vo（动态特征值模式）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedSaleModelRequestVo {

    /**
     * 销售车型代码
     */
    private String saleModelCode;

    /**
     * 订单号（可选）
     */
    private String orderNo;

    /**
     * 销售车型配置类型（特征值选择）
     * key: 特征族编码（如 "001", "002"）
     * value: 特征值编码
     *
     * 示例：
     * {
     *   "001": "00101",  // 特征族001 -> 特征值00101
     *   "002": "00202",  // 特征族002 -> 特征值00202
     *   "003": "00301"   // 特征族003 -> 特征值00301
     * }
     */
    private Map<String, String> saleModelConfigType;

}
