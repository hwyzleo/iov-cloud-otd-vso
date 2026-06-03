package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WishlistMapperTest extends BaseTest {

    @Autowired
    private WishlistMapper wishlistMapper;

    @Test
    @Order(1)
    @DisplayName("根据用户ID物理删除心愿单")
    public void testDeleteByUserId() {
        String userId = "test_user_" + System.currentTimeMillis();
        
        WishlistPo po = WishlistPo.builder()
                .wishlistId("wishlist_" + System.currentTimeMillis())
                .userId(userId)
                .saleModelCode("TEST_SALE_CODE")
                .status("ACTIVE")
                .build();
        
        wishlistMapper.insertPo(po);
        
        List<WishlistPo> inserted = wishlistMapper.selectByUserId(userId);
        assertFalse(inserted.isEmpty(), "心愿单应该被插入成功");
        
        wishlistMapper.deleteByUserId(userId);
        
        List<WishlistPo> afterDelete = wishlistMapper.selectByUserId(userId);
        assertTrue(afterDelete.isEmpty(), "心愿单应该被物理删除");
    }

}