<template>
  <div class="family-address-form-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h1>{{ isEdit ? '编辑地址' : '新增地址' }}</h1>
        <p>省、市、区改为三级联动下拉，数据覆盖全国行政区划，保存后会回到当前结算流程。</p>
      </div>
      <div class="page-actions">
        <el-button @click="handleCancel">
          返回地址列表
        </el-button>
      </div>
    </div>

    <div class="form-card">
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人" prop="consignee">
              <el-input v-model.trim="form.consignee" maxlength="20" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="form.phone" maxlength="11" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.sex" clearable placeholder="选填">
                <el-option label="男" value="1" />
                <el-option label="女" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select
                v-model="form.label"
                clearable
                filterable
                allow-create
                default-first-option
                placeholder="如：家、公司"
              >
                <el-option
                  v-for="label in labelOptions"
                  :key="label"
                  :label="label"
                  :value="label"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="省份" prop="provinceCode">
              <el-select
                v-model="form.provinceCode"
                filterable
                clearable
                style="width: 100%"
                placeholder="请选择省份"
                @change="handleProvinceChange"
              >
                <el-option
                  v-for="province in provinceOptions"
                  :key="province.code"
                  :label="province.name"
                  :value="province.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="cityCode">
              <el-select
                v-model="form.cityCode"
                filterable
                clearable
                style="width: 100%"
                placeholder="请选择城市"
                :disabled="cityOptions.length === 0"
                @change="handleCityChange"
              >
                <el-option
                  v-for="city in cityOptions"
                  :key="city.code"
                  :label="city.name"
                  :value="city.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县" prop="districtCode">
              <el-select
                v-model="form.districtCode"
                filterable
                clearable
                style="width: 100%"
                placeholder="请选择区县"
                :disabled="districtOptions.length === 0"
                @change="handleDistrictChange"
              >
                <el-option
                  v-for="district in districtOptions"
                  :key="district.code"
                  :label="district.name"
                  :value="district.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model.trim="form.detail"
            type="textarea"
            :rows="4"
            maxlength="120"
            show-word-limit
            placeholder="请输入街道、小区、门牌号等详细信息"
          />
        </el-form-item>

        <div v-if="isCurrentDefault" class="default-notice">
          当前地址已是默认地址，保存后仍会保持默认。
        </div>
        <el-checkbox v-else v-model="form.saveAsDefault">
          保存后设为默认地址
        </el-checkbox>
      </el-form>

      <div class="form-footer">
        <el-button @click="handleCancel">
          取消
        </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '保存地址' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Form as ElForm } from 'element-ui'
import { Component, Vue, Watch } from 'vue-property-decorator'
import {
  AddressBook,
  AddressBookPayload,
  createAddressBook,
  getAddressBookById,
  setDefaultAddressBook,
  updateAddressBook
} from '@/api/addressBook'
import {
  ChinaAreaOption,
  getCityOptions,
  getDistrictOptions,
  getProvinceOptions,
  resolveChinaAreaByCodes,
  resolveChinaAreaCodesByNames
} from '@/utils/chinaArea'

@Component({
  name: 'FamilyAddressForm'
})
export default class FamilyAddressForm extends Vue {
  private loading = false
  private submitting = false
  private originalIsDefault = false
  private readonly labelOptions = ['家', '公司', '学校']
  private readonly provinceOptions: ChinaAreaOption[] = getProvinceOptions()
  private cityOptions: ChinaAreaOption[] = []
  private districtOptions: ChinaAreaOption[] = []

  private form: any = this.createEmptyForm()

