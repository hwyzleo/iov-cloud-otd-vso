package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 计数查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class CountQuery {

    private String deliveryPersonId;
    private Boolean delivered;

}
