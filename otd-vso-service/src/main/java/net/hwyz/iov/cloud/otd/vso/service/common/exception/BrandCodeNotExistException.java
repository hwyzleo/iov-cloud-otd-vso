package net.hwyz.iov.cloud.otd.vso.service.common.exception;

public class BrandCodeNotExistException extends VsoBaseException {

    public BrandCodeNotExistException(String buildConfigCode) {
        super("品牌代码不存在，生产配置代码: " + buildConfigCode);
    }

}