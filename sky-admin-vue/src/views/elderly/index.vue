<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 10px">老人姓名：</label>
        <el-input
          v-model="queryParams.name"
          placeholder="请输入老人姓名"
          style="width: 14%"
          clearable
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <div class="tableLab">
          <el-button type="primary" @click="handleAdd">
            + 新增老人档案
          </el-button>
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column prop="name" label="姓名" />
        <el-table-column label="性别" width="80">
          <template slot-scope="{ row }">
            {{ row.gender === '1' ? '男' : row.gender === '0' ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="phone" label="联系电话" min-width="120" />
        <el-table-column prop="familyName" label="关联家属" min-width="140" />
        <el-table-column prop="familyUsername" label="FAMILY账号" min-width="160" />
        <el-table-column prop="address" label="居住地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="所属助餐点" min-width="160">
          <template slot-scope="{ row }">
            <span>{{ row.diningPointName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gridCode" label="所属网格" min-width="100" />
        <el-table-column prop="healthInfo" label="健康状况" min-width="160" show-overflow-tooltip />
        <el-table-column prop="specialNeeds" label="特殊需求" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="blueBug"
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delBut non"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        :current-page="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      :title="title"
      :visible.sync="open"
      width="640px"
      append-to-body
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                <el-option label="男" value="1" />
                <el-option label="女" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="50" :max="120" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联家属" prop="selectedFamilyProfileId">
          <el-select
            v-model="form.selectedFamilyProfileId"
            placeholder="请选择关联家属"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="item in familyProfileOptions"
              :key="item.id"
              :label="formatFamilyProfileLabel(item)"
              :value="item.id"
              :disabled="Boolean(item.disabled)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入详细住址" />
        </el-form-item>
        <el-form-item label="所属助餐点" prop="diningPointId">
          <el-select
            v-model="form.diningPointId"
            placeholder="请选择所属助餐点"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="item in diningPointOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属网格" prop="gridCode">
          <el-input v-model="form.gridCode" placeholder="例如：A-01" />
        </el-form-item>
        <el-form-item label="健康状况" prop="healthInfo">
          <el-input v-model="form.healthInfo" type="textarea" placeholder="如慢性病、过敏源等" />
        </el-form-item>
        <el-form-item label="特殊需求" prop="specialNeeds">
          <el-input v-model="form.specialNeeds" type="textarea" placeholder="如需敲门提醒、行动不便等" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">
          确定
        </el-button>
        <el-button @click="cancel">
          取消
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  addElderly,
  deleteElderly,
  editElderly,
  ElderlyFormData,
  getElderlyPage,
  queryElderlyById
} from '@/api/elderly'
import { DiningPointOption, getDiningPointList } from '@/api/diningPoint'
import { FamilyProfileOption, getFamilyProfileOptions } from '@/api/familyProfile'

interface PageResponse<T> {
  total?: number
  records?: T[]
}

interface FamilyProfileSelectOption extends FamilyProfileOption {
  disabled?: boolean
  isInjected?: boolean
}

interface ElderlyViewForm extends ElderlyFormData {
  selectedFamilyProfileId?: number
}

@Component({
  name: 'Elderly'
})
export default class extends Vue {
  private tableData: ElderlyFormData[] = []
  private loading = false
  private total = 0
  private page = 1
  private pageSize = 10
  private queryParams = {
    name: ''
  }
  private open = false
  private title = ''
  private form: ElderlyViewForm = this.createDefaultForm()
  private diningPointOptions: DiningPointOption[] = []
  private familyProfileOptions: FamilyProfileSelectOption[] = []
  private activeFamilyProfileOptions: FamilyProfileSelectOption[] = []
  private rules = {
    name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    selectedFamilyProfileId: [{ required: true, message: '请选择关联家属', trigger: 'change' }],
    phone: [{ required: true, message: '电话不能为空', trigger: 'blur' }],
    address: [{ required: true, message: '地址不能为空', trigger: 'blur' }],
    diningPointId: [{ required: true, message: '请选择所属助餐点', trigger: 'change' }]
  }

  created() {
    this.loadDiningPointOptions()
    this.loadActiveFamilyProfileOptions()
    this.getList()
  }

  private createDefaultForm(): ElderlyViewForm {
    return {
      id: undefined,
      userId: undefined,
      familyProfileId: undefined,
      familyName: '',
      familyPhone: '',
      familyUsername: '',
      selectedFamilyProfileId: undefined,
      name: '',
      gender: '1',
      age: 60,
      phone: '',
      address: '',
      diningPointId: undefined,
      diningPointName: '',
      gridCode: '',
      healthInfo: '',
      specialNeeds: '',
      idCard: '',
      image: ''
    }
  }

  private unwrapData<T>(response: any, fallback: string): T {
    const payload = response && response.data ? response.data : response
    if (!payload || String(payload.code) !== '1') {
      throw new Error((payload && payload.msg) || fallback)
    }
    return typeof payload.data !== 'undefined' ? payload.data : payload
  }

  private unwrapPage<T>(response: any, fallback: string): PageResponse<T> {
    return this.unwrapData<PageResponse<T>>(response, fallback) || {}
  }

  private getErrorMessage(error: any, fallback: string) {
    const response = error && error.response ? error.response : null
    const data = response && response.data ? response.data : null
    return (data && data.msg) || (error && error.message) || fallback
  }

  private async getList() {
    this.loading = true
    try {
      const params = {
        page: this.page,
        pageSize: this.pageSize,
        name: this.queryParams.name.trim() || undefined
      }
      const pageData = this.unwrapPage<ElderlyFormData>(await getElderlyPage(params), '获取老人档案失败')
      this.tableData = Array.isArray(pageData.records) ? pageData.records : []
      this.total = pageData.total || 0
    } catch (error) {
      this.$message.error(this.getErrorMessage(error, '获取老人档案失败'))
    } finally {
      this.loading = false
    }
  }

  private handleQuery() {
    this.page = 1
    this.getList()
  }

  private async loadDiningPointOptions() {
    try {
      const data = this.unwrapData<DiningPointOption[]>(await getDiningPointList({ status: 1 }), '加载助餐点列表失败')
      this.diningPointOptions = Array.isArray(data) ? data : []
    } catch (error) {
      this.diningPointOptions = []
      this.$message.error(this.getErrorMessage(error, '加载助餐点列表失败'))
    }
  }

  private async loadActiveFamilyProfileOptions() {
    try {
      const data = this.unwrapData<FamilyProfileOption[]>(await getFamilyProfileOptions(), '加载家属档案列表失败')
      this.activeFamilyProfileOptions = Array.isArray(data)
        ? data.map((item) => ({ ...item }))
        : []
      this.familyProfileOptions = [...this.activeFamilyProfileOptions]
    } catch (error) {
      this.activeFamilyProfileOptions = []
      this.familyProfileOptions = []
      this.$message.error(this.getErrorMessage(error, '加载家属档案列表失败'))
    }
  }

  private async handleAdd() {
    this.reset()
    this.title = '新增老人档案'
    await this.loadActiveFamilyProfileOptions()
    this.familyProfileOptions = [...this.activeFamilyProfileOptions]
    this.open = true
  }

  private async handleEdit(row: ElderlyFormData) {
    this.reset()
    try {
      await this.loadActiveFamilyProfileOptions()
      const elderlyData = this.unwrapData<ElderlyFormData>(await queryElderlyById(Number(row.id)), '获取老人详情失败')
      const selectedFamilyProfileId = this.resolveSelectedFamilyProfileId(elderlyData)
      this.form = {
        ...this.createDefaultForm(),
        ...elderlyData,
        selectedFamilyProfileId
      }
      this.title = '修改老人档案'
      this.open = true
    } catch (error) {
      this.$message.error(this.getErrorMessage(error, '获取老人详情失败'))
    }
  }

  private async submitForm() {
    (this.$refs.form as any).validate(async (valid: boolean) => {
      if (!valid) {
        return
      }

      const selectedOption = this.familyProfileOptions.find((item) => item.id === this.form.selectedFamilyProfileId)
      if (!selectedOption || !selectedOption.userId) {
        this.$message.error('请选择关联家属')
        return
      }

      const payload: ElderlyFormData = {
        id: this.form.id,
        userId: selectedOption.userId,
        name: this.form.name,
        gender: this.form.gender,
        age: this.form.age,
        phone: this.form.phone,
        address: this.form.address,
        diningPointId: this.form.diningPointId,
        gridCode: this.form.gridCode,
        healthInfo: this.form.healthInfo,
        specialNeeds: this.form.specialNeeds,
        idCard: this.form.idCard,
        image: this.form.image
      }

      try {
        if (this.form.id !== undefined) {
          this.unwrapData(await editElderly(payload), '修改失败')
          this.$message.success('修改成功')
        } else {
          this.unwrapData(await addElderly(payload), '新增失败')
          this.$message.success('新增成功')
        }
        this.open = false
        this.getList()
      } catch (error) {
        this.$message.error(this.getErrorMessage(error, '保存失败'))
      }
    })
  }

  private async handleDelete(id?: number) {
    if (id === undefined) {
      return
    }
    this.$confirm('确认删除该老人档案吗？', '提示', {
      type: 'warning'
    }).then(async () => {
      try {
        this.unwrapData(await deleteElderly(id), '删除失败')
        this.$message.success('删除成功')
        this.getList()
      } catch (error) {
        this.$message.error(this.getErrorMessage(error, '删除失败'))
      }
    }).catch(() => {})
  }

  private resolveSelectedFamilyProfileId(elderlyData: ElderlyFormData) {
    const matchedByProfileId = elderlyData.familyProfileId
      ? this.activeFamilyProfileOptions.find((item) => item.id === elderlyData.familyProfileId)
      : undefined
    if (matchedByProfileId) {
      this.familyProfileOptions = [...this.activeFamilyProfileOptions]
      return matchedByProfileId.id
    }

    const matchedByUserId = elderlyData.userId
      ? this.activeFamilyProfileOptions.find((item) => item.userId === elderlyData.userId)
      : undefined
    if (matchedByUserId) {
      this.familyProfileOptions = [...this.activeFamilyProfileOptions]
      return matchedByUserId.id
    }

    if (elderlyData.familyProfileId) {
      const injectedOption: FamilyProfileSelectOption = {
        id: elderlyData.familyProfileId,
        userId: elderlyData.userId || 0,
        username: elderlyData.familyUsername,
        name: elderlyData.familyName || '当前已绑定家属',
        phone: elderlyData.familyPhone,
        disabled: true,
        isInjected: true
      }
      this.familyProfileOptions = [injectedOption, ...this.activeFamilyProfileOptions]
      return injectedOption.id
    }

    this.familyProfileOptions = [...this.activeFamilyProfileOptions]
    return undefined
  }

  private formatFamilyProfileLabel(item: FamilyProfileSelectOption) {
    const name = item.name || '未命名家属'
    const phone = item.phone || '-'
    const username = item.username || `ID:${item.userId}`
    const suffix = item.isInjected ? '（当前绑定，已停用或当前不可选）' : ''
    return `${name}（${phone} / ${username}）${suffix}`
  }

  private reset() {
    this.form = this.createDefaultForm()
    this.familyProfileOptions = [...this.activeFamilyProfileOptions]
  }

  private cancel() {
    this.open = false
    this.reset()
  }

  private handleSizeChange(val: number) {
    this.pageSize = val
    this.page = 1
    this.getList()
  }

  private handleCurrentChange(val: number) {
    this.page = val
    this.getList()
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 30px;

  .container {
    background: #fff;
    position: relative;
    z-index: 1;
    padding: 30px 28px;
    border-radius: 4px;

    .tableBar {
      margin-bottom: 20px;

      .tableLab {
        float: right;
      }
    }

    .tableBox {
      width: 100%;
      border: 1px solid $gray-5;
      border-bottom: 0;
    }

    .pageList {
      text-align: center;
      margin-top: 20px;
    }
  }
}
</style>
