<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 10px">助餐点名称：</label>
        <el-input
          v-model="queryParams.name"
          placeholder="请输入助餐点名称"
          style="width: 14%"
          clearable
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <div class="tableLab">
          <el-button type="primary" @click="handleAdd">
            + 新建助餐点
          </el-button>
        </div>
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="tableBox"
      >
        <el-table-column prop="name" label="助餐点名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="operatingHours" label="营业时间" />
        <el-table-column label="状态">
          <template slot-scope="scope">
            <div
              class="tableColumn-status"
              :class="{ 'stop-use': String(scope.row.status) === '0' }"
            >
              {{ String(scope.row.status) === '0' ? '休息中' : '营业中' }}
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
              @click="handleEdit(scope.row)"
            >
              修改
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delBut non"
              @click="handleDelete(scope.row.id)"
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
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="500px"
      append-to-body
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入助餐点名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="operatingHours">
          <el-input v-model="form.operatingHours" placeholder="例如: 08:00-20:00" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">
              营业
            </el-radio>
            <el-radio :label="0">
              休息
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">
          确 定
        </el-button>
        <el-button @click="cancel">
          取 消
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  getDiningPointPage,
  addDiningPoint,
  editDiningPoint,
  deleteDiningPoint,
  queryDiningPointById
} from '@/api/diningPoint'

@Component({
  name: 'DiningPoint'
})
export default class extends Vue {
  private tableData: any[] = []
  private loading = false
  private total = 0
  private page = 1
  private pageSize = 10
  private queryParams = {
    name: ''
  }
  private ids: any[] = []
  private open = false
  private title = ''
  private form: any = {
    id: undefined,
    name: undefined,
    address: undefined,
    contactPhone: undefined,
    operatingHours: undefined,
    status: 1
  }
  private rules = {
    name: [{ required: true, message: '助餐点名称不能为空', trigger: 'blur' }],
    address: [{ required: true, message: '地址不能为空', trigger: 'blur' }]
  }

  created() {
    this.getList()
  }

  private async getList() {
    this.loading = true
    try {
      const res = await getDiningPointPage({
        page: this.page,
        pageSize: this.pageSize,
        name: this.queryParams.name
      })
      const pageData = res.data && res.data.data ? res.data.data : res.data
      this.tableData = pageData && pageData.records ? pageData.records : []
      this.total = pageData && pageData.total ? pageData.total : 0
    } finally {
      this.loading = false
    }
  }

  private handleQuery() {
    this.page = 1
    this.getList()
  }

  private handleAdd() {
    this.reset()
    this.open = true
    this.title = '添加助餐点'
  }

  private async handleEdit(row: any) {
    this.reset()
    const id = row.id || this.ids
    const res = await queryDiningPointById(id)
    this.form = res.data && res.data.data ? res.data.data : res.data
    this.open = true
    this.title = '修改助餐点'
  }

  private async submitForm() {
    (this.$refs['form'] as any).validate(async (valid: boolean) => {
      if (valid) {
        if (this.form.id != undefined) {
          await editDiningPoint(this.form)
          this.$message.success('修改成功')
        } else {
          await addDiningPoint(this.form)
          this.$message.success('新增成功')
        }
        this.open = false
        this.getList()
      }
    })
  }

  private async handleDelete(id: number) {
    this.$confirm('确认删除该助餐点吗?', '提示', {
      type: 'warning'
    }).then(async () => {
      await deleteDiningPoint(id)
      this.$message.success('删除成功')
      this.getList()
    })
  }

  private reset() {
    this.form = {
      id: undefined,
      name: undefined,
      address: undefined,
      contactPhone: undefined,
      operatingHours: undefined,
      status: 1
    }
  }

  private cancel() {
    this.open = false
    this.reset()
  }

  private handleSizeChange(val: number) {
    this.pageSize = val
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
        span {
          cursor: pointer;
          display: inline-block;
          font-size: 14px;
          padding: 0 20px;
          color: $gray-2;
        }
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
