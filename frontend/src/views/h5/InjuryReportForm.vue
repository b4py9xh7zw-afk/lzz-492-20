<template>
  <div class="p-3">
    <van-form @submit="onSubmit">
      <!-- 岗位班次：从今日排班档案选择 -->
      <van-cell-group inset title="岗位班次（从排班档案带出）">
        <van-field
          readonly
          clickable
          :model-value="scheduleText"
          label="排班档案"
          placeholder="点击选择今天的排班（岗位/班次/工人）"
          :rules="[{ required: true, message: '请选择排班档案' }]"
          @click="showSchedulePicker = true"
          is-link
        />
        <van-popup v-model:show="showSchedulePicker" position="bottom" round>
          <van-picker
            :columns="scheduleColumns"
            @confirm="onScheduleConfirm"
            @cancel="showSchedulePicker = false"
            title="选择排班档案"
          />
        </van-popup>
        <van-field name="post" label="岗位" :model-value="form.postName" placeholder="自动带出" readonly />
        <van-field name="shift" label="班次" :model-value="SHIFT_TEXT[form.shiftName] || ''" placeholder="自动带出" readonly />
        <van-field name="worker" label="受伤工人" :model-value="form.workerName" placeholder="自动带出" readonly />
      </van-cell-group>

      <!-- 受伤信息 -->
      <van-cell-group inset title="受伤情况" class="mt-3">
        <van-field
          v-model="form.injuryTimeText"
          is-link
          readonly
          name="injuryTime"
          label="受伤时间"
          placeholder="选择受伤时间"
          :rules="[{ required: true, message: '请选择受伤时间' }]"
          @click="openInjuryPicker"
        />
        <van-field
          v-model="form.injuryLocation"
          label="受伤地点"
          placeholder="如：3#楼东侧6层外架"
          :rules="[{ required: true, message: '请填写受伤地点' }]"
        />
        <van-field name="injuryType" label="伤情类型">
          <template #input>
            <van-radio-group v-model="form.injuryType" direction="horizontal" class="text-sm">
              <van-radio v-for="t in INJURY_TYPE_OPTIONS" :key="t.value" :name="t.value" class="mr-2">
                {{ t.label }}
              </van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
          v-model="form.injuryDesc"
          type="textarea"
          label="受伤经过"
          placeholder="简述作业内容、受伤原因与伤情"
          rows="3"
          autosize
        />
      </van-cell-group>

      <!-- 送医 -->
      <van-cell-group inset title="送医信息" class="mt-3">
        <van-field v-model="form.hospital" label="送医医院" placeholder="如：市第七人民医院急诊骨科" />
        <van-field
          v-model="form.hospitalTimeText"
          is-link
          readonly
          label="送医时间"
          placeholder="选择送医时间（可稍后补）"
          @click="openHospitalPicker"
        />
      </van-cell-group>

      <!-- 见证人 -->
      <van-cell-group inset title="见证人" class="mt-3">
        <div v-for="(w, idx) in witnesses" :key="idx" class="border-b border-gray-100 last:border-0">
          <van-field v-model="w.witnessName" label="姓名" placeholder="见证人姓名" />
          <van-field v-model="w.witnessPhone" label="电话" placeholder="联系电话" type="tel" />
          <van-field name="type" label="身份">
            <template #input>
              <van-radio-group v-model="w.witnessType" direction="horizontal" class="text-xs">
                <van-radio name="coworker" class="mr-2">同班组</van-radio>
                <van-radio name="manager" class="mr-2">管理</van-radio>
                <van-radio name="other">其他</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field v-model="w.statement" type="textarea" label="见证说明" placeholder="看到的情况" rows="2" autosize />
          <div class="text-right pr-4 pb-2">
            <van-button size="mini" plain type="danger" @click="removeWitness(idx)">删除见证人</van-button>
          </div>
        </div>
        <div class="px-4 py-3">
          <van-button size="small" plain icon="plus" @click="addWitness">添加见证人</van-button>
        </div>
      </van-cell-group>

      <div class="flex gap-3 px-4 py-5">
        <van-button round block plain type="primary" native-type="button" :loading="saving" @click="save(false)">
          暂存草稿
        </van-button>
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          提交上报
        </van-button>
      </div>
    </van-form>

    <!-- 受伤时间：日期 -> 时间 -->
    <van-popup v-model:show="injuryDateShow" position="bottom" round>
      <van-date-picker
        v-model="injuryDate"
        title="受伤日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onInjuryDate"
        @cancel="injuryDateShow = false"
      />
    </van-popup>
    <van-popup v-model:show="injuryTimeShow" position="bottom" round>
      <van-time-picker
        v-model="injuryTimeVal"
        title="受伤时间"
        @confirm="onInjuryTime"
        @cancel="injuryTimeShow = false"
      />
    </van-popup>

    <!-- 送医时间：日期 -> 时间 -->
    <van-popup v-model:show="hospitalDateShow" position="bottom" round>
      <van-date-picker
        v-model="hospitalDate"
        title="送医日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onHospitalDate"
        @cancel="hospitalDateShow = false"
      />
    </van-popup>
    <van-popup v-model:show="hospitalTimeShow" position="bottom" round>
      <van-time-picker
        v-model="hospitalTimeVal"
        title="送医时间"
        @confirm="onHospitalTime"
        @cancel="hospitalTimeShow = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { scheduleApi, injuryApi, INJURY_TYPE_OPTIONS, SHIFT_TEXT } from '@/api/injury'

