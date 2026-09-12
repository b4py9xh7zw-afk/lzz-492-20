import request from '@/utils/request'

/**
 * 工伤上报材料包 API
 */

// 排班 / 项目 / 工人基础数据
export const scheduleApi = {
  projects() {
    return request({ url: '/schedule/projects', method: 'get' })
  },
  workers(params) {
    return request({ url: '/schedule/workers', method: 'get', params })
  },
  list(params) {
    return request({ url: '/schedule/list', method: 'get', params })
  },
  events(id) {
    return request({ url: `/schedule/${id}/events`, method: 'get' })
  }
}

// 工伤上报主流程
export const injuryApi = {
  // 主管手机端新建/暂存/提交
  saveReport(data) {
    return request({ url: '/injury/report', method: 'post', data })
  },
  page(params) {
    return request({ url: '/injury/page', method: 'get', params })
  },
  detail(id) {
    return request({ url: `/injury/${id}`, method: 'get' })
  },
  checklist(id) {
    return request({ url: `/injury/${id}/checklist`, method: 'get' })
  },
  // 劳务补充保险资料
  saveInsurance(id, data) {
    return request({ url: `/injury/${id}/insurance`, method: 'post', data })
  },
  // 复工/停工结论
  conclude(id, data) {
    return request({ url: `/injury/${id}/conclusion`, method: 'post', data })
  }
}

// 证明材料
export const materialApi = {
  upload(file, { reportId, materialType, remark }) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('reportId', reportId)
    formData.append('materialType', materialType)
    if (remark) formData.append('remark', remark)
    return request({
      url: '/injury/material/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  remove(id) {
    return request({ url: `/injury/material/${id}`, method: 'delete' })
  },
  previewUrl(id) {
    return `${import.meta.env.VITE_API_BASE_URL || '/api'}/injury/material/preview/${id}`
  }
}

// 材料类型中文名（与后端 MaterialCatalog 对应）
export const MATERIAL_TYPES = {
  site_photo: { name: '事故现场照片', owner: 'supervisor' },
  witness_statement: { name: '见证人证言（签字）', owner: 'supervisor' },
  accident_report: { name: '事故经过书面报告', owner: 'supervisor' },
  medical_diagnosis: { name: '医疗诊断证明', owner: 'common' },
  outpatient_record: { name: '门诊病历', owner: 'common' },
  hospital_record: { name: '住院病历/出院小结', owner: 'common' },
  medical_receipt: { name: '医疗费用票据/清单', owner: 'common' },
  payment_voucher: { name: '费用支付凭证', owner: 'common' },
  labor_contract: { name: '劳动合同/用工关系证明', owner: 'labor' },
  insurance_cert: { name: '参保证明/保单', owner: 'labor' },
  id_card: { name: '工人身份证复印件', owner: 'labor' },
  disability_appraisal: { name: '劳动能力鉴定申请/结论', owner: 'labor' },
  death_cert: { name: '死亡证明/火化证明', owner: 'labor' },
  dependent_proof: { name: '供养亲属证明', owner: 'labor' }
}

export const INJURY_TYPE_OPTIONS = [
  { value: 'outpatient', label: '门诊/轻伤' },
  { value: 'hospitalized', label: '住院' },
  { value: 'disability', label: '疑似伤残' },
  { value: 'death', label: '工亡' }
]

export const SHIFT_TEXT = { day: '白班', middle: '中班', night: '夜班' }
export const STATUS_TEXT = {
  draft: '待提交',
  reported: '已上报',
  submitted: '材料齐',
  concluded: '已结论'
}
export const CONCLUSION_TEXT = { stop: '停工', resume: '复工' }
export const CLAIM_STATUS_TEXT = {
  not_filed: '未报案',
  reported: '已报案',
  claiming: '理赔中',
  paid: '已赔付',
  rejected: '拒赔'
}
