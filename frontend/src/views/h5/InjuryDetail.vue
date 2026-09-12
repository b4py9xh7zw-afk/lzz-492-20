<template>
  <div v-if="detail" class="pb-8">
    <!-- 状态头 -->
    <van-notice-bar
      v-if="detail.checklist && detail.checklist.missingNames.length"
      left-icon="warning-o"
      :text="`还缺 ${detail.checklist.missingNames.length} 份材料：${detail.checklist.missingNames.join('、')}`"
    />
    <van-notice-bar
      v-else
      left-icon="passed"
      color="#07c160"
      background="#e8f9f0"
      text="必需材料已齐全，可提交工伤认定申请"
    />

    <!-- 主信息卡 -->
    <van-cell-group inset title="上报信息" class="mt-3">
      <van-cell title="上报单号" :value="detail.report.reportNo" />
      <van-cell title="状态">
        <template #value>
          <van-tag :type="statusTagType(detail.report.reportStatus)">{{ STATUS_TEXT[detail.report.reportStatus] }}</van-tag>
          <van-tag v-if="detail.report.conclusion"
                   :type="detail.report.conclusion === 'stop' ? 'danger' : 'success'" plain class="ml-1">
            {{ CONCLUSION_TEXT[detail.report.conclusion] }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="项目" :value="detail.project?.projectName" />
      <van-cell title="工人/岗位" :value="`${detail.report.workerName} / ${detail.report.postName || '-'}`" />
      <van-cell title="班次" :value="SHIFT_TEXT[detail.report.shiftName] || '-'" />
      <van-cell title="受伤时间" :value="detail.report.injuryTime" />
      <van-cell title="受伤地点" :label="detail.report.injuryLocation" />
      <van-cell title="伤情类型" :value="typeText(detail.report.injuryType)" />
      <van-cell v-if="detail.report.hospital" title="送医医院" :label="detail.report.hospital" />
      <van-cell v-if="detail.report.hospitalTime" title="送医时间" :value="detail.report.hospitalTime" />
      <van-cell v-if="detail.report.injuryDesc" title="受伤经过" :label="detail.report.injuryDesc" />
      <van-cell title="上报主管" :value="`${detail.report.reporterName || ''} ${detail.report.reportTime || ''}`" />
    </van-cell-group>

    <!-- 见证人 -->
    <van-cell-group inset title="见证人" class="mt-3">
      <van-cell
        v-for="(w, i) in detail.witnesses"
        :key="w.id || i"
        :title="`${w.witnessName}（${witnessTypeText(w.witnessType)}）`"
        :label="`${w.witnessPhone || ''}${w.statement ? '｜' + w.statement : ''}`"
      />
      <van-cell v-if="!detail.witnesses || !detail.witnesses.length" title="暂无见证人" />
    </van-cell-group>

    <!-- 材料缺件清单 -->
    <van-cell-group inset title="材料清单（平台自动核对）" class="mt-3">
      <div class="px-4 pt-2">
        <van-progress :percentage="detail.checklist.progress"
                      :color="detail.checklist.progress === 100 ? '#07c160' : '#1989fa'" />
        <div class="text-xs text-gray-500 mt-1">
          已交 {{ detail.checklist.requiredProvided }}/{{ detail.checklist.requiredTotal }}
        </div>
      </div>

      <!-- 按归属分组提示 -->
      <div v-if="detail.checklist.missingSite.length" class="missing-tip supervisor">
        <b>现场主管待补：</b>{{ detail.checklist.missingSite.join('、') }}
      </div>
      <div v-if="detail.checklist.missingInsurance.length" class="missing-tip labor">
        <b>劳务公司待补：</b>{{ detail.checklist.missingInsurance.join('、') }}
      </div>
      <div v-if="detail.checklist.missingMedical.length" class="missing-tip medical">
        <b>医疗票据待补：</b>{{ detail.checklist.missingMedical.join('、') }}
      </div>

      <van-cell
        v-for="item in detail.checklist.items"
        :key="item.materialType"
        :title="item.materialName"
        :label="`${item.ownerName}｜${item.hint}`"
      >
        <template #right-icon>
          <van-tag :type="item.provided ? 'success' : 'danger'" plain>
            {{ item.provided ? '已交' : '缺失' }}
          </van-tag>
          <van-uploader
            v-if="canUpload && canUploadOwner(item.ownerRole)"
            :after-read="(f) => afterReadMaterial(f, item.materialType)"
            accept="image/*,.pdf"
            class="ml-2"
          />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 已上传材料 -->
    <van-cell-group inset title="已上传材料" class="mt-3">
      <van-swipe-cell v-for="m in detail.materials" :key="m.id">
        <van-cell :title="m.materialName" :label="`${m.uploadUserName || ''} 上传于 ${(m.createTime || '').slice(0,16)}`"
                  is-link @click="preview(m)" />
        <template #right>
          <van-button square type="danger" text="删除" class="h-full"
                      :disabled="!canUpload" @click="removeMaterial(m)" />
        </template>
      </van-swipe-cell>
      <van-cell v-if="!detail.materials || !detail.materials.length" title="暂无材料" />
    </van-cell-group>

    <!-- 劳务保险资料 -->
    <van-cell-group inset title="保险资料（劳务公司补充）" class="mt-3">
      <van-field v-model="insurance.insuranceCompany" label="承保机构" placeholder="保险公司/社保经办机构"
                 :readonly="!canEditInsurance" />
      <van-field v-model="insurance.policyNo" label="保单号" placeholder="保单号/社保电脑号"
                 :readonly="!canEditInsurance" />
      <van-field v-model="insurance.insuredName" label="参保人" :readonly="!canEditInsurance" />
      <van-field label="保障期间" :readonly="!canEditInsurance">
        <template #input>
          <span class="text-sm">{{ insurance.coverageStartDate || '____' }} ~ {{ insurance.coverageEndDate || '____' }}</span>
        </template>
      </van-field>
      <van-field label="起保日期" :readonly="!canEditInsurance">
        <template #input>
          <input v-if="canEditInsurance" type="date" v-model="insurance.coverageStartDate" class="date-input" />
          <span v-else>{{ insurance.coverageStartDate || '-' }}</span>
        </template>
      </van-field>
      <van-field label="终止日期" :readonly="!canEditInsurance">
        <template #input>
          <input v-if="canEditInsurance" type="date" v-model="insurance.coverageEndDate" class="date-input" />
          <span v-else>{{ insurance.coverageEndDate || '-' }}</span>
        </template>
      </van-field>
      <van-field label="理赔状态" :readonly="!canEditInsurance">
        <template #input>
          <van-dropdown-menu v-if="canEditInsurance" class="inline-dropdown">
            <van-dropdown-item v-model="insurance.claimStatus" :options="claimOptions" />
          </van-dropdown-menu>
          <span v-else>{{ CLAIM_STATUS_TEXT[detail.insurance?.claimStatus] || '未报案' }}</span>
        </template>
      </van-field>
      <van-field v-model="insurance.claimNo" label="报案号" placeholder="理赔受理号"
                 :readonly="!canEditInsurance" />
      <van-field v-model="insurance.contactName" label="经办人" :readonly="!canEditInsurance" />
      <van-field v-model="insurance.contactPhone" label="经办电话" type="tel"
                 :readonly="!canEditInsurance" />
      <van-field v-model="insurance.remark" type="textarea" label="备注" rows="2" autosize
                 :readonly="!canEditInsurance" />
      <div v-if="canEditInsurance" class="px-4 py-3">
        <van-button round block type="primary" plain :loading="savingInsurance" @click="saveInsurance">
          保存保险资料
        </van-button>
      </div>
      <van-cell v-if="!canEditInsurance && !detail.insurance" title="劳务公司暂未填写保险资料" />
    </van-cell-group>

    <!-- 复工/停工结论 -->
    <van-cell-group inset title="复工 / 停工结论" class="mt-3">
      <van-cell v-if="detail.report.conclusion" :title="`当前结论：${CONCLUSION_TEXT[detail.report.conclusion]}`"
                :label="`${detail.report.concludedBy || ''} ${detail.report.concludedTime || ''}`" />
      <van-cell v-if="detail.report.conclusionRemark" :title="'结论说明'" :label="detail.report.conclusionRemark" />

      <template v-if="canConclude">
        <van-field name="结论" label="结论">
          <template #input>
            <van-radio-group v-model="conclusionForm.conclusion" direction="horizontal">
              <van-radio name="stop" class="mr-3">停工</van-radio>
              <van-radio name="resume">复工</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field label="停工开始">
          <template #input><input type="date" v-model="conclusionForm.stopStartDate" class="date-input" /></template>
        </van-field>
        <van-field label="预计复工">
          <template #input><input type="date" v-model="conclusionForm.expectedResumeDate" class="date-input" /></template>
        </van-field>
        <van-field label="实际复工">
          <template #input><input type="date" v-model="conclusionForm.actualResumeDate" class="date-input" /></template>
        </van-field>
        <van-field v-model="conclusionForm.conclusionRemark" type="textarea" label="医嘱/条件"
                   placeholder="如：制动休息6周，2026-10-22复诊评估" rows="2" autosize />
        <div class="px-4 py-3">
          <van-button round block type="danger" :loading="concluding" @click="submitConclusion">
            确认结论并回写排班档案
          </van-button>
        </div>
      </template>
      <van-cell v-else title="复工/停工结论由现场主管确认" />
    </van-cell-group>

    <!-- 排班档案时间线 -->
    <van-cell-group inset title="排班档案留痕" class="mt-3">
      <van-cell v-if="detail.schedule"
                :title="`${detail.schedule.postName}｜${SHIFT_TEXT[detail.schedule.shift] || detail.schedule.shift}｜${detail.schedule.scheduleDate}`">
        <template #label>
          <van-tag :type="scheduleTagType(detail.schedule.scheduleStatus)">
            {{ scheduleStatusText(detail.schedule.scheduleStatus) }}
          </van-tag>
          <span v-if="detail.schedule.stopStartDate" class="ml-2 text-xs">停工起：{{ detail.schedule.stopStartDate }}</span>
          <span v-if="detail.schedule.resumeDate" class="ml-2 text-xs">复工：{{ detail.schedule.resumeDate }}</span>
        </template>
      </van-cell>
      <van-steps direction="vertical" :active="detail.scheduleEvents.length" active-color="#07c160" class="px-4 py-3">
        <van-step v-for="(e, i) in detail.scheduleEvents" :key="e.id || i">
          <h4 class="text-sm font-medium">{{ eventTypeText(e.eventType) }}</h4>
          <p class="text-xs text-gray-500">{{ e.operatorName }}｜{{ (e.eventTime || '').replace('T',' ') }}</p>
          <p class="text-xs text-gray-600">{{ e.eventContent }}</p>
        </van-step>
      </van-steps>
      <van-cell v-if="!detail.scheduleEvents || !detail.scheduleEvents.length" title="暂无档案事件" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import {
  injuryApi, materialApi,
  STATUS_TEXT, CONCLUSION_TEXT, SHIFT_TEXT, CLAIM_STATUS_TEXT,
  INJURY_TYPE_OPTIONS
} from '@/api/injury'

const route = useRoute()
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')

const detail = ref(null)
const savingInsurance = ref(false)
const concluding = ref(false)

const insurance = reactive({
  insuranceCompany: '', policyNo: '', insuredName: '',
  coverageStartDate: '', coverageEndDate: '',
  claimStatus: 'not_filed', claimNo: '',
  contactName: '', contactPhone: '', remark: ''
})
const conclusionForm = reactive({
  conclusion: 'stop',
  stopStartDate: new Date().toISOString().slice(0, 10),
  expectedResumeDate: '',
  actualResumeDate: '',
  conclusionRemark: ''
})

const claimOptions = [
  { text: '未报案', value: 'not_filed' },
  { text: '已报案', value: 'reported' },
  { text: '理赔中', value: 'claiming' },
  { text: '已赔付', value: 'paid' },
  { text: '拒赔', value: 'rejected' }
]

const canUpload = computed(() => user && user.role !== 'enterprise')
const canEditInsurance = computed(() => detail.value?.canEditInsurance)
const canConclude = computed(() => detail.value?.canConclude)

const canUploadOwner = (owner) => {
  if (user?.role === 'admin') return true
  if (owner === 'supervisor') return user?.role === 'supervisor'
  if (owner === 'labor') return user?.role === 'labor'
  return user?.role === 'supervisor' || user?.role === 'labor' // common 双方可补
}

const typeText = (t) => (INJURY_TYPE_OPTIONS.find(i => i.value === t) || {}).label || t
const witnessTypeText = (t) => ({ coworker: '同班组工人', manager: '现场管理', other: '其他' }[t] || t)
const statusTagType = (s) => ({ draft: 'default', reported: 'primary', submitted: 'success', concluded: 'warning' }[s] || 'default')
const scheduleTagType = (s) => ({ normal: 'primary', injury_stop: 'danger', resumed: 'success' }[s] || 'default')
const scheduleStatusText = (s) => ({ normal: '正常排班', injury_stop: '工伤停工', resumed: '已复工' }[s] || s)
const eventTypeText = (t) => ({ injury_report: '工伤上报', injury_stop: '停工结论', injury_resume: '复工结论' }[t] || t)

const load = async () => {
  const res = await injuryApi.detail(route.params.id)
  detail.value = res.data
  if (detail.value.insurance) {
    Object.assign(insurance, {
      insuranceCompany: '', policyNo: '', insuredName: '',
      coverageStartDate: '', coverageEndDate: '',
      claimStatus: 'not_filed', claimNo: '',
      contactName: '', contactPhone: '', remark: '',
      ...detail.value.insurance
    })
  } else if (detail.value.report) {
    insurance.insuredName = detail.value.report.workerName
  }
  if (detail.value.report.conclusion === 'stop') {
    conclusionForm.conclusion = 'stop'
    conclusionForm.stopStartDate = detail.value.report.stopStartDate
    conclusionForm.expectedResumeDate = detail.value.report.expectedResumeDate
  }
}

const afterReadMaterial = async (file, materialType) => {
  try {
    file.status = 'uploading'
    file.message = '上传中...'
    await materialApi.upload(file.file, { reportId: route.params.id, materialType })
    showSuccessToast('上传成功')
    await load()
  } catch (e) {
    file.status = 'failed'
    file.message = '上传失败'
  }
}

const preview = (m) => {
  window.open(materialApi.previewUrl(m.id), '_blank')
}

const removeMaterial = async (m) => {
  try {
    await showConfirmDialog({ title: '确认删除该材料？', message: m.materialName })
    await materialApi.remove(m.id)
    showSuccessToast('已删除')
    await load()
  } catch (e) { /* cancel or handled */ }
}

const saveInsurance = async () => {
  savingInsurance.value = true
  try {
    await injuryApi.saveInsurance(route.params.id, {
      ...insurance,
      coverageStartDate: insurance.coverageStartDate || null,
      coverageEndDate: insurance.coverageEndDate || null
    })
    showSuccessToast('保险资料已保存')
    await load()
  } catch (e) { /* handled */ } finally {
    savingInsurance.value = false
  }
}

const submitConclusion = async () => {
  if (conclusionForm.conclusion === 'stop' && !conclusionForm.stopStartDate) {
    return showToast('请填写停工开始日期')
  }
  if (conclusionForm.conclusion === 'resume' && !conclusionForm.actualResumeDate) {
    return showToast('请填写实际复工日期')
  }
  const tip = conclusionForm.conclusion === 'stop'
    ? '确认停工结论？将回写排班档案并通知排班停止派工。'
    : '确认复工结论？将回写排班档案，该工人可恢复排班。'
  try {
    await showConfirmDialog({ title: '结论确认', message: tip })
    await injuryApi.conclude(route.params.id, {
      conclusion: conclusionForm.conclusion,
      stopStartDate: conclusionForm.conclusion === 'stop' ? conclusionForm.stopStartDate : null,
      expectedResumeDate: conclusionForm.expectedResumeDate || null,
      actualResumeDate: conclusionForm.conclusion === 'resume' ? conclusionForm.actualResumeDate : null,
      conclusionRemark: conclusionForm.conclusionRemark
    })
    showSuccessToast('结论已回写排班档案')
    await load()
  } catch (e) { /* cancel */ }
}

load()
</script>

<style scoped>
.missing-tip {
  @apply mx-4 my-2 px-3 py-2 rounded-lg text-xs leading-5;
}
.missing-tip.supervisor { background: #fef0f0; color: #f56c6c; }
.missing-tip.labor { background: #fdf6ec; color: #e6a23c; }
.missing-tip.medical { background: #ecf5ff; color: #409eff; }
.date-input {
  border: 1px solid #dcdee9;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 13px;
  width: 150px;
}
.inline-dropdown {
  display: inline-block;
}
.inline-dropdown :deep(.van-dropdown-menu__bar) {
  box-shadow: none;
  height: 28px;
  background: transparent;
}
.inline-dropdown :deep(.van-dropdown-menu__item) {
  padding: 0;
}
.inline-dropdown :deep(.van-dropdown-menu__title) {
  padding-right: 14px;
}
</style>
