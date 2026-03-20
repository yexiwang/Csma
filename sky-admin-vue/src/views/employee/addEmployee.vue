<template>
  <div class="addBrand-container">
    <HeadLable :title="title" :goback="true" />
    <div class="container">
      <el-form
        ref="ruleForm"
        :model="ruleForm"
        :rules="rules"
        label-width="180px"
        class="demo-ruleForm"
      >
        <el-form-item label="账号:" prop="username">
          <el-input
            v-model="ruleForm.username"
            placeholder="请输入账号"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="员工姓名:" prop="name">
          <el-input
            v-model="ruleForm.name"
            placeholder="请输入员工姓名"
            maxlength="12"
          />
        </el-form-item>

        <el-form-item label="手机号:" prop="phone">
          <el-input
            v-model="ruleForm.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="性别:" prop="sex">
          <el-radio-group v-model="ruleForm.sex">
            <el-radio label="男" />
            <el-radio label="女" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="身份证号:" prop="idNumber" class="idNumber">
          <el-input
            v-model="ruleForm.idNumber"
            placeholder="请输入身份证号"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="角色:" prop="role">
          <el-select v-model="ruleForm.role" placeholder="请选择角色" @change="handleRoleChange">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="操作员" value="OPERATOR" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="ruleForm.role === 'OPERATOR'" label="助餐点:" prop="diningPointId">
          <el-select
            v-model="ruleForm.diningPointId"
            placeholder="请选择助餐点"
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

        <div class="subBox address">
          <el-button @click="goBack">
            取消
          </el-button>
          <el-button
            type="primary"
            :class="{ continue: actionType === 'add' }"
            @click="submitForm('ruleForm', false)"
          >
            保存
          </el-button>
          <el-button
            v-if="actionType === 'add'"
            type="primary"
            @click="submitForm('ruleForm', true)"
          >
            保存并继续新增
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable/index.vue'
import { queryEmployeeById, addEmployee, editEmployee } from '@/api/employee'
import { getDiningPointPage } from '@/api/diningPoint'

interface DiningPointOption {
  id: number
  name: string
}

interface EmployeeForm {
  id?: number
  name: string
  phone: string
  sex: string
  idNumber: string
  username: string
  role: string
  diningPointId: number | null
}

function createDefaultForm(): EmployeeForm {
  return {
    username: '',
    name: '',
    phone: '',
    sex: '男',
    idNumber: '',
    role: 'ADMIN',
    diningPointId: null
  }
}

@Component({
  name: 'AddEmployee',
  components: {
    HeadLable
  }
})
export default class extends Vue {
  private title = '添加员工'
  private actionType = ''
  private ruleForm: EmployeeForm = createDefaultForm()
  private diningPointOptions: DiningPointOption[] = []

