package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * 销售车型配置 DAO 测试类
 *
 * @author hwyz_leo
 */
public class TestSaleModelConfigDao extends BaseTest {

    @Autowired
    private SaleModelConfigDao saleModelConfigDao;

    @Test
    @Order(1)
    @DisplayName("新增一条记录")
    public void testInsertPo() throws Exception {
        SaleModelConfigPo saleModelConfigPo = SaleModelConfigPo.builder()
                .saleCode("H01")
                .type(SaleModelConfigType.OPTIONAL.name())
                .typeCode("XZ02")
                .typeName("高阶智驾")
                .typePrice(BigDecimal.valueOf(20000))
                .typeImage("[]")
                .typeDesc("")
                .typeParam("")
                .enable(true)
                .sort(99)
                .build();
        saleModelConfigDao.insertPo(saleModelConfigPo);
    }

}
