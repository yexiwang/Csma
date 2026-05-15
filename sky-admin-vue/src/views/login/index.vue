<template>
  <div class="login">
    <div class="login-box">
      <div class="login-form">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <h3 class="title-label">
              社区老年助餐服务系统
            </h3>
          </div>

          <div style="margin-bottom: 20px; text-align: center;">
            <el-radio-group v-model="loginForm.loginType" size="small">
              <el-radio-button label="employee">
                员工/管理员
              </el-radio-button>
              <el-radio-button label="user">
                家属/志愿者
              </el-radio-button>
            </el-radio-group>
          </div>

          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
              prefix-icon="iconfont icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="iconfont icon-lock"
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button
              :loading="loading"
              class="login-btn"
              size="medium"
              type="primary"
              style="width: 100%"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { UserModule } from '@/store/modules/user'

@Component({
  name: 'Login',
})
export default class extends Vue {
  private validateUsername = (rule: any, value: string, callback: Function) => {
    if (!value) {
      callback(new Error('请输入用户名'))
    } else {
      callback()
    }
  }
  private validatePassword = (rule: any, value: string, callback: Function) => {
    if (value.length < 3) {
      callback(new Error('密码必须在3位以上'))
    } else {
      callback()
    }
  }
  private loginForm = {
    username: 'admin',
    password: '123456',
    loginType: 'employee'
  }

  loginRules = {
    username: [{ validator: this.validateUsername, trigger: 'blur' }],
    password: [{ validator: this.validatePassword, trigger: 'blur' }],
  }
  private loading = false
  private redirect?: string

  @Watch('$route', { immediate: true })
  private onRouteChange(route: any) {}

  // 登录
  private handleLogin() {
    (this.$refs.loginForm as ElForm).validate(async (valid: boolean) => {
      if (valid) {
        this.loading = true
        try {
          await UserModule.Login(this.loginForm)
          this.$router.push('/')
        } catch (err) {
          this.loading = false
        }
      } else {
        return false
      }
    })
  }
}
</script>

<style lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100%;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(47, 143, 131, 0.18), transparent 32%),
    radial-gradient(circle at bottom right, rgba(62, 141, 133, 0.16), transparent 30%),
    linear-gradient(135deg, #eff8f4 0%, #d7ebe4 52%, #eef5f2 100%);
}

.login-box {
  max-width: 440px;
  width: 100%;
  min-height: 560px;
  border-radius: 28px;
  box-shadow: 0 30px 80px rgba(22, 65, 56, 0.16);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.55);
  animation: loginShellIn 0.78s cubic-bezier(0.22, 1, 0.36, 1);
}

.login-form {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  .el-form {
    width: min(320px, 100%);
    padding: 0 10px;
    opacity: 0;
    transform: translateY(22px);
    animation: loginFadeUp 0.72s ease-out 0.24s forwards;
  }
  .el-form-item {
    margin-bottom: 28px;
  }
  .el-form-item.is-error .el-input__inner {
    border: 0 !important;
    border-bottom: 1px solid #fd7065 !important;
    background: #fff !important;
  }
  .input-icon {
    height: 32px;
    width: 18px;
    margin-left: -2px;
  }
  .el-input__inner {
    border: 0;
    border-bottom: 1px solid #cfe0da;
    border-radius: 0;
    font-size: 14px;
    font-weight: 400;
    color: #223430;
    height: 42px;
    line-height: 42px;
    background: transparent;
    transition: border-color 0.2s ease;
  }
  .el-input__inner:focus {
    border-bottom-color: #2f8f83;
  }
  .el-input__prefix {
    left: 0;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 30px;
  }
  .el-input__inner::placeholder {
    color: #9eafab;
  }
  .el-form-item--medium .el-form-item__content {
    line-height: 42px;
  }
  .el-input--medium .el-input__icon {
    line-height: 42px;
    font-size: 14px;
    color: #7f958f;
  }
  .el-radio-group {
    display: flex;
    width: 100%;
    border-radius: 16px;
    overflow: hidden;
    background: #eef7f3;
    padding: 4px;
  }
  .el-radio-button {
    flex: 1;
  }
  .el-radio-button__inner {
    width: 100%;
    border: 0 !important;
    background: transparent;
    color: #57716a;
    font-size: 13px;
    font-weight: 500;
    padding: 10px 12px;
    box-shadow: none !important;
  }
  .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    color: #ffffff;
    background: linear-gradient(135deg, #2f8f83 0%, #3fa392 100%);
    box-shadow: 0 8px 18px rgba(47, 143, 131, 0.22) !important;
  }
  .el-radio-button:first-child .el-radio-button__inner,
  .el-radio-button:last-child .el-radio-button__inner {
    border-radius: 12px;
  }
}

.login-btn {
  border-radius: 16px;
  padding: 13px 20px !important;
  margin-top: 8px;
  font-weight: 500;
  font-size: 14px;
  border: 0;
  color: #ffffff;
  background: linear-gradient(135deg, #2f8f83 0%, #3fa392 100%);
  box-shadow: 0 10px 24px rgba(47, 143, 131, 0.24);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  &:hover,
  &:focus {
    transform: translateY(-1px);
    background: linear-gradient(135deg, #2b8176 0%, #378f81 100%);
    color: #ffffff;
    box-shadow: 0 14px 28px rgba(47, 143, 131, 0.28);
  }
}
.login-form-title {
  min-height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 34px;
  .title-label {
    font-weight: 600;
    font-size: 26px;
    color: #223430;
    margin-left: 0;
    letter-spacing: 0.02em;
  }
}

@keyframes loginShellIn {
  0% {
    opacity: 0;
    transform: translateY(28px) scale(0.985);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes loginFadeUp {
  0% {
    opacity: 0;
    transform: translateY(18px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-box,
  .login-form .el-form {
    animation: none !important;
    opacity: 1 !important;
    transform: none !important;
  }

  .login-btn {
    transition: none !important;
  }
}

@media (max-width: 480px) {
  .login {
    padding: 14px;
  }

  .login-box {
    border-radius: 22px;
    min-height: auto;
  }

  .login-form {
    padding: 36px 22px 28px;
  }

  .login-form-title {
    margin-bottom: 26px;
    .title-label {
      font-size: 22px;
    }
  }
}
</style>
