package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 销售车型配置结果 DTO
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigResult {

    private Long id;
    private String saleModelCode;
    private String type;
    private String typeCode;
    private String typeName;
    private BigDecimal typePrice;
    private List<String> typeImage;
    private Boolean enable;
    private Integer sort;
    private Instant createTime;
    private String createBy;
    private Instant modifyTime;
    private String modifyBy;

}
