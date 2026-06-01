package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPolicyAvailableVo {

    private String modelCode;

    private String modelName;

    private String carlineCode;

    private String status;

    private Boolean inPolicy;

    private String saleStatus;
}
