package net.hwyz.iov.cloud.otd.vso.service.adapter.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BindingFactsParamException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Web 入参绑定异常处理器（OTD-VSO-DSN-CR-013，OTD-VSO-DSN-CR-017 增加请求体/绑车查询参数）
 * <p>
 * 框架 {@code GlobalExceptionHandler} 只兜底 {@link Exception}，会把“缺少必填参数 / 参数类型不合法”
 * 统一报成 {@code 100002 系统内部错误} + HTTP 500，调用方无法区分“请求写错”和“服务故障”。
 * 这里对入参类异常前置处理，返回 HTTP 400 + VSO 参数校验错误码。
 * <p>
 * 类名带 {@code Vso} 前缀是必需的：framework-web-starter 自 0.0.1-SNAPSHOT(2026-11-24) 起
 * 在 {@code WebAutoConfiguration} 中以 {@code webRequestExceptionHandler} 为名注册了同职责 Bean，
 * 若本类沿用 {@code WebRequestExceptionHandler} 简名，两者 Bean 名相同，在
 * {@code spring.main.allow-bean-definition-overriding=false}（默认）下会导致
 * {@code BeanDefinitionOverrideException}，服务无法启动。
 *
 * @author VSO Team
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class VsoWebRequestExceptionHandler {

    /**
     * 缺少必填请求参数，例如内部服务调用 {@code /order/list} 时未传 {@code orderState}
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        String detail = "缺少必填参数: " + e.getParameterName();
        log.warn("请求参数缺失: {}", detail);
        return badRequest(detail);
    }

    /**
     * 请求参数类型不匹配，例如数字型参数传了非数字
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String detail = "参数类型不合法: " + e.getName();
        log.warn("请求参数类型不匹配: {}", detail);
        return badRequest(detail);
    }

    /**
     * 请求体校验失败（例如绑车事实查询缺少账号或 VIN）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("请求体校验失败");
        log.warn("请求体校验失败: {}", detail);
        return badRequest(detail);
    }

    /**
     * 绑车事实查询参数非法（VIN 格式、账号长度等，OTD-VSO-DSN-CR-017）
     */
    @ExceptionHandler(BindingFactsParamException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindingFactsParam(BindingFactsParamException e) {
        log.warn("绑车事实查询参数非法: {}", e.getMessage());
        return badRequest(e.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> badRequest(String detail) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(VsoErrorCode.INVALID_PARAM, detail));
    }
}
