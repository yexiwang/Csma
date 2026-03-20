<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>家属姓名：</label>
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入家属姓名"
          style="width: 14%"
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <label>手机号：</label>
        <el-input
          v-model="queryParams.phone"
          clearable
          placeholder="请输入手机号"
          style="width: 14%"
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <label>状态：</label>
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="全部状态"
          style="width: 120px"
          @change="handleQuery"
        >
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button class="normal-btn continue" @click="handleQuery">
          查询
        </el-button>
        <el-button @click="resetQuery">
          重置
        </el-button>
        <div class="tableLab">
          <el-button type="primary" @click="handleAdd">
            + 新增家属档案
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column prop="name" label="家属姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="username" label="绑定账号" min-width="160" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elderlyCount" label="已关联老人数量" width="140" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="220" align="center">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="blueBug"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              :class="row.status === 1 ? 'delBut' : 'blueBug'"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delBut non"
              @click="handleDelete(row)"
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
      :title="dialogMode === 'add' ? '新增家属档案' : '编辑家属档案'"
      :visible.sync="dialogVisible"
      width="560px"
      append-to-body
      @close="handleDialogClose"
    >
      <el-form ref="familyProfileForm" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="家属姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入家属姓名" maxlength="50" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>

        <el-form-item label="绑定FAMILY账号" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            placeholder="请选择FAMILY用户账号"
            style="width: 100%"
          >
            <el-option
              v-for="item in familyUserOptions"
              :key="item.userId"
              :label="formatFamilyUserLabel(item)"
              :value="item.userId"
              :disabled="isFamilyUserDisabled(item)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            placeholder="请输入备注"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">
              启用
            </el-radio>
            <el-radio :label="0">
              停用
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  addFamilyProfile,
  deleteFamilyProfile,
  FamilyProfileFormData,
  FamilyProfileItem,
  FamilyUserOption,
  getFamilyProfileById,
  getFamilyProfilePage,
  getFamilyUsers,
  updateFamilyProfile,
  updateFamilyProfileStatus
} from '@/api/familyProfile'

type DialogMode = 'add' | 'edit'

interface PageResponse<T> {
  total?: number
  records?: T[]
}

@Component({
  name: 'FamilyProfileManage'
})
export default class FamilyProfileManage extends Vue {
  private loading = false
  private submitting = false
  private tableData: FamilyProfileItem[] = []
  private total = 0
  private page = 1
  private pageSize = 10
  private dialogVisible = false
  private dialogMode: DialogMode = 'add'
  private familyUserOptions: FamilyUserOption[] = []
  private queryParams = {
    name: '',
    phone: '',
    status: undefined as number | undefined
  }

  private form: FamilyProfileFormData = this.createDefaultForm()

  created() {
    this.getList()
  }

  get rules() {
    return {
      name: [{ required: true, message: '家属姓名不能为空', trigger: 'blur' }],
      userId: [{ required: true, message: '请选择绑定的FAMILY账号', trigger: 'change' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }]
    }
  }

  private createDefaultForm(): FamilyProfileFormData {
    return {
      id: undefined,
      userId: undefined,
      name: '',
      phone: '',
      remark: '',
      status: 1
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

  private resolveErrorMessage(error: any, fallback: string) {
    return (error && error.response && error.response.data && error.response.data.msg) ||
      (error && error.message) ||
      fallback
  }

  private async getList() {
    this.loading = true
    try {
      const params = {
        page: this.page,
        pageSize: this.pageSize,
        name: this.queryParams.name.trim() || undefined,
        phone: this.queryParams.phone.trim() || undefined,
        status: this.queryParams.status
      }
      const pageData = this.unwrapPage<FamilyProfileItem>(await getFamilyProfilePage(params), '获取家属档案列表失败')
      this.tableData = Array.isArray(pageData.records) ? pageData.records : []
      this.total = pageData.total || 0
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '获取家属档案列表失败'))
    } finally {
      this.loading = false
    }
  }

  private async loadFamilyUsers() {
    const data = this.unwrapData<FamilyUserOption[]>(await getFamilyUsers(), '获取FAMILY用户列表失败')
    this.familyUserOptions = Array.isArray(data)
      ? data.map((item) => ({
        ...item,
        bound: Boolean(item.bound)
      }))
      : []
  }

