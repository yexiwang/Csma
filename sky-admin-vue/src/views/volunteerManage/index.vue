<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label>姓名：</label>
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入志愿者姓名"
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
        <label>账号：</label>
        <el-input
          v-model="queryParams.username"
          clearable
          placeholder="请输入账号"
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
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button class="normal-btn continue" @click="handleQuery">
          查询
        </el-button>
        <el-button @click="resetQuery">
          重置
        </el-button>
        <div class="tableLab">
          <el-button type="primary" @click="handleAdd">
            + 新增志愿者
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180" align="center">
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
              {{ row.status === 1 ? '禁用' : '启用' }}
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
      :title="dialogMode === 'add' ? '新增志愿者' : '编辑志愿者'"
      :visible.sync="dialogVisible"
      width="520px"
      append-to-body
      @close="handleDialogClose"
    >
      <el-form ref="volunteerForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="dialogMode === 'add'" label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" maxlength="32" />
        </el-form-item>
        <el-form-item v-else label="账号">
          <el-input :value="form.username" disabled />
        </el-form-item>

        <el-form-item v-if="dialogMode === 'add'" label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            maxlength="64"
          />
        </el-form-item>

        <el-form-item v-if="dialogMode === 'add'" label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
            maxlength="64"
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="32" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">
              启用
            </el-radio>
            <el-radio :label="0">
              禁用
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
  addVolunteer,
  getVolunteerById,
  getVolunteerPage,
  updateVolunteer,
  updateVolunteerStatus,
  VolunteerFormPayload,
  VolunteerItem
} from '@/api/adminVolunteer'

type DialogMode = 'add' | 'edit'

interface VolunteerForm extends VolunteerFormPayload {
  confirmPassword: string
}

@Component({
  name: 'VolunteerManage'
})
export default class VolunteerManage extends Vue {
  private loading = false
  private submitting = false
  private tableData: VolunteerItem[] = []
  private total = 0
  private page = 1
  private pageSize = 10
  private dialogVisible = false
  private dialogMode: DialogMode = 'add'
  private queryParams = {
    name: '',
    phone: '',
    username: '',
    status: undefined as number | undefined
  }

  private form: VolunteerForm = this.createDefaultForm()

  created() {
    this.getList()
  }

  get rules() {
    return {
      username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
      password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
      confirmPassword: [{
        validator: (_rule: any, value: string, callback: (error?: Error) => void) =>
          this.validateConfirmPassword(_rule, value, callback),
        trigger: 'blur'
      }],
      name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
      phone: [
        { required: true, message: '手机号不能为空', trigger: 'blur' },
        { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }]
    }
  }

  private createDefaultForm(): VolunteerForm {
    return {
      id: undefined,
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      phone: '',
      status: 1
    }
  }

  private unwrapPayload(res: any) {
    return res && res.data && typeof res.data !== 'undefined'
      ? (typeof res.data.data !== 'undefined' ? res.data.data : res.data)
      : res
  }

  private assertSuccess(res: any, fallback: string) {
    if (!res || !res.data || String(res.data.code) !== '1') {
      throw new Error((res && res.data && res.data.msg) || fallback)
    }
    return this.unwrapPayload(res)
  }

  private validateConfirmPassword(_rule: any, value: string, callback: (error?: Error) => void) {
    if (this.dialogMode !== 'add') {
      callback()
      return
    }
    if (!value) {
      callback(new Error('确认密码不能为空'))
      return
    }
    if (value !== this.form.password) {
      callback(new Error('两次输入的密码不一致'))
      return
    }
    callback()
  }

  private async getList() {
    this.loading = true
    try {
      const res = await getVolunteerPage({
        page: this.page,
        pageSize: this.pageSize,
        name: this.queryParams.name || undefined,
        phone: this.queryParams.phone || undefined,
        username: this.queryParams.username || undefined,
        status: this.queryParams.status
      })
      const payload = this.assertSuccess(res, '获取志愿者列表失败')
      this.tableData = Array.isArray(payload && payload.records) ? payload.records : []
      this.total = payload && payload.total ? payload.total : 0
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '获取志愿者列表失败'))
    } finally {
      this.loading = false
    }
  }

  private handleQuery() {
    this.page = 1
    this.getList()
  }

  private resetQuery() {
    this.queryParams = {
      name: '',
      phone: '',
      username: '',
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

  private handleAdd() {
    this.dialogMode = 'add'
    this.form = this.createDefaultForm()
    this.dialogVisible = true
  }

  private async handleEdit(row: VolunteerItem) {
    try {
      this.dialogMode = 'edit'
      const res = await getVolunteerById(row.id)
      const payload = this.assertSuccess(res, '获取志愿者详情失败') || {}
      this.form = {
        id: payload.id,
        username: payload.username || '',
        password: '',
        confirmPassword: '',
        name: payload.name || '',
        phone: payload.phone || '',
        status: typeof payload.status === 'number' ? payload.status : 1
      }
      this.dialogVisible = true
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '获取志愿者详情失败'))
    }
  }

  private async handleStatusChange(row: VolunteerItem) {
    const targetStatus = row.status === 1 ? 0 : 1
    const actionText = targetStatus === 1 ? '启用' : '禁用'
    try {
      await this.$confirm(`确认${actionText}该志愿者账号吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await updateVolunteerStatus({ id: row.id, status: targetStatus })
      this.assertSuccess(res, `${actionText}失败`)
      this.$message.success(`${actionText}成功`)
      this.getList()
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      this.$message.error(this.resolveErrorMessage(error, `${actionText}失败`))
    }
  }

  private async submitForm() {
    const formRef = this.$refs.volunteerForm as any
    formRef.validate(async (valid: boolean) => {
      if (!valid) {
        return
      }
      this.submitting = true
      try {
        if (this.dialogMode === 'add') {
          const res = await addVolunteer({
            username: this.form.username.trim(),
            password: this.form.password,
            name: this.form.name.trim(),
            phone: this.form.phone.trim(),
            status: this.form.status
          })
          this.assertSuccess(res, '新增志愿者失败')
          this.$message.success('新增志愿者成功')
        } else {
          const res = await updateVolunteer({
            id: this.form.id,
            name: this.form.name.trim(),
            phone: this.form.phone.trim(),
            status: this.form.status
          })
          this.assertSuccess(res, '编辑志愿者失败')
          this.$message.success('编辑志愿者成功')
        }
        this.dialogVisible = false
        this.getList()
      } catch (error) {
        this.$message.error(this.resolveErrorMessage(error, this.dialogMode === 'add' ? '新增志愿者失败' : '编辑志愿者失败'))
      } finally {
        this.submitting = false
      }
    })
  }

  private handleDialogClose() {
    const formRef = this.$refs.volunteerForm as any
    if (formRef) {
      formRef.resetFields()
    }
    this.form = this.createDefaultForm()
    this.dialogMode = 'add'
  }

  private resolveErrorMessage(error: any, fallback: string) {
    return (error && error.response && error.response.data && error.response.data.msg) ||
      (error && error.message) ||
      fallback
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
