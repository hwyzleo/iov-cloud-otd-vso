package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementConfirmationRequiredException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementVersionConflictException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreement;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreementStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAgreementConfirmationRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PurchaseAgreementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 购车协议测试（US-001 / §14：GLOBAL 回退、车型优先、有效期、多协议确认）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("购车协议 - 车型优先/有效期/版本确认/多协议")
class PurchaseAgreementAppServiceTest {

    @Mock
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Mock
    private OrderAgreementConfirmationRepository confirmationRepository;

    @InjectMocks
    private PurchaseAgreementAppService purchaseAgreementAppService;

    private PurchaseAgreement globalAgreement;
    private PurchaseAgreement modelAgreement;
    private PurchaseAgreement financeAgreement;

    @BeforeEach
    void setUp() {
        globalAgreement = agreement("AG-GLOBAL", "VEHICLE_PURCHASE", "GLOBAL", 1);
        modelAgreement = agreement("AG-MODEL", "VEHICLE_PURCHASE", "SM001", 2);
        financeAgreement = agreement("AG-FIN", "FINANCE_SERVICE", "SM001", 1);
    }

    @Test
    @DisplayName("车型专属协议优先于 GLOBAL（同代码取最高版本）")
    void 车型协议优先() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM001"), any()))
                .thenReturn(List.of(modelAgreement, globalAgreement));

        List<PurchaseAgreementResult> result = purchaseAgreementAppService.listActiveAgreements("SM001");

