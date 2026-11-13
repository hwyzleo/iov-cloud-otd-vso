package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderDealershipVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.DealershipQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderDealershipAppService;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoBaseException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端订单详情销售/交付门店选项接口（OTD-VSO-DSN-CR-008 US-022）
 * <p>
 * 仅负责参数校验、委托应用服务与包装响应；不直连 Feign、不写订单、不捕获异常后返回伪成功。
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile/vso/v1")
public class MobileOrderDealershipController extends BaseController {

    /**
     * 销售门店服务类型
     */
    private static final String SERVICE_TYPE_SALES = "S";

    /**
     * 交付中心服务类型
     */
    private static final String SERVICE_TYPE_DELIVERY = "D";

    private final OrderDealershipAppService orderDealershipAppService;

    /**
     * 查询销售或交付门店选项（US-022）
     *
     * @param serviceType  服务类型，仅 S 或 D
     * @param provinceCode 省级行政区代码
     * @param cityCode     地级行政区代码
     * @param countyCode   县级行政区代码
     * @param requestLon   请求经度
     * @param requestLat   请求纬度
     * @return 门店选项列表，无结果为空列表
     */
    @GetMapping("/dealership/list")
    public ApiResponse<List<OrderDealershipVo>> getDealershipList(
            @RequestParam(value = "serviceType", required = false) String serviceType,
            @RequestParam(value = "provinceCode", required = false) String provinceCode,
            @RequestParam(value = "cityCode", required = false) String cityCode,
            @RequestParam(value = "countyCode", required = false) String countyCode,
            @RequestParam(value = "requestLon", required = false) String requestLon,
            @RequestParam(value = "requestLat", required = false) String requestLat) {
        log.info("手机客户端[{}]获取服务类型[{}]门店选项，省[{}]，市[{}]，区[{}]，经度[{}]，纬度[{}]",
                ParamHelper.getClientAccountInfo(), serviceType, provinceCode, cityCode, countyCode, requestLon, requestLat);
        validateServiceType(serviceType);
        DealershipQuery query = DealershipQuery.builder()
                .serviceType(serviceType)
                .provinceCode(provinceCode)
                .cityCode(cityCode)
                .countyCode(countyCode)
                .requestLon(requestLon)
                .requestLat(requestLat)
                .build();
        return ApiResponse.ok(orderDealershipAppService.listDealerships(query));
    }

    /**
     * 服务类型前置校验：缺失或非 S/D（含售后 A）一律拒绝，不得调用应用服务与 EDD-ORG
     */
    private void validateServiceType(String serviceType) {
        if (!SERVICE_TYPE_SALES.equals(serviceType) && !SERVICE_TYPE_DELIVERY.equals(serviceType)) {
            throw new VsoBaseException(VsoErrorCode.INVALID_PARAM, "serviceType 仅支持 S 或 D");
        }
    }
}