  get rules() {
    return {
      name: [
        {
          required: true,
          validator: (_rule: any, value: string, callback: Function) => {
            if (!value) {
              callback(new Error('请输入员工姓名'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      username: [
        {
          required: true,
          validator: (_rule: any, value: string, callback: Function) => {
            if (!value) {
              callback(new Error('请输入账号'))
              return
            }

            const reg = /^([a-z]|[0-9]){3,20}$/
            if (!reg.test(value)) {
              callback(new Error('账号格式不正确，请输入 3-20 位小写字母或数字'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      phone: [{ required: true, validator: this.checkPhone, trigger: 'blur' }],
      idNumber: [{ required: true, validator: this.validID, trigger: 'blur' }],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }],
      diningPointId: [{ validator: this.validateDiningPoint, trigger: 'change' }]
    }
  }

  created() {
    this.actionType = this.$route.query.id ? 'edit' : 'add'
    if (this.actionType === 'edit') {
      this.title = '修改员工信息'
    }
    this.loadDiningPoints()
    if (this.$route.query.id) {
      this.init()
    }
  }

  private async loadDiningPoints() {
    try {
      const res: any = await getDiningPointPage({ page: 1, pageSize: 200 })
      const pageData = res.data && res.data.data ? res.data.data : res.data
      this.diningPointOptions = pageData && pageData.records ? pageData.records : []
    } catch (error) {
      console.error('加载助餐点失败:', error)
    }
  }

  private isCellPhone(val: string) {
    return /^1(3|4|5|6|7|8|9)\d{9}$/.test(val)
  }

  private checkPhone(_rule: any, value: string, callback: Function) {
    if (!value) {
      callback(new Error('请输入手机号'))
    } else if (!this.isCellPhone(value)) {
      callback(new Error('请输入正确的手机号'))
    } else {
      callback()
    }
  }

  private validID(_rule: any, value: string, callback: Function) {
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    if (!value) {
      callback(new Error('请输入身份证号码'))
    } else if (!reg.test(value)) {
      callback(new Error('身份证号码不正确'))
    } else {
      callback()
    }
  }

  private validateDiningPoint(_rule: any, value: number | null, callback: Function) {
    if (this.ruleForm.role === 'OPERATOR' && !value) {
      callback(new Error('操作员必须绑定助餐点'))
    } else {
      callback()
    }
  }

  private async init() {
    const id = this.$route.query.id
    const res: any = await queryEmployeeById(id)
    if (res.data.code !== 1) {
      this.$message.error(res.data.msg)
      return
    }

    const data = res.data.data || {}
    this.ruleForm = {
      id: data.id,
      username: data.username || '',
      name: data.name || '',
      phone: data.phone || '',
      sex: data.sex === '0' ? '女' : '男',
      idNumber: data.idNumber || '',
      role: data.role || 'ADMIN',
      diningPointId: data.diningPointId || null
    }
  }

  private handleRoleChange(role: string) {
    if (role !== 'OPERATOR') {
      this.ruleForm.diningPointId = null
    }
    const formRef = this.$refs.ruleForm as any
    if (formRef) {
      formRef.clearValidate('diningPointId')
    }
  }

  private goBack() {
    this.$router.push('/employee')
  }

  private buildSubmitParams() {
    return {
      ...this.ruleForm,
      sex: this.ruleForm.sex === '女' ? '0' : '1',
      diningPointId: this.ruleForm.role === 'OPERATOR' ? this.ruleForm.diningPointId : null
    }
  }

  private submitForm(formName: string, stayOnPage: boolean) {
    ;(this.$refs[formName] as any).validate(async (valid: boolean) => {
      if (!valid) {
        return false
      }

      const params = this.buildSubmitParams()
      try {
        if (this.actionType === 'add') {
          const res: any = await addEmployee(params)
          if (res.data.code === 1) {
            this.$message.success('员工添加成功')
            if (stayOnPage) {
              this.ruleForm = createDefaultForm()
            } else {
              this.$router.push({ path: '/employee' })
            }
          } else {
            this.$message.error(res.data.msg)
          }
          return
        }

        const res: any = await editEmployee(params)
        if (res.data.code === 1) {
          this.$message.success('员工信息修改成功')
          this.$router.push({ path: '/employee' })
        } else {
          this.$message.error(res.data.msg)
        }
      } catch (error) {
        console.error(error)
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.addBrand {
  &-container {
    margin: 30px;
    margin-top: 0;

    .HeadLable {
      background-color: transparent;
      margin-bottom: 0;
      padding-left: 0;
    }

    .container {
      position: relative;
      z-index: 1;
      background: #fff;
      padding: 30px;
      border-radius: 4px;

      .subBox {
        padding-top: 30px;
        text-align: center;
        border-top: solid 1px $gray-5;
      }
    }

    .idNumber {
      margin-bottom: 39px;
    }

    .el-form-item {
      margin-bottom: 29px;
    }

    .el-input,
    .el-select {
      width: 293px;
    }
  }
}
</style>
