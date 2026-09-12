<template>
  <div>
    <!-- 查询条件 -->
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="单号/工人/医院/地点" clearable style="width: 220px"
                    @keyup.enter="loadList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.reportStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(t, k) in STATUS_TEXT" :key="k" :label="t" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="伤情">
          <el-select v-model="query.injuryType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in INJURY_TYPE_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 角色说明 -->
    <el-alert :title="roleTip" type="info" :closable="false" class="mb-4" show-icon />

    <!-- 列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="reportNo" label="上报单号" width="150" />
        <el-table-column label="工人/岗位" min-width="140">
          <template #default="{ row }">
            {{ row.workerName }} / {{ row.postName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="班次" width="70">
          <template #default="{ row }">{{ SHIFT_TEXT[row.shiftName] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="injuryTime" label="受伤时间" width="160" />
        <el-table-column prop="injuryLocation" label="地点" min-width="160" show-overflow-tooltip />
        <el-table-column prop="hospital" label="送医医院" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.reportStatus)">{{ STATUS_TEXT[row.reportStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结论" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.conclusion" :type="row.conclusion === 'stop' ? 'danger' : 'success'">
              {{ CONCLUSION_TEXT[row.conclusion] }}
            </el-tag>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="材料" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.materialProgress || 0" :stroke-width="8"
                         :status="(row.materialProgress || 0) === 100 ? 'success' : ''" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" size="62%" :title="detail?.report?.reportNo || '工伤详情'">
      <template v-if="detail">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="上报信息" name="base">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="项目">{{ detail.project?.projectName }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusTagType(detail.report.reportStatus)">
                  {{ STATUS_TEXT[detail.report.reportStatus] }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="工人">{{ detail.report.workerName }}</el-descriptions-item>
              <el-descriptions-item label="岗位/班次">
                {{ detail.report.postName }} / {{ SHIFT_TEXT[detail.report.shiftName] }}
              </el-descriptions-item>
              <el-descriptions-item label="受伤时间">{{ detail.report.injuryTime }}</el-descriptions-item>
              <el-descriptions-item label="伤情类型">{{ typeText(detail.report.injuryType) }}</el-descriptions-item>
              <el-descriptions-item label="受伤地点" :span="2">{{ detail.report.injuryLocation }}</el-descriptions-item>
              <el-descriptions-item label="送医医院">{{ detail.report.hospital }}</el-descriptions-item>
              <el-descriptions-item label="送医时间">{{ detail.report.hospitalTime }}</el-descriptions-item>
              <el-descriptions-item label="受伤经过" :span="2">{{ detail.report.injuryDesc }}</el-descriptions-item>
              <el-descriptions-item label="上报主管">
                {{ detail.report.reporterName }} / {{ detail.report.reportTime }}
              </el-descriptions-item>
              <el-descriptions-item label="见证人">
                <span v-for="w in detail.witnesses" :key="w.id" class="mr-3">
                  {{ w.witnessName }}（{{ w.witnessPhone }}）
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="`材料清单（缺${detail.checklist.missingNames.length}）`" name="material">
            <el-alert
              v-if="detail.checklist.missingNames.length"
              type="warning"
              :closable="false"
              show-icon
              class="mb-3"
              :title="`还缺 ${detail.checklist.missingNames.length} 份：${detail.checklist.missingNames.join('、')}`"
            />
            <el-alert v-else type="success" :closable="false" show-icon class="mb-3"
                      title="必需材料已齐全" />

            <el-table :data="detail.checklist.items" border size="small">
              <el-table-column prop="materialName" label="材料名称" min-width="180" />
              <el-table-column label="责任方" width="110">
                <template #default="{ row }">
                  <el-tag :type="ownerTag(row.ownerRole)">{{ row.ownerName }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.provided ? 'success' : 'danger'">
                    {{ row.provided ? '已交' : '缺失' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="hint" label="要求/提示" min-width="200" />
            </el-table>

            <el-divider content-position="left">已上传材料</el-divider>
            <el-table :data="detail.materials" border size="small">
              <el-table-column prop="materialName" label="材料" min-width="160" />
              <el-table-column prop="originalName" label="文件名" min-width="180" show-overflow-tooltip />
              <el-table-column prop="uploadUserName" label="上传人" width="110" />
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button link type="primary" @click="preview(row)">预览</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="保险资料" name="insurance">
            <el-form label-width="110px" :disabled="!detail.canEditInsurance" class="max-w-2xl">
              <el-form-item label="承保机构">
                <el-input v-model="insurance.insuranceCompany" placeholder="保险公司/社保经办机构" />
              </el-form-item>
              <el-form-item label="保单号">
                <el-input v-model="insurance.policyNo" />
              </el-form-item>
              <el-form-item label="参保人">
                <el-input v-model="insurance.insuredName" />
              </el-form-item>
              <el-form-item label="保障期间">
                <el-date-picker v-model="insurance.coverageStartDate" type="date" value-format="YYYY-MM-DD"
                                placeholder="起保日期" class="!w-44" />
                <span class="mx-2">~</span>
                <el-date-picker v-model="insurance.coverageEndDate" type="date" value-format="YYYY-MM-DD"
                                placeholder="终止日期" class="!w-44" />
              </el-form-item>
              <el-form-item label="理赔状态">
                <el-select v-model="insurance.claimStatus" style="width: 160px">
                  <el-option v-for="(t, k) in CLAIM_STATUS_TEXT" :key="k" :label="t" :value="k" />
                </el-select>
              </el-form-item>
              <el-form-item label="报案/受理号">
                <el-input v-model="insurance.claimNo" />
              </el-form-item>
              <el-form-item label="经办人">
                <el-input v-model="insurance.contactName" class="!w-48" />
                <el-input v-model="insurance.contactPhone" placeholder="电话" class="!w-48 ml-2" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="insurance.remark" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item v-if="detail.canEditInsurance">
                <el-button type="primary" :loading="savingInsurance" @click="saveInsurance">保存保险资料</el-button>
                <span class="text-xs text-gray-400 ml-2">仅劳务公司/管理员可编辑</span>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="复工/停工结论" name="conclusion">
            <el-form label-width="110px" class="max-w-2xl" :disabled="!detail.canConclude">
              <el-form-item label="当前结论">
                <el-tag v-if="detail.report.conclusion"
                        :type="detail.report.conclusion === 'stop' ? 'danger' : 'success'">
                  {{ CONCLUSION_TEXT[detail.report.conclusion] }}
                  （{{ detail.report.concludedBy }} {{ detail.report.concludedTime }}）
                </el-tag>
                <span v-else class="text-gray-400">暂无结论</span>
              </el-form-item>
              <el-form-item label="结论">
                <el-radio-group v-model="conclusionForm.conclusion">
                  <el-radio value="stop">停工</el-radio>
                  <el-radio value="resume">复工</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="停工开始">
                <el-date-picker v-model="conclusionForm.stopStartDate" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="预计复工">
                <el-date-picker v-model="conclusionForm.expectedResumeDate" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="实际复工">
                <el-date-picker v-model="conclusionForm.actualResumeDate" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="医嘱/复工条件">
                <el-input v-model="conclusionForm.conclusionRemark" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item v-if="detail.canConclude">
                <el-button type="danger" :loading="concluding" @click="submitConclusion">
                  确认结论并回写排班档案
                </el-button>
              </el-form-item>
              <el-form-item v-else>
                <span class="text-xs text-gray-400">企业账号仅可查看；结论由现场主管/管理员确认</span>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="排班档案留痕" name="archive">
            <el-descriptions v-if="detail.schedule" :column="2" border class="mb-3">
              <el-descriptions-item label="岗位班次">
                {{ detail.schedule.postName }} / {{ SHIFT_TEXT[detail.schedule.shift] }}
              </el-descriptions-item>
              <el-descriptions-item label="排班日期">{{ detail.schedule.scheduleDate }}</el-descriptions-item>
              <el-descriptions-item label="档案状态">
                <el-tag :type="scheduleTagType(detail.schedule.scheduleStatus)">
                  {{ scheduleStatusText(detail.schedule.scheduleStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="停工/复工">
                {{ detail.schedule.stopStartDate || '-' }} ~ {{ detail.schedule.resumeDate || '未复工' }}
              </el-descriptions-item>
            </el-descriptions>
            <el-timeline>
              <el-timeline-item v-for="e in detail.scheduleEvents" :key="e.id"
                                :type="e.eventType === 'injury_resume' ? 'success' : 'danger'"
                                :timestamp="`${e.operatorName || ''} ${e.eventTime || ''}`">
                <h4 class="font-medium">{{ eventTypeText(e.eventType) }}</h4>
                <p class="text-gray-600 text-sm">{{ e.eventContent }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  injuryApi, materialApi,
  STATUS_TEXT, CONCLUSION_TEXT, SHIFT_TEXT, CLAIM_STATUS_TEXT, INJURY_TYPE_OPTIONS
} from '@/api/injury'

const user = JSON.parse(localStorage.getItem('user') || 'null')
const route = useRoute()

const query = reactive({ current: 1, size: 10, keyword: '', reportStatus: '', injuryType: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const drawer = ref(false)
const activeTab = ref('base')
const detail = ref(null)
const savingInsurance = ref(false)
const concluding = ref(false)

const insurance = reactive({})
const conclusionForm = reactive({
  conclusion: 'stop', stopStartDate: '', expectedResumeDate: '', actualResumeDate: '', conclusionRemark: ''
})

const roleTip = computed(() => ({
  admin: '平台管理员：可见全部项目数据，可上报、补保险、下结论。',
  supervisor: '现场主管：仅本项目；可手机端上报、传现场证明、确认复工/停工。',
  labor: '劳务公司：仅本公司合作项目；可补充保险资料、上传合同/保单、补传医疗票据。',
  enterprise: '用工企业：仅可查看本项目工伤数据，不能修改或上传。'
}[user?.role || 'admin']))

const statusTagType = (s) => ({ draft: 'info', reported: '', submitted: 'success', concluded: 'warning' }[s] || '')
const typeText = (t) => (INJURY_TYPE_OPTIONS.find(i => i.value === t) || {}).label || t
const ownerTag = (o) => ({ supervisor: 'danger', labor: 'warning', common: '' }[o] || '')
const scheduleTagType = (s) => ({ normal: '', injury_stop: 'danger', resumed: 'success' }[s] || '')
const scheduleStatusText = (s) => ({ normal: '正常排班', injury_stop: '工伤停工', resumed: '已复工' }[s] || s)
const eventTypeText = (t) => ({ injury_report: '工伤上报', injury_stop: '停工结论', injury_resume: '复工结论' }[t] || t)

const loadList = async () => {
  loading.value = true
  try {
    const res = await injuryApi.page({
      current: query.current, size: query.size,
      keyword: query.keyword || undefined,
      reportStatus: query.reportStatus || undefined,
      injuryType: query.injuryType || undefined
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { current: 1, keyword: '', reportStatus: '', injuryType: '' })
  loadList()
}

const openDetail = async (id) => {
  const res = await injuryApi.detail(id)
  detail.value = res.data
  Object.keys(insurance).forEach(k => delete insurance[k])
  if (detail.value.insurance) {
    Object.assign(insurance, detail.value.insurance)
  } else {
    Object.assign(insurance, {
      insuredName: detail.value.report.workerName, claimStatus: 'not_filed'
    })
  }
  Object.assign(conclusionForm, {
    conclusion: detail.value.report.conclusion || 'stop',
    stopStartDate: detail.value.report.stopStartDate || '',
    expectedResumeDate: detail.value.report.expectedResumeDate || '',
    actualResumeDate: detail.value.report.actualResumeDate || '',
    conclusionRemark: detail.value.report.conclusionRemark || ''
  })
  activeTab.value = 'base'
  drawer.value = true
}

const preview = (m) => window.open(materialApi.previewUrl(m.id), '_blank')

const saveInsurance = async () => {
  savingInsurance.value = true
  try {
    await injuryApi.saveInsurance(detail.value.report.id, insurance)
    ElMessage.success('保险资料已保存')
    await openDetail(detail.value.report.id)
  } finally {
    savingInsurance.value = false
  }
}

const submitConclusion = async () => {
  if (conclusionForm.conclusion === 'stop' && !conclusionForm.stopStartDate) {
    return ElMessage.warning('请填写停工开始日期')
  }
  if (conclusionForm.conclusion === 'resume' && !conclusionForm.actualResumeDate) {
    return ElMessage.warning('请填写实际复工日期')
  }
  await ElMessageBox.confirm('确认后结论将回写排班档案并留痕，是否继续？', '结论确认', { type: 'warning' })
  concluding.value = true
  try {
    await injuryApi.conclude(detail.value.report.id, {
      conclusion: conclusionForm.conclusion,
      stopStartDate: conclusionForm.stopStartDate || null,
      expectedResumeDate: conclusionForm.expectedResumeDate || null,
      actualResumeDate: conclusionForm.actualResumeDate || null,
      conclusionRemark: conclusionForm.conclusionRemark
    })
    ElMessage.success('结论已回写排班档案')
    await Promise.all([loadList(), openDetail(detail.value.report.id)])
  } finally {
    concluding.value = false
  }
}

loadList()

// 从排班档案页跳转时自动打开对应工伤单
if (route.query.id) {
  openDetail(Number(route.query.id))
}
</script>
