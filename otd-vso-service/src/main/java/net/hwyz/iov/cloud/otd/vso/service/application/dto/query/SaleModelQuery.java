package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 销售车型查询参数
 *
 * @author VSO Team
 */
@Data
@Builder
public class SaleModelQuery {

    private String saleCode;
    private String modelName;
    private Instant beginTime;
    private Instant endTime;

}
