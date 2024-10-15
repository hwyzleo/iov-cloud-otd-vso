package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseAgreementPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 销售车型购车协议 DAO 测试类
 *
 * @author hwyz_leo
 */
public class TestPurchaseAgreementDao extends BaseTest {

    @Autowired
    private PurchaseAgreementDao purchaseAgreementDao;

    @Test
    @Order(1)
    @DisplayName("新增一条记录")
    public void testInsertPo() throws Exception {
        PurchaseAgreementPo purchaseAgreementPo = PurchaseAgreementPo.builder()
                .saleCode("H01")
                .type(1)
                .title("意向金协议")
                .intro("创始权益（价值6000元），首年用车服务包（价值999元），5000元选配基金（价值5000元）")
                .detail("创始权益（价值6000元），首年用车服务包（价值999元），5000元选配基金（价值5000元）")
                .enable(true)
                .build();
        purchaseAgreementDao.insertPo(purchaseAgreementPo);
    }

}
