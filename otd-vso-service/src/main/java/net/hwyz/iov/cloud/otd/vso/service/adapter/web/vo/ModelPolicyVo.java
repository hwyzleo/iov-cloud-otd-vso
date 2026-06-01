package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPolicyVo {

    private Long id;

    private String saleModelCode;

    private String modelCode;

    private String saleStatus;

    private List<String> availableRegions;

    private List<String> channels;

    private String marketingName;

    private String marketingImage;

    private String marketingCopy;

    private Integer sortWeight;

    private Timestamp effectiveFrom;

    private Timestamp effectiveTo;

    private String description;
}
