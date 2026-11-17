package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.*;

import java.io.Serializable;

/**
 * 管理后台交付中心员工
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCenterStaffMpt implements Serializable {

    /**
     * 门店代码
     */
    private String dealershipCode;

    /**
     * 门店名称
     */
    private String dealershipName;

    /**
     * 员工ID（ORG tb_employee.id），作为交付人员标识
     */
    private Long employeeId;

    /**
     * 工号
     */
    private String employeeCode;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工手机号（可空）
     */
    private String phone;

    /**
     * 未交付订单数
     */
    private Integer notDeliveryOrderCount;

}