const router = useRouter()
const route = useRoute()
const editId = route.params.id || null

const showSchedulePicker = ref(false)
const schedules = ref([])
const scheduleColumns = ref([])

const form = reactive({
  scheduleId: null,
  projectId: null,
  workerId: null,
  workerName: '',
  postName: '',
  shiftName: 'day',
  injuryTimeText: '',
  injuryLocation: '',
  injuryType: 'outpatient',
  injuryDesc: '',
  hospital: '',
  hospitalTimeText: ''
})
const witnesses = ref([{ witnessName: '', witnessPhone: '', witnessType: 'coworker', statement: '' }])
const saving = ref(false)
const submitting = ref(false)

const minDate = new Date(2024, 0, 1)
const maxDate = new Date(2030, 11, 31)

const pad = (n) => String(n).padStart(2, '0')
const nowParts = () => {
  const d = new Date()
  return {
    date: [`${d.getFullYear()}`, pad(d.getMonth() + 1), pad(d.getDate())],
    time: [pad(d.getHours()), pad(d.getMinutes())]
  }
}

// 受伤时间选择
const injuryDateShow = ref(false)
const injuryTimeShow = ref(false)
const injuryDate = ref(nowParts().date)
const injuryTimeVal = ref(nowParts().time)
const openInjuryPicker = () => { injuryDateShow.value = true }
const onInjuryDate = ({ selectedValues }) => {
  injuryDate.value = selectedValues
  injuryDateShow.value = false
  injuryTimeShow.value = true
}
const onInjuryTime = ({ selectedValues }) => {
  injuryTimeVal.value = selectedValues
  injuryTimeShow.value = false
  const [y, m, d] = injuryDate.value
  const [hh, mm] = selectedValues
  form.injuryTimeText = `${y}-${m}-${d} ${hh}:${mm}:00`
}

// 送医时间选择
const hospitalDateShow = ref(false)
const hospitalTimeShow = ref(false)
const hospitalDate = ref(nowParts().date)
const hospitalTimeVal = ref(nowParts().time)
const openHospitalPicker = () => { hospitalDateShow.value = true }
const onHospitalDate = ({ selectedValues }) => {
  hospitalDate.value = selectedValues
  hospitalDateShow.value = false
  hospitalTimeShow.value = true
}
const onHospitalTime = ({ selectedValues }) => {
  hospitalTimeVal.value = selectedValues
  hospitalTimeShow.value = false
  const [y, m, d] = hospitalDate.value
  const [hh, mm] = selectedValues
  form.hospitalTimeText = `${y}-${m}-${d} ${hh}:${mm}:00`
}

