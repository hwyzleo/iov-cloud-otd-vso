package net.hwyz.iov.cloud.otd.vso.service.application.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.org.api.enums.DealershipServiceType;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.vo.Dealership;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderDealershipVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.DealershipQuery;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DealershipQueryFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoBaseException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单详情销售/交付门店选项应用服务（OTD-VSO-DSN-CR-008）
 * <p>
 * VSO 是销售场景的薄适配层：不持有门店主数据、不查询组织数据库、不读写订单。
 * 只把端上通用条件透传给 EDD-ORG，并把上游结果映射为 VSO 自有 VO；
 * 上游异常或降级返回 null 一律转业务错误，禁止当作“无门店”返回空列表。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDealershipAppService {

    /**
     * 交付中心服务类型（EDD-ORG DealershipServiceType.D）
     */
    private static final String SERVICE_TYPE_DELIVERY = "D";

    /**
     * 销售门店服务类型（EDD-ORG DealershipServiceType.S）
     */
    private static final String SERVICE_TYPE_SALES = "S";

    private final OrgDealershipService orgDealershipService;

    /**
     * 查询销售或交付门店选项（US-022）
     *
     * @param query 通用查询条件
     * @return 门店选项列表，无结果为空列表；顺序与距离均沿用 EDD-ORG 结果
     */
    public List<OrderDealershipVo> listDealerships(DealershipQuery query) {
        DealershipServiceType serviceType = resolveServiceType(query == null ? null : query.getServiceType());
        List<Dealership> dealershipList;
        try {
            dealershipList = orgDealershipService.getDealershipList(serviceType,
                    query.getProvinceCode(), query.getCityCode(), query.getCountyCode(),
                    query.getRequestLon(), query.getRequestLat());
        } catch (Exception e) {
            log.error("订单详情门店选项查询失败，服务类型[{}]，省[{}]，市[{}]，区[{}]，状态码[{}]",
                    serviceType, query.getProvinceCode(), query.getCityCode(), query.getCountyCode(),
                    feignStatus(e), e);
            throw new DealershipQueryFailedException("订单详情门店选项查询失败", e);
        }
        if (dealershipList == null) {
            // 降级返回 null 代表上游故障，不等价于“无门店”
            log.error("订单详情门店选项查询降级返回 null，服务类型[{}]，省[{}]，市[{}]，区[{}]",
                    serviceType, query.getProvinceCode(), query.getCityCode(), query.getCountyCode());
            throw new DealershipQueryFailedException("订单详情门店选项查询失败");
        }
        return toVoList(dealershipList);
    }

    /**
     * 仅接受 S/D（不开放售后 A），非法类型不调用 EDD-ORG
     */
    private DealershipServiceType resolveServiceType(String serviceType) {
        DealershipServiceType resolved = DealershipServiceType.valOf(serviceType);
        if (resolved == null
                || !(SERVICE_TYPE_SALES.equals(resolved.name()) || SERVICE_TYPE_DELIVERY.equals(resolved.name()))) {
            throw new VsoBaseException(VsoErrorCode.INVALID_PARAM, "serviceType 仅支持 S 或 D");
        }
        return resolved;
    }

    /**
     * 原样映射上游结果，不重排、不重算距离、不新增字段
     */
    private List<OrderDealershipVo> toVoList(List<Dealership> dealershipList) {
        List<OrderDealershipVo> voList = new ArrayList<>(dealershipList.size());
        for (Dealership dealership : dealershipList) {
            if (dealership == null) {
                continue;
            }
            OrderDealershipVo vo = new OrderDealershipVo();
            vo.setCode(dealership.getCode());
            vo.setName(dealership.getName());
            vo.setAddress(dealership.getAddress());
            vo.setDistance(dealership.getDistance());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 提取 Feign 状态码用于日志，不记录响应体
     */
    private Integer feignStatus(Throwable throwable) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < 5; depth++) {
            if (current instanceof FeignException feignException) {
                return feignException.status();
            }
            current = current.getCause();
        }
        return null;
    }
}
