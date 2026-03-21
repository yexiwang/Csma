<template>
  <div class="volunteer-overview-page">
    <div v-loading="overviewLoading" class="overview-panel">
      <div class="overview-header">
        <div class="overview-copy">
          <div class="overview-eyebrow">
            志愿者个人概览
          </div>
          <h1>{{ volunteerName }}</h1>
          <p>查看个人服务画像、累计服务表现和当前服务状态。</p>
        </div>

        <div class="overview-actions">
          <div class="overview-status">
            <div class="status-label">
              当前服务状态
            </div>
            <el-tag :type="volunteerStatusTag" size="medium">
              {{ volunteerStatusText }}
            </el-tag>
          </div>

          <el-button
            type="primary"
            plain
            icon="el-icon-download"
            :loading="exportLoading"
            @click="handleExport"
          >
            导出
          </el-button>
        </div>
      </div>

      <div v-if="overviewLoadFailed" class="overview-fallback">
        概览数据暂时不可用，已显示默认信息，不影响后续查看任务。
      </div>

      <div class="overview-grid">
        <el-card
          v-for="card in overviewCards"
          :key="card.key"
          shadow="never"
          class="overview-stat-card"
        >
          <div class="stat-label">
            {{ card.label }}
          </div>
          <div class="stat-value">
            {{ card.value }}
          </div>
          <div class="stat-desc">
            {{ card.desc }}
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { UserModule } from '@/store/modules/user'
import { exportVolunteerOverview, getVolunteerOverview, VolunteerOverview } from '@/api/volunteer'

interface OverviewCard {
  key: string
  label: string
  value: string
  desc: string
}

@Component({
  name: 'VolunteerOverview'
})
export default class VolunteerOverviewPage extends Vue {
  private overview: VolunteerOverview = this.createDefaultOverview()
  private overviewLoading = false
  private overviewLoadFailed = false
  private exportLoading = false

  created() {
    this.loadVolunteerOverview()
  }

  get volunteerName() {
    return this.overview.name || UserModule.name || '志愿者'
  }

  get volunteerStatusText() {
    if (this.overview.status === 1) {
      return '启用'
    }
    if (this.overview.status === 0) {
      return '停用'
    }
    return '--'
  }

  get volunteerStatusTag() {
    if (this.overview.status === 1) {
      return 'success'
    }
    if (this.overview.status === 0) {
      return 'info'
    }
    return 'info'
  }

  get overviewCards(): OverviewCard[] {
    return [
      {
        key: 'totalOrders',
        label: '累计服务单量',
        value: String(Number(this.overview.totalOrders || 0)),
        desc: '累计完成配送订单'
      },
      {
        key: 'totalHours',
        label: '累计服务时长',
        value: `${Number(this.overview.totalHours || 0).toFixed(1)} 小时`,
        desc: '按历史统计时长累计'
      },
      {
        key: 'rating',
        label: '综合评分',
        value: this.hasNumericValue(this.overview.rating) ? Number(this.overview.rating).toFixed(1) : '--',
        desc: '当前综合服务评分'
      },
      {
        key: 'level',
        label: '等级',
        value: this.overview.level || this.overview.level === 0 ? `Lv.${this.overview.level}` : '--',
        desc: '按累计完成订单量计算'
      }
    ]
  }

  private createDefaultOverview(): VolunteerOverview {
    return {
      name: UserModule.name || '',
      status: undefined,
      totalOrders: 0,
      totalHours: 0,
      rating: undefined,
      level: undefined
    }
  }

  private hasNumericValue(value: any) {
    return value !== undefined && value !== null && value !== '' && !Number.isNaN(Number(value))
  }

  private async loadVolunteerOverview() {
    this.overviewLoading = true
    this.overviewLoadFailed = false

    try {
      const overview = await getVolunteerOverview()
      this.overview = {
        ...this.createDefaultOverview(),
        ...overview,
        name: (overview && overview.name) || UserModule.name || ''
      }
    } catch (_error) {
      this.overviewLoadFailed = true
      this.overview = this.createDefaultOverview()
    } finally {
      this.overviewLoading = false
    }
  }

  private async handleExport() {
    this.exportLoading = true

    try {
      const response = await exportVolunteerOverview()
      const blob = response.data
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = this.resolveDownloadFileName(response.headers && response.headers['content-disposition'])
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (_error) {
      this.$message.error('导出失败，请稍后重试')
    } finally {
      this.exportLoading = false
    }
  }

  private resolveDownloadFileName(contentDisposition?: string) {
    if (!contentDisposition) {
      return '志愿者个人概览.xlsx'
    }

    const utf8Match = contentDisposition.match(/filename\*\s*=\s*UTF-8''([^;]+)/i)
    if (utf8Match && utf8Match[1]) {
      return decodeURIComponent(utf8Match[1])
    }

    const normalMatch = contentDisposition.match(/filename\s*=\s*"?([^";]+)"?/i)
    if (normalMatch && normalMatch[1]) {
      return decodeURIComponent(normalMatch[1])
    }

    return '志愿者个人概览.xlsx'
  }
}
</script>

<style lang="scss" scoped>
.volunteer-overview-page {
  padding: 24px;
}

.overview-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.overview-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.overview-eyebrow {
  color: #909399;
  font-size: 13px;
}

.overview-copy {
  h1 {
    margin: 8px 0 0;
    font-size: 28px;
    color: #303133;
  }

  p {
    margin: 10px 0 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
  }
}

.overview-status {
  min-width: 140px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff9e8;
}

.status-label {
  margin-bottom: 10px;
  color: #909399;
  font-size: 13px;
}

.overview-fallback {
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f4f4f5;
  color: #606266;
  line-height: 1.6;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.overview-stat-card {
  border-radius: 12px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 10px;
  color: #303133;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .volunteer-overview-page {
    padding: 16px;
  }

  .overview-header {
    flex-direction: column;
  }

  .overview-actions {
    width: 100%;
    align-items: stretch;
  }

  .overview-status {
    width: 100%;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
