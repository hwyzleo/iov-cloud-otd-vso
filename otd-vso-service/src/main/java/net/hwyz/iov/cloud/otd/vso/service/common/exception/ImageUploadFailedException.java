package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 图片上传申请或完成确认失败异常（OTD-VSO-DSN-CR-003 §4.1/§4.2）
 * <p>
 * 用于 EDD-OSS 调用失败、Redis 绑定不可用、对象状态未达 UPLOADED 等场景，禁止返回半成品地址。
 *
 * @author VSO Team
 */
public class ImageUploadFailedException extends VsoBaseException {

    public ImageUploadFailedException(String message) {
        super(VsoErrorCode.IMAGE_UPLOAD_FAILED, message);
    }
}
