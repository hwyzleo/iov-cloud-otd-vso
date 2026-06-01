package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.mapper;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * 销售车型 DAO 测试类
 *
 * @author hwyz_leo
 */
public class TestSaleModelMapper extends BaseTest {

    @Autowired
    private SaleModelMapper saleModelMapper;

    @Test
    @Order(1)
    @DisplayName("新增一条记录")
    public void testInsertPo() throws Exception {
        SaleModelPo saleModelPo = SaleModelPo.builder()
                .saleModelCode("TEST_" + System.currentTimeMillis())
                .modelName("高阶智驾")
                .parameters("{}")
                .images("{}")
                .earnestMoney(true)
                .downPayment(true)
                .enable(true)
                .sort(99)
                .listingStatus("active")
                .build();
        saleModelMapper.insertPo(saleModelPo);
    }

}
