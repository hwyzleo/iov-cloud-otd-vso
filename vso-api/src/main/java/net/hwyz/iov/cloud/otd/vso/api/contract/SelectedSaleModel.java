package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 已选择的销售车型
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedSaleModel extends SaleModel {

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

    /**
     * 车型图片集
     */
    private List<String> modelImages;

    /**
     * 车型描述
     */
    private String modelDesc;

}
