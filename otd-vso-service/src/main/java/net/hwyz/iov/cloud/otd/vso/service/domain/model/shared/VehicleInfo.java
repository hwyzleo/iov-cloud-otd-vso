package net.hwyz.iov.cloud.otd.vso.service.domain.model.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 车辆信息值对象
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class VehicleInfo {

    /**
     * 车型编码
     */
    private String modelCode;

    /**
     * 车型名称
     */
    private String modelName;

    /**
     * 配置编码
     */
    private String configCode;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 颜色编码
     */
    private String colorCode;

    /**
     * 颜色名称
     */
    private String colorName;

    /**
     * VIN（车辆识别码）
     */
    private String vin;

    public VehicleInfo(String modelCode, String modelName, String configCode, String configName, 
                      String colorCode, String colorName) {
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.configCode = configCode;
        this.configName = configName;
        this.colorCode = colorCode;
        this.colorName = colorName;
    }

    /**
     * 验证车辆信息完整性
     */
    public boolean isComplete() {
        return modelCode != null && configCode != null && colorCode != null;
    }

    /**
     * 是否与另一个车辆信息相同
     */
    public boolean isSameAs(VehicleInfo other) {
        if (other == null) {
            return false;
        }
        return this.modelCode.equals(other.modelCode) 
            && this.configCode.equals(other.configCode) 
            && this.colorCode.equals(other.colorCode);
    }

    /**
     * 获取图片URL列表
     */
    public java.util.List<String> getImageUrls() {
        return new java.util.ArrayList<>();
    }

    /**
     * 获取描述
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (modelName != null) {
            sb.append(modelName);
        }
        if (configName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(configName);
        }
        if (colorName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(colorName);
        }
        return sb.toString();
    }

}
