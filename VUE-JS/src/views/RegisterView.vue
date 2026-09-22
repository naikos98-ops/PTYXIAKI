<template>
  <div class="register-container">
    <div class="register-decorations">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <div class="register-card animate-fade-in">
      <div class="register-header">
        <div class="logo-container">
          <span class="logo-text">Estate<span class="highlight">Sol</span></span>
        </div>
        <h2>Δημιουργία Λογαριασμού</h2>
        <p>Εγγραφείτε για να ξεκινήσετε τη διαχείριση ακινήτων</p>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">

        <div class="form-group">
          <label for="username">Όνομα Χρήστη (Username)</label>
          <div class="input-wrapper">
            <input
              id="username"
              type="text"
              v-model="form.username"
              placeholder="Επιλέξτε ένα username"
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
              v-model="form.password"
              placeholder="Επιλέξτε έναν ισχυρό κωδικό"
              required
              class="form-input"
            />
          </div>
        </div>


        <div class="form-group">
          <label for="confirmPassword">Επιβεβαίωση Κωδικού</label>
          <div class="input-wrapper">
            <input
              id="confirmPassword"
              type="password"
              v-model="form.confirmPassword"
              placeholder="Εισάγετε ξανά τον κωδικό"
              required
              class="form-input"
            />
          </div>
        </div>


        <div class="form-group">
          <label>Τύπος Λογαριασμού</label>
          <div class="role-selector-tabs">
            <button
              type="button"
              class="tab-btn"
              :class="{ active: form.role === 'AMATEUR_USER' }"
              @click="setRole('AMATEUR_USER')"
            >
              <span>Ιδιώτης</span>
            </button>
            <button
              type="button"
              class="tab-btn"
              :class="{ active: form.role === 'CONSTRUCTION_COMPANY' }"
              @click="setRole('CONSTRUCTION_COMPANY')"
            >
              <span>Τεχνική</span>
            </button>
            <button
              type="button"
              class="tab-btn"
              :class="{ active: form.role === 'REAL_ESTATE_AGENCY' }"
              @click="setRole('REAL_ESTATE_AGENCY')"
            >
              <span>Μεσιτικό</span>
            </button>
          </div>
        </div>


        <div v-if="form.role === 'CONSTRUCTION_COMPANY' || form.role === 'REAL_ESTATE_AGENCY'" class="business-fields-group animate-slide-down">
          <div class="business-header">
            <span> Στοιχεία Εταιρείας (AFM/GEMI)</span>
          </div>


          <div class="form-group">
            <label for="afm">ΑΦΜ (9 ψηφία)</label>
            <div class="input-wrapper">
              <input
                id="afm"
                type="text"
                v-model="form.afm"
                placeholder="Εισάγετε το ΑΦΜ της εταιρείας"
                required
                class="form-input"
              />
            </div>
          </div>


          <div class="form-group">
            <label for="gemiNumber">Αριθμός ΓΕΜΗ</label>
            <div class="input-wrapper">
              <input
                id="gemiNumber"
                type="text"
                v-model="form.gemiNumber"
                placeholder="Εισάγετε τον αριθμό ΓΕΜΗ"
                required
                class="form-input"
              />
            </div>
          </div>


          <div class="form-group">
            <label for="region">Περιοχή Δραστηριότητας</label>
            <div class="input-wrapper">
              <input
                id="region"
                type="text"
                v-model="form.region"
                placeholder="π.χ. Αττική, Θεσσαλονίκη"
                required
                class="form-input"
              />
            </div>
          </div>
        </div>

        <div v-if="error" class="error-message animate-slide-down">
          {{ error }}
        </div>

        <button type="submit" class="btn-register" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>Δημιουργία Λογαριασμού </span>
        </button>

        <div class="divider">
          <span>Ή</span>
        </div>

        <router-link to="/login" class="btn-login-link">
          Σύνδεση σε υπάρχοντα λογαριασμό
        </router-link>
      </form>
    </div>
  </div>
</template>

<script>
import { auth } from '@/stores/auth'

