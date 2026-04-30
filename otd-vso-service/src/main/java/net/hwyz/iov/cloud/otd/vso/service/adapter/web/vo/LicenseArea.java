package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 上牌区域
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseArea {

    /**
     * 省级行政区代码
     */
    private String provinceCode;

    /**
     * 地区级行政区代码
     */
    private String cityCode;

    /**
     * 显示名称
     */
    private String displayName;

}
