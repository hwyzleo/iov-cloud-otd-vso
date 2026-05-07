package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 销售车型配置排序 DTO。
 */
@Data
public class SaleModelConfigSortDto {

    /**
     * 特征族排序列表
     */
    private List<FamilySortItem> families;

    @Data
    public static class FamilySortItem {
        /**
         * 特征族ID
         */
        private Long familyId;

        /**
         * 特征族排序
         */
        private Integer sort;

        /**
         * 特征值排序列表
         */
        private List<FeatureSortItem> features;
    }

    @Data
    public static class FeatureSortItem {
        /**
         * 特征值ID
         */
        private Long featureId;

        /**
         * 特征值排序
         */
        private Integer sort;
    }
}