  private handleQuery() {
    this.page = 1
    this.getList()
  }

  private resetQuery() {
    this.queryParams = {
      name: '',
      phone: '',
      status: undefined
    }
    this.page = 1
    this.getList()
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

  private async handleAdd() {
    this.dialogMode = 'add'
    this.form = this.createDefaultForm()
    try {
      await this.loadFamilyUsers()
      this.dialogVisible = true
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '获取FAMILY用户列表失败'))
    }
  }

  private async handleEdit(row: FamilyProfileItem) {
    this.dialogMode = 'edit'
    this.form = this.createDefaultForm()
    try {
      await this.loadFamilyUsers()
      const detail = this.unwrapData<FamilyProfileItem>(await getFamilyProfileById(row.id), '获取家属档案详情失败')
      this.form = {
        id: detail.id,
        userId: detail.userId,
        name: detail.name || '',
        phone: detail.phone || '',
        remark: detail.remark || '',
        status: typeof detail.status === 'number' ? detail.status : 1
      }
      this.dialogVisible = true
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '获取家属档案详情失败'))
    }
  }

  private async handleStatusChange(row: FamilyProfileItem) {
    const targetStatus = row.status === 1 ? 0 : 1
    const actionText = targetStatus === 1 ? '启用' : '停用'
    try {
      await this.$confirm(`确认${actionText}该家属档案吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      this.unwrapData(await updateFamilyProfileStatus(row.id, targetStatus), `${actionText}失败`)
      this.$message.success(`${actionText}成功`)
      this.getList()
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      this.$message.error(this.resolveErrorMessage(error, `${actionText}失败`))
    }
  }

  private async handleDelete(row: FamilyProfileItem) {
    try {
      await this.$confirm(`确认删除家属档案“${row.name}”吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      this.unwrapData(await deleteFamilyProfile(row.id), '删除失败')
      this.$message.success('删除成功')
      this.getList()
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      this.$message.error(this.resolveErrorMessage(error, '删除失败'))
    }
  }

  private async submitForm() {
    const formRef = this.$refs.familyProfileForm as any
    formRef.validate(async (valid: boolean) => {
      if (!valid) {
        return
      }
      this.submitting = true
      try {
        const payload: FamilyProfileFormData = {
          id: this.form.id,
          userId: this.form.userId,
          name: this.form.name.trim(),
          phone: this.form.phone ? this.form.phone.trim() : '',
          remark: this.form.remark ? this.form.remark.trim() : '',
          status: this.form.status
        }
        if (this.dialogMode === 'add') {
          this.unwrapData(await addFamilyProfile(payload), '新增家属档案失败')
          this.$message.success('新增家属档案成功')
        } else {
          this.unwrapData(await updateFamilyProfile(payload), '编辑家属档案失败')
          this.$message.success('编辑家属档案成功')
        }
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        this.$message.error(this.resolveErrorMessage(error, this.dialogMode === 'add' ? '新增家属档案失败' : '编辑家属档案失败'))
      } finally {
        this.submitting = false
      }
    })
  }

  private handleDialogClose() {
    const formRef = this.$refs.familyProfileForm as any
    if (formRef) {
      formRef.resetFields()
    }
    this.form = this.createDefaultForm()
    this.dialogMode = 'add'
  }

  private isFamilyUserDisabled(item: FamilyUserOption) {
    if (!item || !Boolean(item.bound)) {
      return false
    }
    return !(this.dialogMode === 'edit' && item.boundFamilyProfileId === this.form.id)
  }

  private formatFamilyUserLabel(item: FamilyUserOption) {
    const name = item.name || item.boundFamilyProfileName || `用户${item.userId}`
    const phone = item.phone || '-'
    const username = item.username || `ID:${item.userId}`
    const suffix = this.isFamilyUserDisabled(item) ? '（已绑定）' : ''
    return `${name}（${phone} / ${username}）${suffix}`
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
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 20px;

      label {
        color: #606266;
        white-space: nowrap;
      }

      .tableLab {
        margin-left: auto;
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
