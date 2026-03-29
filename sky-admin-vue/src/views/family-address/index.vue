<template>
  <div v-loading="loading" class="family-address-page">
    <div class="page-header">
      <div>
        <h1>地址簿</h1>
        <p>选择当前订单使用的配送地址，或维护常用地址。</p>
      </div>
      <div class="page-actions">
        <el-button v-if="checkoutMode" @click="handleBack">
          返回结算
        </el-button>
        <el-button type="primary" @click="goToCreate">
          新增地址
        </el-button>
      </div>
    </div>

    <div v-if="addressList.length > 0" class="address-list">
      <div
        v-for="address in addressList"
        :key="address.id"
        :class="['address-card', { active: isSelected(address) }]"
        @click="handleSelect(address)"
      >
        <div class="address-card-header">
          <div class="address-contact">
            <span class="consignee">{{ address.consignee }}</span>
            <span class="phone">{{ address.phone }}</span>
          </div>
          <div class="address-tags">
            <el-tag v-if="Number(address.isDefault) === 1" size="mini" type="warning">
              默认
            </el-tag>
            <el-tag v-if="address.label" size="mini">
              {{ address.label }}
            </el-tag>
          </div>
        </div>

        <div class="address-detail">
          {{ formatFullAddress(address) }}
        </div>

        <div class="address-card-footer">
          <span class="address-hint">
            {{ checkoutMode ? '点击使用此地址并返回结算' : '常用配送地址' }}
          </span>
          <div class="address-actions">
            <el-button
              type="text"
              size="mini"
              :disabled="Number(address.isDefault) === 1 || defaultingAddressId === address.id"
              @click.stop="handleSetDefault(address)"
            >
              {{ defaultingAddressId === address.id ? '设置中...' : '设为默认' }}
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click.stop="handleEdit(address)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="mini"
              class="danger"
              :disabled="deletingAddressId === address.id"
              @click.stop="handleDelete(address)"
            >
              {{ deletingAddressId === address.id ? '删除中...' : '删除' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-wrapper">
      <el-empty description="当前还没有可用地址">
        <el-button type="primary" @click="goToCreate">
          新增地址
        </el-button>
        <el-button v-if="checkoutMode" @click="handleBack">
          返回结算
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  AddressBook,
  deleteAddressBook,
  getAddressBookList,
  setDefaultAddressBook
} from '@/api/addressBook'
import {
  formatFullAddress,
  resolveSelectedAddress
} from '@/utils/familyAddress'

@Component({
  name: 'FamilyAddressList'
})
export default class FamilyAddressList extends Vue {
  private loading = false
  private deletingAddressId: number | null = null
  private defaultingAddressId: number | null = null
  private addressList: AddressBook[] = []
  private selectedAddressId: number | null = null

  private formatFullAddress = formatFullAddress

  created() {
    this.syncRouteState()
    this.loadAddresses()
  }

  get checkoutMode() {
    return this.$route.query.mode === 'checkout'
  }

  private parseAddressId(value: any) {
    if (value === undefined || value === null || value === '') {
      return null
    }

    const parsed = Number(value)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }

  private syncRouteState() {
    this.selectedAddressId = this.parseAddressId(this.$route.query.selectedAddressId)
  }

  private buildListQuery() {
    const query: any = {}

    if (this.checkoutMode) {
      query.mode = 'checkout'
    }

    if (this.selectedAddressId) {
      query.selectedAddressId = String(this.selectedAddressId)
    }

    return query
  }

  private buildCheckoutQuery() {
    const query: any = {
      resumeCheckout: '1'
    }

    if (this.selectedAddressId) {
      query.selectedAddressId = String(this.selectedAddressId)
    }

    return query
  }

  private async loadAddresses() {
    this.loading = true
    try {
      const res = await getAddressBookList()
      const data = res.data && res.data.data ? res.data.data : res.data
      this.addressList = Array.isArray(data) ? data : []

      const selectedAddress = resolveSelectedAddress(this.addressList, {
        preferredAddressId: this.selectedAddressId
      })
      this.selectedAddressId = selectedAddress ? selectedAddress.id : null
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '地址列表加载失败，请重试'))
    } finally {
      this.loading = false
    }
  }

  private isSelected(address: AddressBook) {
    return Number(address.id) === this.selectedAddressId
  }

  private handleBack() {
    this.$router.push({
      path: '/family-order',
      query: this.buildCheckoutQuery()
    })
  }

  private handleSelect(address: AddressBook) {
    this.selectedAddressId = address.id
    if (this.checkoutMode) {
      this.handleBack()
    }
  }

  private goToCreate() {
    this.$router.push({
      path: '/family-addresses/new',
      query: this.buildListQuery()
    })
  }

  private handleEdit(address: AddressBook) {
    this.selectedAddressId = this.isSelected(address) ? address.id : this.selectedAddressId
    this.$router.push({
      path: `/family-addresses/${address.id}/edit`,
      query: this.buildListQuery()
    })
  }

  private async handleSetDefault(address: AddressBook) {
    if (Number(address.isDefault) === 1 || this.defaultingAddressId === address.id) {
      return
    }

    this.defaultingAddressId = address.id
    try {
      await setDefaultAddressBook(address.id)
      await this.loadAddresses()
      this.$message.success('默认地址已更新')
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '设置默认地址失败，请重试'))
    } finally {
      this.defaultingAddressId = null
    }
  }

  private async handleDelete(address: AddressBook) {
    if (this.deletingAddressId === address.id) {
      return
    }

    try {
      await this.$confirm('确认删除这条地址吗？', '提示', {
        type: 'warning'
      })
    } catch (error) {
      if (error === 'cancel' || error === 'close') {
        return
      }
      throw error
    }

    this.deletingAddressId = address.id
    try {
      await deleteAddressBook(address.id)
      await this.loadAddresses()
      this.$message.success('地址已删除')
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '删除地址失败，请重试'))
    } finally {
      this.deletingAddressId = null
    }
  }

  private resolveErrorMessage(error: any, fallbackMessage: string) {
    const responseMessage = error && error.response && error.response.data && error.response.data.msg
    const directMessage = error && error.message
    return responseMessage || directMessage || fallbackMessage
  }
}
</script>

<style lang="scss" scoped>
.family-address-page {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;

  h1 {
    margin: 0;
    color: #303133;
    font-size: 28px;
  }

  p {
    margin: 8px 0 0;
    color: #909399;
    font-size: 14px;
  }
}

.page-actions {
  display: flex;
  gap: 12px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.address-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  padding: 18px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 24px rgba(31, 35, 41, 0.08);
  }

  &.active {
    border-color: #2f8f83;
    box-shadow: 0 14px 28px rgba(47, 143, 131, 0.18);
  }
}

.address-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.address-contact {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  color: #303133;

  .consignee {
    font-size: 18px;
    font-weight: 600;
  }

  .phone {
    color: #606266;
  }
}

.address-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.address-detail {
  margin-top: 14px;
  color: #606266;
  line-height: 1.7;
  min-height: 48px;
}

.address-card-footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.address-hint {
  color: #909399;
  font-size: 13px;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.danger {
  color: #f56c6c;
}

.empty-wrapper {
  margin-top: 48px;
  background: #fff;
  border-radius: 18px;
  padding: 32px 24px;
}

@media (max-width: 768px) {
  .family-address-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-actions {
    justify-content: flex-end;
  }

  .address-list {
    grid-template-columns: 1fr;
  }

  .address-card-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
