import { reactive } from 'vue'

export const auth = reactive({
  user: null,
  isAuthenticated: false,

  role: null,

  async checkAuth() {
    try {
      const res = await fetch('/api/me')

      if (!res.ok) {
        throw new Error('Not authenticated')
      }

      const userData = await res.json()

      this.isAuthenticated = true
      this.user = userData
      const roleStr = userData.role || '';
      if (roleStr === 'ROLE_ADMIN') {
        this.role = 'ADMIN'
      } else if (roleStr === 'ROLE_CONSTRUCTION_COMPANY') {
        this.role = 'CONSTRUCTION_COMPANY'
      } else if (roleStr === 'ROLE_REAL_ESTATE_AGENCY') {
        this.role = 'REAL_ESTATE_AGENCY'
      } else {
        this.role = 'AMATEUR_USER'
      }

      return true
    } catch {
      this.isAuthenticated = false
      this.user = null
      this.role = null
      return false
    }
  }
})
