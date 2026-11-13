package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 订单详情销售/交付门店选项（OTD-VSO-DSN-CR-008 §4.2）
 * <p>
 * VSO 自有契约，避免 EDD-ORG 内部 VO 变化直接穿透到 iOS；不新增排序、距离重算或分页字段。
 *
 * @author VSO Team
 */
@Data
public class OrderDealershipVo {

    /**
     * 门店编码，供客户端后续提交
     */
    private String code;

    /**
     * 门店全称
     */
    private String name;

    /**
     * 门店地址
     */
    private String address;

    /**
     * 距离（千米），由 EDD-ORG 计算；无法计算时为 null
     */
    private Double distance;

}
