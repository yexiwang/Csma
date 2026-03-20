import { VuexModule, Module, Action, Mutation, getModule } from 'vuex-module-decorators'
import { login, userLogin, userLogout } from '@/api/employee'
import {
  getRole,
  getStoreId,
  getToken,
  getUserInfo,
  removeRole,
  removeStoreId,
  removeToken,
  removeUserInfo,
  setRole,
  setStoreId,
  setToken,
  setUserInfo
} from '@/utils/cookies'
import store from '@/store'
import Cookies from 'js-cookie'
import { Message } from 'element-ui'
import { jwtDecode } from 'jwt-decode'

function parseUserInfo() {
  const raw = getUserInfo()
  if (!raw) {
    return {}
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    console.error('Failed to parse user_info cookie', error)
    return {}
  }
}

export interface IUserState {
  token: string
  name: string
  avatar: string
  storeId: string
  introduction: string
  userInfo: any
  roles: string[]
  username: string
}

@Module({ dynamic: true, store, name: 'user' })
class User extends VuexModule implements IUserState {
  public token = getToken() || ''
  public userInfo = parseUserInfo()
  public name = (this.userInfo as any).name || ''
  public avatar = (this.userInfo as any).avatar || ''
  public storeId: string = getStoreId() || ''
  public introduction = (this.userInfo as any).introduction || ''
  public roles: string[] = getRole() ? [getRole() as string] : []
  public username = Cookies.get('username') || ((this.userInfo as any).userName || (this.userInfo as any).username || '')

  @Mutation
  private SET_TOKEN(token: string) {
    this.token = token
  }

  @Mutation
  private SET_NAME(name: string) {
    this.name = name
  }

  @Mutation
  private SET_USERINFO(userInfo: any) {
    this.userInfo = { ...userInfo }
  }

  @Mutation
  private SET_AVATAR(avatar: string) {
    this.avatar = avatar
  }

  @Mutation
  private SET_INTRODUCTION(introduction: string) {
    this.introduction = introduction
  }

  @Mutation
  private SET_ROLES(roles: string[]) {
    this.roles = roles
  }

  @Mutation
  private SET_STOREID(storeId: string) {
    this.storeId = storeId
  }

  @Mutation
  private SET_USERNAME(name: string) {
    this.username = name
  }

  @Action
  public async Login(userInfo: { username: string; password: string; loginType: string }) {
    let { username, password, loginType } = userInfo
    username = username.trim()
    this.SET_USERNAME(username)
    Cookies.set('username', username)

    const res = loginType === 'user'
      ? await userLogin({ username, password })
      : await login({ username, password })

    const { data } = res
    if (String(data.code) !== '1') {
      Message.error(data.msg)
      throw new Error(data.msg || 'Login failed')
    }

    const loginData = data.data || {}
    this.SET_TOKEN(loginData.token)
    setToken(loginData.token)

    let decoded: any = {}
    try {
      decoded = jwtDecode(loginData.token)
    } catch (error) {
      console.error('Token decode failed', error)
    }

    const role = loginData.role || decoded.role || (loginType === 'user' ? 'FAMILY' : 'ADMIN')
    const normalizedUserInfo = {
      ...loginData,
      role,
      userName: loginData.userName || loginData.username || username,
      username: loginData.username || loginData.userName || username
    }

    this.SET_ROLES([role])
    setRole(role)

    this.SET_USERINFO(normalizedUserInfo)
    setUserInfo(normalizedUserInfo)

    this.SET_NAME(normalizedUserInfo.name || normalizedUserInfo.userName || username)
    this.SET_AVATAR(normalizedUserInfo.avatar || '')
    this.SET_INTRODUCTION(normalizedUserInfo.introduction || '')

    if (normalizedUserInfo.storeId) {
      this.SET_STOREID(normalizedUserInfo.storeId)
      setStoreId(normalizedUserInfo.storeId)
    }

    return data
  }

  @Action
  public ResetToken() {
    removeToken()
    removeRole()
    removeUserInfo()
    this.SET_TOKEN('')
    this.SET_ROLES([])
    this.SET_USERINFO({})
  }

  @Action
  public async changeStore(data: any) {
    this.SET_STOREID(data.data)
    this.SET_TOKEN(data.authorization)
    setStoreId(data.data)
    setToken(data.authorization)
  }

  @Action
  public async GetUserInfo() {
    if (this.token === '') {
      throw Error('GetUserInfo: token is undefined!')
    }

    const data = parseUserInfo()
    if (!data || Object.keys(data).length === 0) {
      throw Error('Verification failed, please Login again.')
    }

    const role = data.role || getRole()
    const roles = role ? [role] : []
    const { name, avatar, introduction, applicant, storeManagerName, storeId = '' } = data

    this.SET_ROLES(roles)
    this.SET_USERINFO(data)
    this.SET_NAME(name || applicant || storeManagerName || data.userName || data.username || '')
    this.SET_AVATAR(avatar || '')
    this.SET_INTRODUCTION(introduction || '')
    this.SET_STOREID(storeId)
    if (role) {
      setRole(role)
    }
  }

  @Action
  public async LogOut() {
    try {
      await userLogout({})
    } catch (error) {
      console.log(error)
    }

    removeToken()
    removeRole()
    removeUserInfo()
    removeStoreId()
    this.SET_TOKEN('')
    this.SET_ROLES([])
    this.SET_USERINFO({})
    this.SET_NAME('')
    this.SET_AVATAR('')
    this.SET_INTRODUCTION('')
    this.SET_STOREID('')
    Cookies.remove('username')
    Cookies.remove('user_info')
    Cookies.remove('role')
  }
}

export const UserModule = getModule(User)
