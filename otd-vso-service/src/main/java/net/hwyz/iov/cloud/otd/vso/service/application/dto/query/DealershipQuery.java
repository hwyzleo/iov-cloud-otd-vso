package net.hwyz.iov.cloud.otd.vso.service.application.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 销售/交付门店选项查询参数（OTD-VSO-DSN-CR-008 §4.1）
 * <p>
 * 只承载端上通用查询条件，不含 orderNo、accountId、品牌、车型或场景编码；
 * 区域编码与经纬度原样透传 EDD-ORG，VSO 不做二次筛选。
 *
 * @author VSO Team
 */
@Data
@Builder
public class DealershipQuery {

    /**
     * 服务类型，仅 S（销售）或 D（交付）
     */
    private String serviceType;

    /**
     * 省级行政区代码
     */
    private String provinceCode;

    /**
     * 地级行政区代码
     */
    private String cityCode;

    /**
     * 县级行政区代码
     */
    private String countyCode;

    /**
     * 请求经度
     */
    private String requestLon;

    /**
     * 请求纬度
     */
    private String requestLat;

}
