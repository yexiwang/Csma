<template>
  <div class="volunteer-page">
    <div class="task-panel">
      <div class="task-panel-header">
        <div class="task-panel-copy">
          <div class="task-panel-title">
            我的任务
          </div>
          <div class="task-panel-desc">
            按任务状态处理取餐与配送，操作完成后自动刷新对应列表。
          </div>
        </div>

        <div class="tabs">
          <div
            v-for="tab in tabs"
            :key="String(tab.value)"
            :class="['tab-item', { active: activeTab === tab.value }]"
            @click="changeTab(tab.value)"
          >
            {{ tab.label }}
          </div>
        </div>
      </div>

      <el-row v-loading="loading" :gutter="20">
        <el-col
          v-for="item in tableData"
          :key="item.id"
          :xs="24"
          :sm="12"
          :lg="8"
          class="task-col"
        >
          <el-card shadow="hover" class="task-card">
            <div slot="header" class="task-header">
              <span>订单号：{{ item.number }}</span>
              <el-tag :type="getOrderStatusTag(item.status)">
                {{ getOrderStatusText(item.status) }}
              </el-tag>
            </div>

            <div class="task-content">
              <p><strong>服务老人：</strong>{{ getDisplayElderName(item) }}</p>
              <p><strong>助餐点：</strong>{{ item.diningPointName || '--' }}</p>
              <p><strong>联系人：</strong>{{ item.consignee || '--' }} {{ item.phone || '' }}</p>
              <p><strong>地址：</strong>{{ item.address || '--' }}</p>
              <p><strong>下单时间：</strong>{{ item.orderTime || '--' }}</p>
              <p v-if="item.remark">
                <strong>备注：</strong>{{ item.remark }}
              </p>
            </div>

            <div class="task-actions">
              <el-button
                v-if="item.status === ORDER_STATUS.MEAL_READY"
                type="primary"
                @click="handlePickup(item.id)"
              >
                确认取餐
              </el-button>
              <el-button
                v-if="item.status === ORDER_STATUS.DELIVERY_IN_PROGRESS"
                type="success"
                @click="openCompleteDialog(item)"
              >
                完成配送
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty
        v-if="!loading && tableData.length === 0"
        description="当前没有任务"
      />

      <el-pagination
        v-if="total > 0"
        class="pageList"
        :page-sizes="[6, 12, 18]"
        :page-size="pageSize"
        :current-page="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      title="确认完成配送"
      :visible.sync="completeDialogVisible"
      width="520px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :show-close="!completeSubmitting"
      :close-on-press-escape="!completeSubmitting"
      @close="handleCompleteDialogClose"
    >
      <div v-if="pendingCompleteTask" class="complete-dialog">
        <div class="complete-dialog-card">
          <div class="complete-dialog-row">
            <span class="label">订单号</span>
            <span class="value">{{ pendingCompleteTask.number || '--' }}</span>
          </div>
          <div class="complete-dialog-row">
            <span class="label">服务老人</span>
            <span class="value">{{ getDisplayElderName(pendingCompleteTask) }}</span>
          </div>
          <div class="complete-dialog-row">
            <span class="label">收餐地址</span>
            <span class="value address">{{ pendingCompleteTask.address || '--' }}</span>
          </div>
        </div>

        <div class="complete-dialog-tip">
          请确认已将餐品送达老人或收餐人。
        </div>
      </div>

      <div slot="footer" class="complete-dialog-footer">
        <el-button :disabled="completeSubmitting" @click="completeDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="completeSubmitting" @click="submitCompleteDialog">
          确认完成
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  getVolunteerOrderPage,
  VolunteerTaskItem,
  volunteerConfirmComplete,
  volunteerConfirmPickup
} from '@/api/volunteer'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'

interface VolunteerTab {
  label: string
  value: number
}

@Component({
  name: 'VolunteerTask'
})
export default class VolunteerTask extends Vue {
  private readonly ORDER_STATUS = ORDER_STATUS
  private readonly tabs: VolunteerTab[] = [
    { label: '待取餐', value: ORDER_STATUS.MEAL_READY },
    { label: '配送中', value: ORDER_STATUS.DELIVERY_IN_PROGRESS },
    { label: '已完成', value: ORDER_STATUS.COMPLETED }
  ]

  private tableData: VolunteerTaskItem[] = []
  private loading = false
  private total = 0
  private page = 1
  private pageSize = 6
  private activeTab = ORDER_STATUS.MEAL_READY
  private latestLoadToken = 0
  private completeDialogVisible = false
  private completeSubmitting = false
  private pendingCompleteTask: VolunteerTaskItem | null = null

  created() {
    this.refreshCurrentTab()
  }

