type AreaMap = Record<string, Record<string, string>>

export interface ChinaAreaOption {
  code: string
  name: string
}

export interface ResolvedChinaArea {
  provinceCode: string
  provinceName: string
  cityCode: string
  cityName: string
  districtCode: string
  districtName: string
}

const areaData = require('area-data/pcaa') as AreaMap

const buildOptions = (parentCode: string): ChinaAreaOption[] => {
  const childrenMap = areaData[parentCode] || {}

  return Object.keys(childrenMap).map((code) => ({
    code,
    name: childrenMap[code]
  }))
}

const findCodeByName = (parentCode: string, areaName?: string) => {
  if (!areaName) {
    return null
  }

  const normalizedName = areaName.trim()
  const childrenMap = areaData[parentCode] || {}
  const matchedCode = Object.keys(childrenMap).find((code) => childrenMap[code] === normalizedName)
  return matchedCode || null
}

export const getProvinceOptions = () => buildOptions('86')

export const getCityOptions = (provinceCode?: string) => {
  if (!provinceCode) {
    return []
  }
  return buildOptions(String(provinceCode))
}

export const getDistrictOptions = (cityCode?: string) => {
  if (!cityCode) {
    return []
  }
  return buildOptions(String(cityCode))
}

export const resolveChinaAreaByCodes = (codes: Array<string | number>) => {
  if (!Array.isArray(codes) || codes.length !== 3) {
    return null
  }

  const [provinceCodeRaw, cityCodeRaw, districtCodeRaw] = codes
  const provinceCode = String(provinceCodeRaw)
  const cityCode = String(cityCodeRaw)
  const districtCode = String(districtCodeRaw)
  const provinceName = areaData['86'] && areaData['86'][provinceCode]
  const cityName = areaData[provinceCode] && areaData[provinceCode][cityCode]
  const districtName = areaData[cityCode] && areaData[cityCode][districtCode]

  if (!provinceName || !cityName || !districtName) {
    return null
  }

  return {
    provinceCode,
    provinceName,
    cityCode,
    cityName,
    districtCode,
    districtName
  }
}

export const resolveChinaAreaCodesByNames = (
  provinceName?: string,
  cityName?: string,
  districtName?: string
) => {
  const provinceCode = findCodeByName('86', provinceName)
  if (!provinceCode) {
    return []
  }

  const cityCode = findCodeByName(provinceCode, cityName)
  if (!cityCode) {
    return [provinceCode]
  }

  const districtCode = findCodeByName(cityCode, districtName)
  if (!districtCode) {
    return [provinceCode, cityCode]
  }

  return [provinceCode, cityCode, districtCode]
}
