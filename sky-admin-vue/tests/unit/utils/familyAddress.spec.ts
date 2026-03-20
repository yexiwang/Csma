import {
  formatAddressSummary,
  formatFullAddress,
  resolveDefaultAddress,
  resolveSelectedAddress
} from '@/utils/familyAddress'
import { AddressBook } from '@/api/addressBook'

describe('Utils:familyAddress', () => {
  const addressList: AddressBook[] = [
    {
      id: 1,
      consignee: '张三',
      phone: '13800000001',
      provinceName: '浙江省',
      cityName: '杭州市',
      districtName: '西湖区',
      detail: '文三路 1 号',
      label: '家',
      isDefault: 0
    },
    {
      id: 2,
      consignee: '李四',
      phone: '13800000002',
      provinceName: '浙江省',
      cityName: '杭州市',
      districtName: '拱墅区',
      detail: '湖州街 8 号',
      label: '公司',
      isDefault: 1
    }
  ]

  it('resolves the default address from address list', () => {
    expect(resolveDefaultAddress(addressList)).toEqual(addressList[1])
    expect(resolveDefaultAddress([])).toBeNull()
  })

  it('resolves selected address by preferred id first', () => {
    expect(resolveSelectedAddress(addressList, {
      preferredAddressId: 1,
      currentSelectedAddressId: 2
    })).toEqual(addressList[0])
  })

  it('falls back to current selected id, then default, then first item', () => {
    expect(resolveSelectedAddress(addressList, {
      currentSelectedAddressId: 2
    })).toEqual(addressList[1])

    expect(resolveSelectedAddress(addressList, {
      preferredAddressId: 999
    })).toEqual(addressList[1])

    expect(resolveSelectedAddress([addressList[0]], {
      preferredAddressId: 999
    })).toEqual(addressList[0])
  })

  it('formats address text consistently', () => {
    expect(formatFullAddress(addressList[0])).toBe('浙江省杭州市西湖区文三路 1 号')
    expect(formatAddressSummary(addressList[0])).toBe('【家】张三 13800000001')
    expect(formatFullAddress(null)).toBe('')
    expect(formatAddressSummary(null)).toBe('')
  })
})
