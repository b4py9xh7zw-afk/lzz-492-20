package com.scaffolding.dto.injury;

import lombok.Data;

import java.time.LocalDate;

/**
 * 复工/停工结论请求
 *
 * @author scaffolding
 */
@Data
public class ConclusionRequest {

    /** 结论：stop-停工，resume-复工 */
    private String conclusion;

    /** 停工开始日期（stop） */
    private LocalDate stopStartDate;

    /** 预计复工日期（stop） */
    private LocalDate expectedResumeDate;

    /** 实际复工日期（resume） */
    private LocalDate actualResumeDate;

    /** 结论说明（医嘱/复工条件） */
    private String conclusionRemark;
}
