package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseBenefitsPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 销售车型购车权益 DAO 测试类
 *
 * @author hwyz_leo
 */
public class TestPurchaseBenefitsDao extends BaseTest {

    @Autowired
    private PurchaseBenefitsDao purchaseBenefitsDao;

    @Test
    @Order(1)
    @DisplayName("新增一条记录")
    public void testInsertPo() throws Exception {
        PurchaseBenefitsPo purchaseBenefitsPo = PurchaseBenefitsPo.builder()
                .saleCode("H01")
                .startTime(new Date(1727712000000L))
                .endTime(new Date(1735574400000L))
                .intro("创始权益（价值6000元），首年用车服务包（价值999元），5000元选配基金（价值5000元）")
                .detail("创始权益（价值6000元），首年用车服务包（价值999元），5000元选配基金（价值5000元）")
                .enable(true)
                .build();
        purchaseBenefitsDao.insertPo(purchaseBenefitsPo);
    }

}
