package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 生产配置无法匹配异常
 *
 * @author VSO Team
 */
@Slf4j
public class BuildConfigNotMatchedException extends VsoBaseException {

    public BuildConfigNotMatchedException(String saleModelCode) {
        super(ERROR_CODE_BUILD_CONFIG_NOT_MATCHED);
        log.warn("销售车型代码[{}]无法匹配到有效的生产配置", saleModelCode);
    }

}