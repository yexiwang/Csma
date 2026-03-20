import { AddressBook } from '@/api/addressBook'

export interface ResolveSelectedAddressOptions {
  preferredAddressId?: number | string | null
  currentSelectedAddressId?: number | string | null
  defaultAddress?: AddressBook | null
}

const normalizeAddressId = (value?: number | string | null) => {
  if (value === undefined || value === null || value === '') {
    return null
  }

  const normalized = Number(value)
  return Number.isFinite(normalized) && normalized > 0 ? normalized : null
}

export const resolveDefaultAddress = (addressList: AddressBook[]) => {
  const defaultAddress = addressList.find((item) => Number(item.isDefault) === 1)
  return defaultAddress || null
}

export const resolveSelectedAddress = (
  addressList: AddressBook[],
  options: ResolveSelectedAddressOptions = {}
) => {
  if (!Array.isArray(addressList) || addressList.length === 0) {
    return null
  }

  const defaultAddress = options.defaultAddress || resolveDefaultAddress(addressList)
  const candidateIds = [
    normalizeAddressId(options.preferredAddressId),
    normalizeAddressId(options.currentSelectedAddressId),
    defaultAddress ? normalizeAddressId(defaultAddress.id) : null,
    normalizeAddressId(addressList[0] && addressList[0].id)
  ]

  for (const candidateId of candidateIds) {
    if (!candidateId) {
      continue
    }

    const matchedAddress = addressList.find((item) => Number(item.id) === candidateId)
    if (matchedAddress) {
      return matchedAddress
    }
  }

  return null
}

export const formatFullAddress = (address?: Partial<AddressBook> | null) => {
  if (!address) {
    return ''
  }

  return [
    address.provinceName,
    address.cityName,
    address.districtName,
    address.detail
  ].filter(Boolean).join('')
}

export const formatAddressSummary = (address?: Partial<AddressBook> | null) => {
  if (!address) {
    return ''
  }

  const label = address.label ? `【${address.label}】` : ''
  return `${label}${address.consignee || ''} ${address.phone || ''}`.trim()
}
