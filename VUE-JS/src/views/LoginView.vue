<template>
  <div class="login-container">
    <div class="login-decorations">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <div class="login-card animate-fade-in">
      <div class="login-header">
        <div class="logo-container">
          <span class="logo-text">Estate<span class="highlight">Sol</span></span>
        </div>
        <h2>Καλώς Ήρθατε</h2>
        <p>Συνδεθείτε στην πύλη διαχείρισης ακινήτων σας</p>
      </div>


      <div class="role-tabs">
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeRole === 'AMATEUR_USER' }"
          @click="activeRole = 'AMATEUR_USER'"
        >
          <span>Ιδιώτης</span>
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeRole === 'CONSTRUCTION_COMPANY' }"
          @click="activeRole = 'CONSTRUCTION_COMPANY'"
        >
          <span>Τεχνική</span>
        </button>
        <button
          type="button"
          class="tab-btn"
          :class="{ active: activeRole === 'REAL_ESTATE_AGENCY' }"
          @click="activeRole = 'REAL_ESTATE_AGENCY'"
        >
          <span>Μεσιτικό</span>
        </button>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Όνομα Χρήστη (Username)</label>
          <div class="input-wrapper">
            <input
              id="username"
              type="text"
              v-model="username"
              placeholder="Εισάγετε το username σας"
              required
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="password">Κωδικός Πρόσβασης (Password)</label>
          <div class="input-wrapper">
            <input
              id="password"
              type="password"
              v-model="password"
              placeholder="••••••••"
              required
              class="form-input"
            />
          </div>
        </div>

        <div v-if="error" class="error-message animate-slide-down">
          {{ error }}
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Σύνδεση </span>
        </button>

        <div class="register-prompt">
          Δεν έχετε λογαριασμό;
          <router-link to="/register" class="link-register">Εγγραφείτε εδώ</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { auth } from '@/stores/auth'

export default {
  name: 'LoginView',
  data() {
    return {
      username: '',
      password: '',
      activeRole: 'AMATEUR_USER',
      loading: false,
      error: ''
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = ''

      const formData = new URLSearchParams()
      formData.append('username', this.username)
      formData.append('password', this.password)

      try {
        const res = await fetch('/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: formData,
          redirect: 'manual'
        })

        if (res.type === 'opaqueredirect' || res.ok || res.status === 200 || res.status === 302) {
           const isValid = await auth.checkAuth()
           if (isValid) {
             if (auth.role === 'ADMIN') {
               this.$router.push('/admin/dashboard')
             } else {
               this.$router.push('/dashboard')
             }
           } else {
             this.error = 'Μη έγκυρα στοιχεία σύνδεσης.'
           }
        } else {
          this.error = 'Σφάλμα σύνδεσης. Ελέγξτε το όνομα χρήστη και τον κωδικό σας.'
        }
      } catch (e) {
        console.error(e)
        this.error = 'Προέκυψε σφάλμα επικοινωνίας με τον διακομιστή.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top right, #1e3a58 0%, var(--primary-color) 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}


.login-decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 209, 0, 0.08) 0%, rgba(255, 209, 0, 0) 70%);
}

.circle-1 {
  width: 500px;
  height: 500px;
  top: -150px;
  right: -150px;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
}

.login-card {
  background: rgba(255, 255, 255, 0.98);
  padding: 45px 40px;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3), var(--shadow-xl);
  width: 100%;
  max-width: 440px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 2;
  position: relative;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-family: var(--font-title);
  font-size: 28px;
  font-weight: 800;
  color: var(--primary-color);
  letter-spacing: -0.5px;
}

.logo-text .highlight {
  color: var(--accent-color);
  text-shadow: 1px 1px 0px rgba(15, 41, 66, 0.1);
}

.login-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 6px;
}

.login-header p {
  color: var(--text-muted);
  font-size: 14px;
}

.role-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 28px;
  background: var(--primary-light);
  padding: 4px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.tab-btn {
  flex: 1;
  padding: 10px 4px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-fast);
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.tab-btn:hover {
  color: var(--primary-color);
  background: rgba(255, 255, 255, 0.5);
}

.tab-btn.active {
  background: #ffffff;
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.tab-icon {
  font-size: 16px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary-color);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: var(--text-muted);
  font-size: 16px;
  z-index: 1;
}

.form-input {
  width: 100%;
  padding: 12px 16px 12px 42px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 15px;
  font-family: var(--font-body);
  transition: var(--transition-normal);
  background: #f8fafc;
  color: var(--text-main);
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(15, 41, 66, 0.08);
}

.btn-login {
  background: var(--primary-color);
  color: #ffffff;
  padding: 14px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-normal);
  box-shadow: 0 4px 12px rgba(15, 41, 66, 0.2);
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.btn-login:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(15, 41, 66, 0.35);
}

.btn-login:disabled {
  background: var(--border-color);
  color: var(--text-muted);
  cursor: not-allowed;
  box-shadow: none;
}

.register-prompt {
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 8px;
}

.link-register {
  color: var(--primary-color);
  font-weight: 700;
  text-decoration: none;
  transition: var(--transition-fast);
  border-bottom: 2px solid transparent;
  padding-bottom: 2px;
}

.link-register:hover {
  color: var(--primary-hover);
  border-color: var(--accent-color);
}

.error-message {
  color: var(--danger-color);
  font-size: 13px;
  font-weight: 600;
  background: var(--danger-light);
  padding: 12px;
  border-radius: var(--radius-md);
  text-align: center;
  border: 1px solid #fecaca;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
