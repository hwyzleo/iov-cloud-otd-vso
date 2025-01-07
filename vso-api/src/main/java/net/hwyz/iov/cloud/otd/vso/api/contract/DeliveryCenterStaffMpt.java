package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

/**
 * 管理后台交付中心员工
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeliveryCenterStaffMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 门店代码
     */
    private String dealershipCode;

    /**
     * 门店名称
     */
    private String dealershipName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 未交付订单数
     */
    private Integer notDeliveryOrderCount;

}
