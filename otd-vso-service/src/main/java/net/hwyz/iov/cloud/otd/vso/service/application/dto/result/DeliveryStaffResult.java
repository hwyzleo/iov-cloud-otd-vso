package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交付中心人员结果 DTO
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
    private Long userId;
    private String userName;
    private String nickName;
    private String phonenumber;
    private Integer notDeliveryOrderCount;

}
