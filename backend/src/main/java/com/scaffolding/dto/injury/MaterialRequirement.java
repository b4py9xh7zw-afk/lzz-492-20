package com.scaffolding.dto.injury;

import lombok.Data;

/**
 * 材料清单项（用于"还缺哪些证明"提示）
 *
 * @author scaffolding
 */
@Data
public class MaterialRequirement {

    /** 材料类型编码 */
    private String materialType;

    /** 材料名称 */
    private String materialName;

    /** 材料归属：supervisor-现场证明，labor-保险/用工资料，common-医疗票据 */
    private String ownerRole;

    /** 归属中文名 */
    private String ownerName;

    /** 是否已上传 */
    private Boolean provided;

    /** 是否为当前伤情类型必需 */
    private Boolean required;

    /** 提示说明 */
    private String hint;

    public MaterialRequirement() {
    }

    public MaterialRequirement(String materialType, String materialName, String ownerRole,
                               String ownerName, Boolean required, String hint) {
        this.materialType = materialType;
        this.materialName = materialName;
        this.ownerRole = ownerRole;
        this.ownerName = ownerName;
        this.required = required;
        this.hint = hint;
    }
}