  get currentStatus() {
    return this.activeTab
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private getDisplayElderName(item: VolunteerTaskItem | null) {
    return item && (item.elderName || item.consignee)
      ? (item.elderName || item.consignee) as string
      : '历史订单未记录服务老人'
  }

  private async loadTasks(status: number = this.currentStatus) {
    const loadToken = ++this.latestLoadToken
    this.loading = true

    try {
      const pageData = await getVolunteerOrderPage({
        page: this.page,
        pageSize: this.pageSize,
        status
      })

      if (loadToken !== this.latestLoadToken) {
        return
      }

      this.tableData = pageData.records || []
      this.total = Number(pageData.total || 0)
    } catch (error) {
      if (loadToken !== this.latestLoadToken) {
        return
      }

      this.tableData = []
      this.total = 0
      this.$message.error(this.resolveErrorMessage(error, '获取任务列表失败，请重试'))
    } finally {
      if (loadToken === this.latestLoadToken) {
        this.loading = false
      }
    }
  }

  private refreshCurrentTab() {
    return this.loadTasks(this.currentStatus)
  }

  private async setActiveTab(
    status: number,
    options: { resetPage?: boolean; reload?: boolean } = {}
  ) {
    const { resetPage = false, reload = true } = options
    this.activeTab = status

    if (resetPage) {
      this.page = 1
    }

    if (reload) {
      await this.loadTasks(status)
    }
  }

  private changeTab(status: number) {
    if (this.activeTab === status) {
      return
    }

    this.setActiveTab(status, { resetPage: true, reload: true })
  }

  private async handlePickup(id: number) {
    try {
      await this.$confirm('确认已经在助餐点取到餐品？', '提示', { type: 'warning' })
      await volunteerConfirmPickup(id)
      this.$message.success('已确认取餐，订单已进入配送中')
      await this.setActiveTab(ORDER_STATUS.DELIVERY_IN_PROGRESS, { resetPage: true, reload: true })
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        this.$message.error(this.resolveErrorMessage(error, '确认取餐失败，请重试'))
      }
    }
  }

  private openCompleteDialog(item: VolunteerTaskItem) {
    this.pendingCompleteTask = { ...item }
    this.completeDialogVisible = true
  }

  private async submitCompleteDialog() {
    if (!this.pendingCompleteTask) {
      this.closeCompleteDialog()
      return
    }

    const task = { ...this.pendingCompleteTask }
    this.completeSubmitting = true

    try {
      await volunteerConfirmComplete(task.id)
      this.completeSubmitting = false
      this.closeCompleteDialog()
      this.$message.success('订单已完成配送')
      await this.setActiveTab(ORDER_STATUS.COMPLETED, { resetPage: true, reload: true })
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '完成配送失败，请重试'))
    } finally {
      this.completeSubmitting = false
    }
  }

  private handleCompleteDialogClose() {
    if (this.completeSubmitting) {
      return
    }

    this.closeCompleteDialog()
  }

  private closeCompleteDialog() {
    this.completeDialogVisible = false
    this.completeSubmitting = false
    this.pendingCompleteTask = null
  }

  private handleSizeChange(pageSize: number) {
    this.pageSize = pageSize
    this.page = 1
    this.refreshCurrentTab()
  }

  private handleCurrentChange(page: number) {
    this.page = page
    this.refreshCurrentTab()
  }

  private resolveErrorMessage(error: any, fallbackMessage: string) {
    const responseMessage = error && error.response && error.response.data && error.response.data.msg
    const directMessage = error && error.message
    return responseMessage || directMessage || fallbackMessage
  }
}
</script>

<style lang="scss" scoped>
.volunteer-page {
  padding: 24px;
}

.task-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.task-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.task-panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.task-panel-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tab-item {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f4f4f5;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    background: #f8b500;
    color: #fff;
    font-weight: 600;
    box-shadow: 0 8px 18px rgba(248, 181, 0, 0.18);
  }
}

.task-col {
  margin-bottom: 20px;
}

.task-card {
  min-height: 260px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.task-content p {
  margin: 10px 0;
  color: #606266;
  line-height: 1.5;
}

.task-actions {
  margin-top: 16px;
  text-align: right;
}

.pageList {
  margin-top: 8px;
  text-align: center;
}

.complete-dialog-card {
  padding: 18px;
  border-radius: 14px;
  background: #fff9e8;
}

.complete-dialog-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }

  .label {
    width: 72px;
    color: #909399;
    flex-shrink: 0;
    line-height: 1.6;
  }

  .value {
    min-width: 0;
    color: #303133;
    line-height: 1.6;
    word-break: break-word;
    overflow-wrap: anywhere;
  }
}

.complete-dialog-tip {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f4f4f5;
  color: #606266;
  line-height: 1.6;
}

.complete-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .volunteer-page {
    padding: 16px;
  }

  .task-panel-header {
    flex-direction: column;
  }

  .complete-dialog-row {
    flex-direction: column;
    gap: 6px;

    .label {
      width: auto;
    }
  }
}
</style>
