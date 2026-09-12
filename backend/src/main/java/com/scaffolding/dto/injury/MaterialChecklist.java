package com.scaffolding.dto.injury;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 材料清单核对结果
 *
 * @author scaffolding
 */
@Data
public class MaterialChecklist {

    /** 必需材料总数 */
    private int requiredTotal;

    /** 已上传必需材料数 */
    private int requiredProvided;

    /** 完成度 0-100 */
    private int progress;

    /** 材料清单 */
    private List<MaterialRequirement> items = new ArrayList<>();

    /** 缺失材料名称列表（用于消息提醒） */
    private List<String> missingNames = new ArrayList<>();

    /** 现场证明缺失（主管负责） */
    private List<String> missingSite = new ArrayList<>();

    /** 保险/用工资料缺失（劳务负责） */
    private List<String> missingInsurance = new ArrayList<>();

    /** 医疗票据缺失（共同补齐） */
    private List<String> missingMedical = new ArrayList<>();
}