        assertEquals(1, result.size());
        assertEquals("AG-MODEL", result.get(0).getAgreementId());
        assertEquals(2, result.get(0).getVersionNo());
    }

    @Test
    @DisplayName("无车型专属协议时回退 GLOBAL")
    void 回退全局协议() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM999"), any()))
                .thenReturn(List.of(globalAgreement));

        List<PurchaseAgreementResult> result = purchaseAgreementAppService.listActiveAgreements("SM999");

        assertEquals(1, result.size());
        assertEquals("GLOBAL", result.get(0).getSaleModelCode());
    }

    @Test
    @DisplayName("协议有效期：未生效/已过期均视为无效")
    void 有效期过滤() {
        LocalDateTime now = LocalDateTime.now();
        PurchaseAgreement future = PurchaseAgreement.builder()
                .agreementCode("VEHICLE_PURCHASE").versionNo(1)
                .agreementStatus(PurchaseAgreementStatus.ACTIVE)
                .effectiveFrom(now.plusDays(1)).build();
        PurchaseAgreement expired = PurchaseAgreement.builder()
                .agreementCode("VEHICLE_PURCHASE").versionNo(1)
                .agreementStatus(PurchaseAgreementStatus.ACTIVE)
                .effectiveTo(now.minusDays(1)).build();
        PurchaseAgreement draft = PurchaseAgreement.builder()
                .agreementCode("VEHICLE_PURCHASE").versionNo(1)
                .agreementStatus(PurchaseAgreementStatus.DRAFT).build();

        assertTrue(!future.isValid(now));
        assertTrue(!expired.isValid(now));
        assertTrue(!draft.isValid(now));
        assertTrue(globalAgreement.isValid(now));
    }

    @Test
    @DisplayName("多协议确认：保存全部快照且版本一致")
    void 多协议确认生成快照() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM001"), any()))
                .thenReturn(List.of(modelAgreement, financeAgreement));

        List<OrderAgreementConfirmation> confirmations = purchaseAgreementAppService.buildConfirmations(
                "OID1", "SM001", List.of(
                        AgreementConfirmationCmd.builder().agreementCode("VEHICLE_PURCHASE").versionNo(2).build(),
                        AgreementConfirmationCmd.builder().agreementCode("FINANCE_SERVICE").versionNo(1).build()));

        assertEquals(2, confirmations.size());
        assertTrue(confirmations.stream().allMatch(c -> "OID1".equals(c.getOrderId())));
        assertTrue(confirmations.stream().anyMatch(c -> "AG-MODEL".equals(c.getAgreementId())));
        assertTrue(confirmations.stream().anyMatch(c -> "AG-FIN".equals(c.getAgreementId())));
        assertNotNull(confirmations.get(0).getConfirmTime());
    }

    @Test
    @DisplayName("缺少必确认协议：拒绝下单")
    void 缺少必确认协议_拒绝() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM001"), any()))
                .thenReturn(List.of(modelAgreement, financeAgreement));

        assertThrows(AgreementConfirmationRequiredException.class, () ->
                purchaseAgreementAppService.buildConfirmations("OID1", "SM001",
                        List.of(AgreementConfirmationCmd.builder()
                                .agreementCode("VEHICLE_PURCHASE").versionNo(2).build())));
    }

    @Test
    @DisplayName("协议重复确认：拒绝")
    void 重复确认_拒绝() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM001"), any()))
                .thenReturn(List.of(modelAgreement));

        assertThrows(AgreementConfirmationRequiredException.class, () ->
                purchaseAgreementAppService.buildConfirmations("OID1", "SM001", List.of(
                        AgreementConfirmationCmd.builder().agreementCode("VEHICLE_PURCHASE").versionNo(2).build(),
                        AgreementConfirmationCmd.builder().agreementCode("VEHICLE_PURCHASE").versionNo(2).build())));
    }

    @Test
    @DisplayName("版本不匹配：拒绝")
    void 版本不匹配_拒绝() {
        when(purchaseAgreementRepository.findActiveBySaleModel(eq("SM001"), any()))
                .thenReturn(List.of(modelAgreement));

        assertThrows(AgreementNotExistException.class, () ->
                purchaseAgreementAppService.buildConfirmations("OID1", "SM001",
                        List.of(AgreementConfirmationCmd.builder()
                                .agreementCode("VEHICLE_PURCHASE").versionNo(99).build())));
    }

    @Test
    @DisplayName("后台维护：保存协议并校验必填")
    void 后台保存协议() {
        purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                .agreementCode("VEHICLE_PURCHASE")
                .versionNo(1)
                .title("购车协议")
                .detail("条款")
                .agreementStatus("active")
                .build());

        ArgumentCaptor<PurchaseAgreement> captor = ArgumentCaptor.forClass(PurchaseAgreement.class);
        verify(purchaseAgreementRepository).save(captor.capture());
        assertEquals("GLOBAL", captor.getValue().getSaleModelCode());
        assertEquals(PurchaseAgreementStatus.ACTIVE, captor.getValue().getAgreementStatus());

        assertThrows(AgreementNotExistException.class, () ->
                purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                        .agreementCode("X").versionNo(1).title("t").detail("d").agreementStatus("unknown").build()));
        verify(purchaseAgreementRepository, never()).delete(any());
    }

    @Test
    @DisplayName("后台下架：状态置为 off_shelf")
    void 后台下架协议() {
        when(purchaseAgreementRepository.findByAgreementId("AG-MODEL")).thenReturn(java.util.Optional.of(modelAgreement));

        purchaseAgreementAppService.offShelf("AG-MODEL");

        ArgumentCaptor<PurchaseAgreement> captor = ArgumentCaptor.forClass(PurchaseAgreement.class);
        verify(purchaseAgreementRepository).save(captor.capture());
        assertEquals(PurchaseAgreementStatus.OFF_SHELF, captor.getValue().getAgreementStatus());
    }

    @Test
    @DisplayName("同一车型协议版本重复：返回业务错误而非数据库异常")
    void 协议版本重复预校验() {
        PurchaseAgreement existing = agreement("AG-EXIST", "VEHICLE_PURCHASE", "SM001", 1);
        when(purchaseAgreementRepository.search("SM001", "VEHICLE_PURCHASE", null))
                .thenReturn(List.of(existing));

        AgreementVersionConflictException ex = assertThrows(AgreementVersionConflictException.class, () ->
                purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                        .agreementCode("VEHICLE_PURCHASE")
                        .saleModelCode("SM001")
                        .versionNo(1)
                        .title("购车协议")
                        .detail("detail")
                        .agreementStatus("active")
                        .build()));
        assertTrue(ex.getMessage().contains("已存在"));
        verify(purchaseAgreementRepository, never()).save(any());
    }

    @Test
    @DisplayName("并发下唯一键冲突：DuplicateKeyException 转为协议版本冲突业务错误")
    void 协议版本唯一键冲突转业务错误() {
        when(purchaseAgreementRepository.search(anyString(), anyString(), any())).thenReturn(List.of());
        when(purchaseAgreementRepository.save(any(PurchaseAgreement.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException(
                        "Duplicate entry for key 'uk_purchase_agreement_version'"));

        assertThrows(AgreementVersionConflictException.class, () ->
                purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                        .agreementCode("VEHICLE_PURCHASE")
                        .saleModelCode("SM001")
                        .versionNo(2)
                        .title("购车协议")
                        .detail("detail")
                        .agreementStatus("active")
                        .build()));
    }

    @Test
    @DisplayName("更新同一条协议（同版本同 agreementId）不应判定为冲突")
    void 更新同一条协议不算冲突() {
        PurchaseAgreement existing = agreement("AG-SAME", "VEHICLE_PURCHASE", "SM001", 1);
        when(purchaseAgreementRepository.search("SM001", "VEHICLE_PURCHASE", null))
                .thenReturn(List.of(existing));

        String agreementId = purchaseAgreementAppService.save(SavePurchaseAgreementCmd.builder()
                .agreementId("AG-SAME")
                .agreementCode("VEHICLE_PURCHASE")
                .saleModelCode("SM001")
                .versionNo(1)
                .title("购车协议（改标题）")
                .detail("detail")
                .agreementStatus("active")
                .build());

        assertEquals("AG-SAME", agreementId);
        verify(purchaseAgreementRepository).save(any(PurchaseAgreement.class));
    }

    private PurchaseAgreement agreement(String id, String code, String saleModel, int version) {
        return PurchaseAgreement.builder()
                .agreementId(id)
                .agreementCode(code)
                .saleModelCode(saleModel)
                .versionNo(version)
                .title(code + " v" + version)
                .intro("intro")
                .detail("detail")
                .agreementStatus(PurchaseAgreementStatus.ACTIVE)
                .effectiveFrom(LocalDateTime.now().minusDays(1))
                .build();
    }
}