export default {
  name: 'RegisterView',
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        role: 'AMATEUR_USER',
        afm: '',
        gemiNumber: '',
        region: ''
      },
      loading: false,
      error: ''
    }
  },
  methods: {
    setRole(role) {
      this.form.role = role
      if (role === 'AMATEUR_USER') {
        this.form.afm = ''
        this.form.gemiNumber = ''
        this.form.region = ''
      }
    },
    async handleRegister() {
      if (this.form.password !== this.form.confirmPassword) {
        this.error = "Οι κωδικοί πρόσβασης δεν ταιριάζουν!"
        return
      }

      if (this.form.role === 'CONSTRUCTION_COMPANY' || this.form.role === 'REAL_ESTATE_AGENCY') {
        const afm = this.form.afm.trim()
        if (afm.length !== 9 || !/^\d+$/.test(afm)) {
          this.error = "Το ΑΦΜ πρέπει να αποτελείται από ακριβώς 9 ψηφία!"
          return
        }
      }

      this.loading = true
      this.error = ''

      try {
        const registerRes = await fetch('/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: this.form.username,
            password: this.form.password,
            role: this.form.role,
            afm: this.form.role !== 'AMATEUR_USER' ? this.form.afm : null,
            gemiNumber: this.form.role !== 'AMATEUR_USER' ? this.form.gemiNumber : null,
            region: this.form.role !== 'AMATEUR_USER' ? this.form.region : null
          })
        })

        if (!registerRes.ok) {
          const text = await registerRes.text().catch(() => '')
          throw new Error(text || 'Η εγγραφή απέτυχε.')
        }

        console.log('Registration successful. logging in...')

        const loginData = new URLSearchParams()
        loginData.append('username', this.form.username)
        loginData.append('password', this.form.password)

        const loginRes = await fetch('/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: loginData,
          redirect: 'manual'
        })

        if (loginRes.type === 'opaqueredirect' || loginRes.ok || loginRes.status === 200 || loginRes.status === 302) {
          await new Promise(r => setTimeout(r, 500))
          const isValid = await auth.checkAuth()

          if (isValid) {
             const target = auth.role === 'ADMIN' ? '/admin/dashboard' : '/dashboard'
             this.$router.push(target)
          } else {
             this.$router.push('/login?registered=true')
          }
        } else {
          this.$router.push('/login?registered=true')
        }

      } catch (e) {
        console.error(e)
        this.error = e.message || 'Προέκυψε σφάλμα κατά την εγγραφή.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top right, #1e3a58 0%, var(--primary-color) 100%);
  position: relative;
  overflow: hidden;
  padding: 30px 20px;
}


.register-decorations {
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
  background: radial-gradient(circle, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0) 70%);
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

.register-card {
  background: rgba(255, 255, 255, 0.98);
  padding: 40px 35px;
  border-radius: var(--radius-xl);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3), var(--shadow-xl);
  width: 100%;
  max-width: 460px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 2;
  position: relative;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 30px;
}

.logo-text {
  font-family: var(--font-title);
  font-size: 26px;
  font-weight: 800;
  color: var(--primary-color);
  letter-spacing: -0.5px;
}

.logo-text .highlight {
  color: var(--success-color);
  text-shadow: 1px 1px 0px rgba(15, 41, 66, 0.1);
}

.register-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.register-header p {
  color: var(--text-muted);
  font-size: 13px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
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
  padding: 11px 16px 11px 42px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 14px;
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

.role-selector-tabs {
  display: flex;
  gap: 6px;
  background: var(--primary-light);
  padding: 4px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.tab-btn {
  flex: 1;
  padding: 8px 2px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-fast);
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.tab-btn:hover {
  color: var(--primary-color);
  background: rgba(255, 255, 255, 0.5);
}

.tab-btn.active {
  background: #ffffff;
  color: var(--success-color);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.tab-icon {
  font-size: 14px;
}

.business-fields-group {
  border: 2px dashed var(--border-color);
  padding: 16px;
  border-radius: var(--radius-lg);
  background: #fdfdfd;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.business-header {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary-color);
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 6px;
  margin-bottom: 4px;
}

.btn-register {
  background: var(--success-color);
  color: #ffffff;
  padding: 13px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-normal);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  margin-top: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.btn-register:hover:not(:disabled) {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.35);
}

.btn-register:disabled {
  background: var(--border-color);
  color: var(--text-muted);
  cursor: not-allowed;
  box-shadow: none;
}

.divider {
  display: flex;
  align-items: center;
  margin: 6px 0;
  color: var(--text-muted);
  font-size: 12px;
  justify-content: center;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--border-color);
}

.divider span {
  padding: 0 10px;
}

.btn-login-link {
  display: block;
  text-align: center;
  color: var(--primary-color);
  text-decoration: none;
  font-size: 13px;
  font-weight: 700;
  transition: var(--transition-fast);
  padding: 8px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: #ffffff;
}

.btn-login-link:hover {
  background: var(--primary-light);
  border-color: var(--primary-color);
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
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