const scheduleText = computed(() => {
  if (!form.scheduleId) return ''
  return `${form.workerName} / ${form.postName} / ${SHIFT_TEXT[form.shiftName] || form.shiftName}`
})

const onScheduleConfirm = ({ selectedOptions }) => {
  const row = selectedOptions?.[0]?.raw?._row
  if (row) {
    form.scheduleId = row.id
    form.projectId = row.projectId
    form.workerId = row.workerId
    form.workerName = row.workerName
    form.postName = row.postName
    form.shiftName = row.shift
  }
  showSchedulePicker.value = false
}

const addWitness = () => witnesses.value.push({ witnessName: '', witnessPhone: '', witnessType: 'coworker', statement: '' })
const removeWitness = (idx) => witnesses.value.splice(idx, 1)

const loadSchedules = async () => {
  try {
    // 支持补报：默认拉近 14 天排班
    const end = new Date()
    const start = new Date(Date.now() - 14 * 24 * 3600 * 1000)
    const fmt = (d) => d.toISOString().slice(0, 10)
    const res = await scheduleApi.list({ startDate: fmt(start), endDate: fmt(end) })
    const rows = res.data || []
    schedules.value = rows
    scheduleColumns.value = rows.map(s => ({
      text: `${s.scheduleDate} ${s.workerName}｜${s.postName}｜${SHIFT_TEXT[s.shift] || s.shift}`,
      value: s.id,
      _row: s
    }))
  } catch (e) { /* ignore */ }
}

const loadDetail = async () => {
  if (!editId) return
  const res = await injuryApi.detail(editId)
  const d = res.data
  const r = d.report
  Object.assign(form, {
    scheduleId: r.scheduleId,
    projectId: r.projectId,
    workerId: r.workerId,
    workerName: r.workerName,
    postName: r.postName,
    shiftName: r.shiftName,
    injuryTimeText: r.injuryTime,
    injuryLocation: r.injuryLocation,
    injuryType: r.injuryType,
    injuryDesc: r.injuryDesc,
    hospital: r.hospital,
    hospitalTimeText: r.hospitalTime || ''
  })
  if (d.witnesses && d.witnesses.length) {
    witnesses.value = d.witnesses.map(w => ({
      witnessName: w.witnessName,
      witnessPhone: w.witnessPhone,
      witnessType: w.witnessType || 'coworker',
      statement: w.statement
    }))
  }
}

const buildPayload = (submit) => ({
  id: editId ? Number(editId) : undefined,
  scheduleId: form.scheduleId,
  projectId: form.projectId,
  workerId: form.workerId,
  injuryTime: form.injuryTimeText || undefined,
  injuryLocation: form.injuryLocation,
  injuryDesc: form.injuryDesc,
  injuryType: form.injuryType,
  hospital: form.hospital,
  hospitalTime: form.hospitalTimeText || undefined,
  submit,
  witnesses: witnesses.value.filter(w => w.witnessName)
})

const save = async (submit) => {
  if (!form.scheduleId) return showToast('请先选择排班档案')
  if (!form.injuryTimeText) return showToast('请选择受伤时间')
  if (!form.injuryLocation) return showToast('请填写受伤地点')
  if (submit && !form.hospital) return showToast('提交时需填写送医医院')
  if (submit && witnesses.value.filter(w => w.witnessName).length === 0) return showToast('提交时至少填写1名见证人')

  saving.value = !submit
  submitting.value = submit
  try {
    const res = await injuryApi.saveReport(buildPayload(submit))
    showSuccessToast(submit ? '已提交，去补材料' : '草稿已保存')
    router.replace(`/h5/injury/${res.data.id}`)
  } catch (e) { /* 拦截器已提示 */ } finally {
    saving.value = false
    submitting.value = false
  }
}

const onSubmit = () => save(true)

onMounted(async () => {
  await loadSchedules()
  await loadDetail()
})
</script>
