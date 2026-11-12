package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 图片上传凭证无效异常（OTD-VSO-DSN-CR-003 §4.2）
 * <p>
 * 凭证缺失、已过期、操作人不一致或已被消费。
 *
 * @author VSO Team
 */
public class ImageUploadTicketInvalidException extends VsoBaseException {

    public ImageUploadTicketInvalidException(String message) {
        super(VsoErrorCode.IMAGE_UPLOAD_TICKET_INVALID, message);
    }
}
