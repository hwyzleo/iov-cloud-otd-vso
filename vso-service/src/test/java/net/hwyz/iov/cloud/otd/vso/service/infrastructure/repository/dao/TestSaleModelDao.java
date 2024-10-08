package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao;

import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelType;
import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 销售车型 DAO 测试类
 *
 * @author hwyz_leo
 */
public class TestSaleModelDao extends BaseTest {

    @Autowired
    private SaleModelDao saleModelDao;

    @Test
    @Order(1)
    @DisplayName("新增一条记录")
    public void testInsertPo() throws Exception {
        SaleModelPo saleModelPo = SaleModelPo.builder()
                .saleCode("H01")
                .saleModelType(SaleModelType.OPTIONAL.name())
                .saleModelTypeCode("X02")
                .saleName("高阶智驾")
                .salePrice("￥3000")
                .saleImage("")
                .saleDesc("")
                .saleParam("")
                .enable(true)
                .sort(0)
                .build();
        saleModelDao.insertPo(saleModelPo);
    }

}
