package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交付中心人员结果 DTO
 * <p>
 * 人员标识与展示信息来自 ORG 员工主数据（EDD-ORG-DSN-CR-003），不含 MPT 账号字段。
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStaffResult {

    private String dealershipCode;
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
    private Integer notDeliveryOrderCount;

}
