package net.hwyz.iov.cloud.otd.vso.service.facade.mp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.SaleModelResponse;
import net.hwyz.iov.cloud.otd.vso.api.feign.mp.SaleModelMpApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;
import net.hwyz.iov.cloud.tsp.framework.commons.util.ParamHelper;
import org.springframework.web.bind.annotation.*;

/**
 * 销售车型相关手机接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mp/saleModel")
public class SaleModelMpController implements SaleModelMpApi {

    private final SaleModelAppService saleModelAppService;

    /**
     * 获取销售车型列表
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型列表
     */
    @Override
    @GetMapping("/{saleCode}")
    public Response<SaleModelResponse> getSaleModelList(@PathVariable("saleCode") String saleCode,
                                                        @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售代码[{}]销售车型列表", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        return new Response<>(saleModelAppService.getSaleModelResponse(saleCode));
    }
}