  private rules = {
    consignee: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      {
        validator: (_rule: any, value: string, callback: Function) => {
          if (!value) {
            callback()
            return
          }
          if (!/^1\d{10}$/.test(value)) {
            callback(new Error('请输入正确的 11 位手机号'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    provinceCode: [{ required: true, message: '请选择省份', trigger: 'change' }],
    cityCode: [{ required: true, message: '请选择城市', trigger: 'change' }],
    districtCode: [{ required: true, message: '请选择区县', trigger: 'change' }],
    detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
  }

  @Watch('$route', { immediate: true })
  private async onRouteChange() {
    this.resetForm()

    if (this.isEdit && this.currentAddressId) {
      await this.loadAddressDetail(this.currentAddressId)
    }
  }

  get isEdit() {
    return Boolean(this.currentAddressId)
  }

  get isCurrentDefault() {
    return this.originalIsDefault
  }

  get checkoutMode() {
    return this.$route.query.mode === 'checkout'
  }

  get currentAddressId() {
    const parsedId = Number(this.$route.params.id)
    return Number.isFinite(parsedId) && parsedId > 0 ? parsedId : null
  }

  private createEmptyForm() {
    return {
      id: undefined as number | undefined,
      consignee: '',
      phone: '',
      sex: '',
      provinceCode: '',
      provinceName: '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
      detail: '',
      label: '',
      saveAsDefault: false
    }
  }

  private parseAddressId(value: any) {
    if (value === undefined || value === null || value === '') {
      return null
    }

    const parsed = Number(value)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }

  private buildListQuery() {
    const query: any = {}
    const selectedAddressId = this.parseAddressId(this.$route.query.selectedAddressId)

    if (this.checkoutMode) {
      query.mode = 'checkout'
    }

    if (selectedAddressId) {
      query.selectedAddressId = String(selectedAddressId)
    }

    return query
  }

  private buildCheckoutQuery(addressId?: number | null) {
    const query: any = {
      resumeCheckout: '1'
    }

    if (addressId) {
      query.selectedAddressId = String(addressId)
    }

    return query
  }

  private resetForm() {
    this.loading = false
    this.submitting = false
    this.originalIsDefault = false
    this.form = this.createEmptyForm()
    this.cityOptions = []
    this.districtOptions = []
    this.$nextTick(() => {
      const formRef = this.$refs.form as ElForm | undefined
      if (formRef) {
        formRef.clearValidate()
      }
    })
  }

  private updateProvinceName() {
    const matchedProvince = this.provinceOptions.find((item) => item.code === this.form.provinceCode)
    this.form.provinceName = matchedProvince ? matchedProvince.name : ''
  }

  private updateCityName() {
    const matchedCity = this.cityOptions.find((item) => item.code === this.form.cityCode)
    this.form.cityName = matchedCity ? matchedCity.name : ''
  }

  private updateDistrictName() {
    const matchedDistrict = this.districtOptions.find((item) => item.code === this.form.districtCode)
    this.form.districtName = matchedDistrict ? matchedDistrict.name : ''
  }

  private handleProvinceChange(value: string) {
    this.form.provinceCode = value || ''
    this.updateProvinceName()
    this.cityOptions = getCityOptions(this.form.provinceCode)
    this.form.cityCode = ''
    this.form.cityName = ''
    this.form.districtCode = ''
    this.form.districtName = ''
    this.districtOptions = []
  }

  private handleCityChange(value: string) {
    this.form.cityCode = value || ''
    this.updateCityName()
    this.districtOptions = getDistrictOptions(this.form.cityCode)
    this.form.districtCode = ''
    this.form.districtName = ''
  }

  private handleDistrictChange(value: string) {
    this.form.districtCode = value || ''
    this.updateDistrictName()
  }

  private applyRegionCodes(codes: string[]) {
    const [provinceCode, cityCode, districtCode] = codes.map((item) => String(item))

    this.form.provinceCode = provinceCode || ''
    this.updateProvinceName()
    this.cityOptions = getCityOptions(this.form.provinceCode)

    this.form.cityCode = cityCode || ''
    this.updateCityName()
    this.districtOptions = getDistrictOptions(this.form.cityCode)

    this.form.districtCode = districtCode || ''
    this.updateDistrictName()
  }

  private async loadAddressDetail(id: number) {
    this.loading = true
    try {
      const res = await getAddressBookById(id)
      const data = res.data && res.data.data ? res.data.data : res.data
      if (!data) {
        throw new Error('未找到当前地址信息')
      }

      const address = data as AddressBook
      const regionCodes = address.provinceCode && address.cityCode && address.districtCode
        ? [String(address.provinceCode), String(address.cityCode), String(address.districtCode)]
        : resolveChinaAreaCodesByNames(address.provinceName, address.cityName, address.districtName)

      this.originalIsDefault = Number(address.isDefault) === 1
      this.form = {
        id: address.id,
        consignee: address.consignee || '',
        phone: address.phone || '',
        sex: address.sex === undefined || address.sex === null ? '' : String(address.sex),
        provinceCode: '',
        provinceName: '',
        cityCode: '',
        cityName: '',
        districtCode: '',
        districtName: '',
        detail: address.detail || '',
        label: address.label || '',
        saveAsDefault: false
      }

      if (regionCodes.length === 3) {
        this.applyRegionCodes(regionCodes as string[])
      } else {
        this.form.provinceCode = address.provinceCode || ''
        this.form.provinceName = address.provinceName || ''
        this.form.cityCode = address.cityCode || ''
        this.form.cityName = address.cityName || ''
        this.form.districtCode = address.districtCode || ''
        this.form.districtName = address.districtName || ''
        this.cityOptions = getCityOptions(this.form.provinceCode)
        this.districtOptions = getDistrictOptions(this.form.cityCode)
      }
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '地址详情加载失败，请重试'))
      this.handleCancel()
    } finally {
      this.loading = false
    }
  }

  private handleCancel() {
    this.$router.push({
      path: '/family-addresses',
      query: this.buildListQuery()
    })
  }

  private buildPayload(): AddressBookPayload {
    const label = this.form.label ? String(this.form.label).trim() : ''
    const sex = this.form.sex === '' ? '' : String(this.form.sex)
    const resolvedRegion = resolveChinaAreaByCodes([
      this.form.provinceCode,
      this.form.cityCode,
      this.form.districtCode
    ])

    if (!resolvedRegion) {
      throw new Error('请选择完整的省市区')
    }

    return {
      id: this.form.id,
      consignee: this.form.consignee.trim(),
      phone: this.form.phone.trim(),
      sex,
      provinceCode: resolvedRegion.provinceCode,
      provinceName: resolvedRegion.provinceName,
      cityCode: resolvedRegion.cityCode,
      cityName: resolvedRegion.cityName,
      districtCode: resolvedRegion.districtCode,
      districtName: resolvedRegion.districtName,
      detail: this.form.detail.trim(),
      label
    }
  }

  private handleSubmit() {
    ;(this.$refs.form as ElForm).validate(async (valid: boolean) => {
      if (!valid || this.submitting) {
        return false
      }

      this.submitting = true
      try {
        const payload = this.buildPayload()
        let savedAddressId = this.currentAddressId

        if (this.isEdit) {
          await updateAddressBook(payload)
        } else {
          const res = await createAddressBook(payload)
          const data = res.data && res.data.data ? res.data.data : res.data
          savedAddressId = data && data.id ? Number(data.id) : null
          if (!savedAddressId) {
            throw new Error('新增地址成功，但未获取到地址编号')
          }
        }

        if (!savedAddressId) {
          throw new Error('地址保存成功，但未获取到地址编号')
        }

        if (!this.originalIsDefault && this.form.saveAsDefault) {
          await setDefaultAddressBook(savedAddressId)
        }

        this.$message.success(this.isEdit ? '地址已更新' : '地址已新增')
        if (this.checkoutMode) {
          this.$router.push({
            path: '/family-order',
            query: this.buildCheckoutQuery(savedAddressId)
          })
        } else {
          this.$router.push({
            path: '/family-addresses',
            query: {
              selectedAddressId: String(savedAddressId)
            }
          })
        }
      } catch (error) {
        this.$message.error(this.resolveErrorMessage(error, '地址保存失败，请重试'))
      } finally {
        this.submitting = false
      }
    })
  }

  private resolveErrorMessage(error: any, fallbackMessage: string) {
    const responseMessage = error && error.response && error.response.data && error.response.data.msg
    const directMessage = error && error.message
    return responseMessage || directMessage || fallbackMessage
  }
}
</script>

<style lang="scss" scoped>
.family-address-form-page {
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

.form-card {
  background: #fff;
  border-radius: 18px;
  padding: 28px;
  box-shadow: 0 12px 28px rgba(31, 35, 41, 0.06);
}

.default-notice {
  margin-top: 8px;
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff7e6;
  color: #8c6d1f;
}

.form-footer {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .family-address-form-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .form-card {
    padding: 20px 16px;
  }

  .form-footer {
    flex-direction: column-reverse;
  }

  .form-footer .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
