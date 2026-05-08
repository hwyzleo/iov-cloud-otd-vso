package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.WishlistVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.WishlistAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 手机端心愿单控制器
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/mobile/wishlist/v1")
public class MobileWishlistController extends BaseController {

    private final WishlistAppService wishlistAppService;

    @GetMapping("/list")
    public ApiResponse<List<WishlistListVo>> getWishlistList() {
        log.info("手机客户端[{}]获取心愿单列表", ParamHelper.getClientAccountInfo());
        List<WishlistListResult> results = wishlistAppService.getWishlistList(SecurityContextHolder.getUserId());
        return ApiResponse.ok(WishlistVoAssembler.INSTANCE.toVoList(results));
    }

    @PostMapping("/action/create")
    public ApiResponse<String> createWishlist(@RequestBody @Valid CreateWishlistRequestVo request) {
        log.info("手机客户端[{}]创建心愿单", ParamHelper.getClientAccountInfo());
        CreateWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toCreateWishlistCmd(SecurityContextHolder.getUserId(), request);
        String wishlistId = wishlistAppService.createWishlist(cmd);
        return ApiResponse.ok(wishlistId);
    }

    @PostMapping("/action/modify")
    public ApiResponse<Void> modifyWishlist(@RequestBody @Valid ModifyWishlistRequestVo request) {
        log.info("手机客户端[{}]修改心愿单[{}]", ParamHelper.getClientAccountInfo(), request.getWishlistId());
        ModifyWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toModifyWishlistCmd(SecurityContextHolder.getUserId(), request);
        wishlistAppService.modifyWishlist(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/action/delete")
    public ApiResponse<Void> deleteWishlist(@RequestBody @Valid DeleteWishlistRequestVo request) {
        log.info("手机客户端[{}]删除心愿单[{}]", ParamHelper.getClientAccountInfo(), request.getWishlistId());
        DeleteWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toDeleteWishlistCmd(SecurityContextHolder.getUserId(), request);
        wishlistAppService.deleteWishlist(cmd);
        return ApiResponse.ok();
    }

    @GetMapping("/{wishlistId}")
    public ApiResponse<WishlistDetailVo> getWishlist(@PathVariable String wishlistId) {
        log.info("手机客户端[{}]获取心愿单[{}]详情", ParamHelper.getClientAccountInfo(), wishlistId);
        WishlistDetailResult result = wishlistAppService.getWishlistDetail(SecurityContextHolder.getUserId(), wishlistId);
        WishlistDetailVo vo = WishlistVoAssembler.INSTANCE.toDetailVo(result);
        return ApiResponse.ok(vo);
    }

}