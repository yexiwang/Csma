<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">员工姓名：</label>
        <el-input
          v-model="input"
          placeholder="请输入员工姓名"
          style="width: 15%"
          clearable
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <label style="margin: 0 5px 0 20px">员工角色：</label>
        <el-select
          v-model="selectedRole"
          placeholder="全部角色"
          clearable
          style="width: 160px"
          @change="handleQuery"
        >
          <el-option
            v-for="item in roleOptions"
            :key="item.value || 'ALL'"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <label style="margin: 0 5px 0 20px">所属助餐点：</label>
        <el-select
          v-model="selectedDiningPointId"
          placeholder="全部助餐点"
          clearable
          filterable
          style="width: 180px"
          @change="handleQuery"
        >
          <el-option
            v-for="item in diningPointOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-button class="normal-btn continue" @click="handleQuery">
          查询
        </el-button>
        <el-button
          type="primary"
          style="float: right"
          @click="addEmployeeHandle('add')"
        >
          + 添加员工
        </el-button>
      </div>

      <el-table
        v-if="tableData.length"
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column prop="name" label="员工姓名" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="角色">
          <template slot-scope="scope">
            {{ getRoleLabel(scope.row.role) }}
          </template>
        </el-table-column>
        <el-table-column label="助餐点ID">
          <template slot-scope="scope">
            {{ scope.row.diningPointId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="账号状态">
          <template slot-scope="scope">
            <div
              class="tableColumn-status"
              :class="{ 'stop-use': String(scope.row.status) === '0' }"
            >
              {{ String(scope.row.status) === '0' ? '禁用' : '启用' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" />
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              class="blueBug"
              :class="{ 'disabled-text': scope.row.username === 'admin' }"
              :disabled="scope.row.username === 'admin'"
              @click="addEmployeeHandle(scope.row.id, scope.row.username)"
            >
              修改
            </el-button>
            <el-button
              :disabled="scope.row.username === 'admin'"
              type="text"
              size="small"
              class="non"
              :class="{
                'disabled-text': scope.row.username === 'admin',
                blueBug: scope.row.status == '0',
                delBut: scope.row.status != '0'
              }"
              @click="statusHandle(scope.row)"
            >
              {{ scope.row.status == '1' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Empty v-else :is-search="isSearch" />

      <el-pagination
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="counts"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable/index.vue'
import { getEmployeeList, enableOrDisableEmployee } from '@/api/employee'
import { DiningPointOption, getDiningPointList } from '@/api/diningPoint'
import InputAutoComplete from '@/components/InputAutoComplete/index.vue'
import Empty from '@/components/Empty/index.vue'

@Component({
  name: 'Employee',
  components: {
    HeadLable,
    InputAutoComplete,
    Empty
  }
})
export default class extends Vue {
  private input = ''
  private selectedRole: string | null = null
  private selectedDiningPointId: number | null = null
  private counts = 0
  private page = 1
  private pageSize = 10
  private tableData: any[] = []
  private diningPointOptions: DiningPointOption[] = []
  private id = ''
  private status = ''
  private isSearch = false
  private roleOptions = [
    { label: '全部', value: null },
    { label: '管理员', value: 'ADMIN' },
    { label: '操作员', value: 'OPERATOR' }
  ]

  created() {
    this.loadDiningPointOptions()
    this.init()
  }

  private async loadDiningPointOptions() {
    try {
      const res: any = await getDiningPointList()
      if (String(res.data.code) === '1' && Array.isArray(res.data.data)) {
        this.diningPointOptions = res.data.data
      }
    } catch (err) {
      const error = err as Error
      this.$message.error('助餐点列表加载失败：' + error.message)
    }
  }

  private initProp(val: string) {
    this.input = val
    this.handleQuery()
  }

  private handleQuery() {
    this.page = 1
    this.init(true)
  }

  private getRoleLabel(role: string) {
    return role === 'OPERATOR' ? '操作员' : '管理员'
  }

  private async init(isSearch?: boolean) {
    this.isSearch = !!isSearch
    const params = {
      page: this.page,
      pageSize: this.pageSize,
      name: this.input || undefined,
      role: this.selectedRole || undefined,
      diningPointId: this.selectedDiningPointId || undefined
    }

    try {
      const res: any = await getEmployeeList(params)
      if (String(res.data.code) === '1') {
        this.tableData = res.data && res.data.data && res.data.data.records
        this.counts = res.data.data.total
      }
    } catch (err) {
      const error = err as Error
      this.$message.error('请求出错了：' + error.message)
    }
  }

  private addEmployeeHandle(st: string, username?: string) {
    if (st === 'add') {
      this.$router.push({ path: '/employee/add' })
      return
    }

    if (username === 'admin') {
      return
    }

    this.$router.push({ path: '/employee/add', query: { id: st } })
  }

  private statusHandle(row: any) {
    if (row.username === 'admin') {
      return
    }

    this.id = row.id
    this.status = row.status
    this.$confirm('确认调整该账号的状态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      enableOrDisableEmployee({ id: this.id, status: !this.status ? 1 : 0 })
        .then((res: any) => {
          if (String(res.status) === '200') {
            this.$message.success('账号状态更改成功！')
            this.init()
          }
        })
        .catch((err: any) => {
          this.$message.error('请求出错了：' + err.message)
        })
    })
  }

  private handleSizeChange(val: number) {
    this.pageSize = val
    this.init()
  }

  private handleCurrentChange(val: number) {
    this.page = val
    this.init()
  }
}
</script>

<style lang="scss" scoped>
.disabled-text {
  color: #bac0cd !important;
}
</style>
