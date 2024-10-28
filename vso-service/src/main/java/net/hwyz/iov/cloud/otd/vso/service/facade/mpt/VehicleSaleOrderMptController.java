package net.hwyz.iov.cloud.otd.vso.service.facade.mpt;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.api.feign.mpt.VehicleSaleOrderMptApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleSaleOrderAppService;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.MptAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.util.ParamHelper;
import org.springframework.web.bind.annotation.*;

/**
 * 车辆销售订单相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/vehicleSaleOrder")
public class VehicleSaleOrderMptController implements VehicleSaleOrderMptApi {

    private final VehicleSaleOrderAppService vehicleSaleOrderAppService;

    /**
     * 分配交付人员
     *
     * @param request 分配交付人员请求
     */
    @Override
    @PostMapping("/order/action/assignDeliveryPerson")
    public void assignDeliveryPerson(@RequestBody @Valid AssignDeliveryPersonRequest request, @RequestHeader(required = false) MptAccount mptAccount) {
        logger.info("管理后台用户[{}]分配交付人员", ParamHelper.getMptAccountInfo(mptAccount));
        vehicleSaleOrderAppService.assignDeliveryPerson(request.getOrderNum(), request);
    }

    /**
     * 分配车辆
     *
     * @param request 分配车辆请求
     */
    @Override
    @PostMapping("/order/action/assignVehicle")
    public void assignVehicle(@RequestBody @Valid AssignVehicleRequest request, @RequestHeader(required = false) MptAccount mptAccount) {
        logger.info("管理后台用户[{}]分配车辆", ParamHelper.getMptAccountInfo(mptAccount));
        vehicleSaleOrderAppService.assignVehicle(request.getOrderNum(), request);
    }

}
