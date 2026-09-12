package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工伤证明材料实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_material")
public class InjuryMaterial extends BaseEntity {

    /** 工伤上报单ID */
    private Long reportId;

    /** 材料类型编码 */
    private String materialType;

    /** 材料名称 */
    private String materialName;

    /** 存储文件名 */
    private String fileName;

    /** 原始文件名 */
    private String originalName;

    /** 相对存储路径 */
    private String filePath;

    /** 访问URL */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 扩展名 */
    private String fileExt;

    /** 材料归属（supervisor-现场证明，labor-保险资料，common-医疗票据） */
    private String ownerRole;

    /** 上传人ID */
    private Long uploadUserId;

    /** 上传人姓名 */
    private String uploadUserName;

    /** 备注 */
    private String remark;
}
