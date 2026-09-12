package com.scaffolding.service;

import com.scaffolding.dto.injury.MaterialChecklist;
import com.scaffolding.dto.injury.MaterialRequirement;
import com.scaffolding.entity.InjuryMaterial;
import com.scaffolding.entity.InjuryReport;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工伤材料清单引擎
 * <p>
 * 依据《工伤保险条例》第十八条工伤认定申请需提交的材料（工伤认定申请表、
 * 劳动关系/事实劳动关系证明、医疗诊断证明），结合平台现场处置需要，
 * 拆分为：现场证明（主管）、保险/用工资料（劳务）、医疗票据（共同）。
 *
 * @author scaffolding
 */
@Component
public class MaterialCatalog {

    /** 伤情类型 -> 额外必需材料类型（在通用清单之外按伤情追加） */
    private static final Map<String, List<String>> EXTRA_BY_INJURY_TYPE = new HashMap<>();

    static {
        // 住院需完整病历与费用清单
        EXTRA_BY_INJURY_TYPE.put("hospitalized", Arrays.asList("hospital_record", "payment_voucher"));
        // 疑似伤残需劳动能力鉴定申请
        EXTRA_BY_INJURY_TYPE.put("disability", Arrays.asList("hospital_record", "payment_voucher", "disability_appraisal"));
        // 工亡需死亡/火化证明、供养亲属证明
        EXTRA_BY_INJURY_TYPE.put("death", Arrays.asList("death_cert", "dependent_proof"));
        // 门诊轻伤仅需门诊病历
        EXTRA_BY_INJURY_TYPE.put("outpatient", Collections.singletonList("outpatient_record"));
    }

    /** 材料类型 -> 定义：名称、归属、归属中文名、提示 */
    private static final Map<String, MaterialRequirement> DEFS = new LinkedHashMap<>();

    static {
        put("site_photo", "事故现场照片/视频截图", "supervisor", "现场主管", "拍摄受伤地点、设备、作业面及安全防护情况");
        put("witness_statement", "见证人证言（签字）", "supervisor", "现场主管", "至少2名同班组/在场人员，签字并留联系电话");
        put("accident_report", "事故经过书面报告", "supervisor", "现场主管", "载明时间、地点、原因、经过，项目部盖章");
        put("medical_diagnosis", "医疗诊断证明", "common", "医疗票据", "急诊/首诊诊断证明，医院盖章");
        put("outpatient_record", "门诊病历", "common", "医疗票据", "门诊就诊记录与处置意见");
        put("hospital_record", "住院病历/出院小结", "common", "医疗票据", "住院全套病历、手术记录、出院小结");
        put("medical_receipt", "医疗费用票据/费用清单", "common", "医疗票据", "发票原件及费用明细清单");
        put("payment_voucher", "费用支付凭证", "common", "医疗票据", "垫付/支付凭证，用于理赔核对");
        put("labor_contract", "劳动合同/用工关系证明", "labor", "劳务公司", "劳动合同或考勤、工牌等事实劳动关系证明");
        put("insurance_cert", "参保证明/保单", "labor", "劳务公司", "工伤保险参保证明或项目团意险保单");
        put("id_card", "受伤工人身份证复印件", "labor", "劳务公司", "正反面复印件，工人签字确认");
        put("disability_appraisal", "劳动能力鉴定申请/结论", "labor", "劳务公司", "伤情稳定后向劳鉴委申请");
        put("death_cert", "死亡证明/火化证明", "labor", "劳务公司", "医疗机构或公安部门出具");
        put("dependent_proof", "供养亲属证明", "labor", "劳务公司", "户籍/亲属关系及供养证明");
    }

    private static void put(String type, String name, String owner, String ownerName, String hint) {
        DEFS.put(type, new MaterialRequirement(type, name, owner, ownerName, false, hint));
    }

    /**
     * 根据伤情类型计算必需材料类型集合
     */
    public Set<String> requiredTypes(String injuryType) {
        Set<String> types = new LinkedHashSet<>(Arrays.asList(
                "site_photo", "witness_statement", "accident_report", "medical_diagnosis",
                "medical_receipt", "labor_contract", "insurance_cert", "id_card"));
        if (injuryType != null) {
            List<String> extra = EXTRA_BY_INJURY_TYPE.get(injuryType);
            if (extra != null) {
                types.addAll(extra);
            }
        }
        return types;
    }

    /**
     * 依据已上传材料核对清单
     */
    public MaterialChecklist evaluate(InjuryReport report, List<InjuryMaterial> materials) {
        MaterialChecklist checklist = new MaterialChecklist();
        Set<String> requiredTypes = requiredTypes(report.getInjuryType());

        // 已上传材料按类型分组（同类型只计一次，重复上传视为补拍/补扫）
        Set<String> providedTypes = materials.stream()
                .map(InjuryMaterial::getMaterialType)
                .collect(Collectors.toSet());

        for (Map.Entry<String, MaterialRequirement> entry : DEFS.entrySet()) {
            String type = entry.getKey();
            boolean required = requiredTypes.contains(type);
            if (!required) {
                continue;
            }
            MaterialRequirement item = entry.getValue();
            MaterialRequirement cloned = new MaterialRequirement(
                    item.getMaterialType(), item.getMaterialName(), item.getOwnerRole(),
                    item.getOwnerName(), true, item.getHint());
            boolean provided = providedTypes.contains(type);
            cloned.setProvided(provided);
            checklist.getItems().add(cloned);
            if (provided) {
                checklist.setRequiredProvided(checklist.getRequiredProvided() + 1);
            } else {
                checklist.getMissingNames().add(item.getMaterialName());
                if ("supervisor".equals(item.getOwnerRole())) {
                    checklist.getMissingSite().add(item.getMaterialName());
                } else if ("labor".equals(item.getOwnerRole())) {
                    checklist.getMissingInsurance().add(item.getMaterialName());
                } else {
                    checklist.getMissingMedical().add(item.getMaterialName());
                }
            }
        }
        checklist.setRequiredTotal(requiredTypes.size());
        int progress = checklist.getRequiredTotal() == 0 ? 0
                : checklist.getRequiredProvided() * 100 / checklist.getRequiredTotal();
        checklist.setProgress(progress);
        return checklist;
    }

    /** 材料类型中文名（供上传接口校验） */
    public String nameOf(String materialType) {
        MaterialRequirement def = DEFS.get(materialType);
        return def == null ? materialType : def.getMaterialName();
    }

    /** 材料归属（供上传接口校验） */
    public String ownerOf(String materialType) {
        MaterialRequirement def = DEFS.get(materialType);
        return def == null ? "common" : def.getOwnerRole();
    }

    public boolean exists(String materialType) {
        return DEFS.containsKey(materialType);
    }
}
