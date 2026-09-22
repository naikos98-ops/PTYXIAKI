<template>
  <div class="user-management-container animate-fade-in">
    <div class="user-management-header">
      <div class="title-section">
        <h2> Διαχείριση Χρηστών</h2>
        <p class="subtitle">Εποπτεία εγγραφών, εγκρίσεων AFM/GEMI και διαχείριση λογαριασμών</p>
      </div>
      <div class="search-section">
        <div class="search-wrapper">
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Αναζήτηση χρήστη..."
            class="form-input-premium search-input"
          />
        </div>
      </div>
    </div>

    <div class="user-management-content">

      <div class="section">
        <div class="section-header-row">
          <h3>Εγγεγραμμένοι Χρήστες</h3>


          <div class="role-filter-group">
            <span class="filter-label">Φίλτρο Ρόλου:</span>
            <div class="pill-group">
              <label class="pill-btn" :class="{ active: roleFilter === 'ALL' }">
                <input type="radio" v-model="roleFilter" value="ALL" class="pill-radio" /> Όλοι
              </label>
              <label class="pill-btn" :class="{ active: roleFilter === 'AMATEUR_USER' }">
                <input type="radio" v-model="roleFilter" value="AMATEUR_USER" class="pill-radio" /> Ιδιώτες
              </label>
              <label class="pill-btn" :class="{ active: roleFilter === 'CONSTRUCTION_COMPANY' }">
                <input type="radio" v-model="roleFilter" value="CONSTRUCTION_COMPANY" class="pill-radio" /> Κατασκευαστικές
              </label>
              <label class="pill-btn" :class="{ active: roleFilter === 'REAL_ESTATE_AGENCY' }">
                <input type="radio" v-model="roleFilter" value="REAL_ESTATE_AGENCY" class="pill-radio" /> Μεσιτικά
              </label>
            </div>
          </div>
        </div>

        <div class="user-list">
          <div v-if="loading" class="loading-state">
            <div class="spinner-large"></div>
            <span>Φόρτωση χρηστών...</span>
          </div>

          <div v-else-if="filteredUsers.length === 0" class="empty-state">
            <p>Δεν βρέθηκαν χρήστες με τα συγκεκριμένα κριτήρια.</p>
          </div>

          <div v-else v-for="user in filteredUsers" :key="user.id" class="user-card card-premium">
            <div class="user-card-header">
              <span class="user-role-badge" :class="user.role">
                {{ getRoleLabel(user.role) }}
              </span>
              <div class="user-card-actions">
                <button @click="updateUsername(user)" class="btn-action-icon btn-save" title="Αποθήκευση Username">

                </button>
                <button v-if="user.role === 'CONSTRUCTION_COMPANY' || user.role === 'REAL_ESTATE_AGENCY'" @click="saveDocuments(user)" class="btn-action-icon btn-docs" title="Αποθήκευση Εγγράφων">

                </button>
                <router-link v-if="user.role === 'CONSTRUCTION_COMPANY' || user.role === 'REAL_ESTATE_AGENCY'" :to="'/profile/' + user.id" class="btn-action-icon btn-profile-view" title="Προβολή Προφίλ" style="display: flex; align-items: center; justify-content: center; text-decoration: none;">

                </router-link>
                <button v-if="user.role !== 'CONSTRUCTION_COMPANY' && user.role !== 'REAL_ESTATE_AGENCY' && user.role !== 'ADMIN'" @click="viewUserApartments(user)" class="btn-action-icon btn-view" title="Προβολή Ακινήτων">

                </button>
                <button @click="deleteUser(user.id)" class="btn-action-icon btn-delete" title="Διαγραφή Χρήστη">

                </button>
              </div>
            </div>

            <div class="user-card-info">
              <div class="input-wrapper">
                <label class="input-label">Username</label>
                <input
                  type="text"
                  v-model="user.username"
                  class="form-input-premium input-sm"
                />
              </div>


              <div v-if="user.role === 'CONSTRUCTION_COMPANY' || user.role === 'REAL_ESTATE_AGENCY'" class="company-fields">
                <div class="fields-grid-2">
                  <div class="input-wrapper">
                    <label class="input-label">ΑΦΜ (9 Ψηφία)</label>
                    <input type="text" v-model="user.afm" class="form-input-premium input-sm" />
                  </div>
                  <div class="input-wrapper">
                    <label class="input-label">ΓΕΜΗ</label>
                    <input type="text" v-model="user.gemiNumber" class="form-input-premium input-sm" />
                  </div>
                </div>
                <div class="input-wrapper">
                  <label class="input-label">Περιφέρεια / Δήμος Δραστηριοποίησης</label>
                  <input type="text" v-model="user.region" class="form-input-premium input-sm" />
                </div>

                <div class="verification-toggle">
                  <label class="checkbox-container-premium">
                    <input type="checkbox" v-model="user.verified" @change="toggleVerification(user)">
                    <span class="checkmark-premium"></span>
                    <span class="checkbox-text">Έγκριση AFM/GEMI (Πιστοποιημένος Επαγγελματίας)</span>
                  </label>
                </div>
              </div>
            </div>

            <div v-if="user.status" class="status-alert" :class="user.statusType">
              {{ user.status }}
            </div>
          </div>
        </div>
      </div>

      <hr class="section-divider">


      <div class="section">
        <div class="create-user-card card-premium">
          <div class="create-header">
            <h3> Προσθήκη Νέου Χρήστη</h3>
            <p>Δημιουργήστε έναν νέο λογαριασμό χρήστη ή διαχειριστή απευθείας στο σύστημα.</p>
          </div>

          <div class="create-user-form">
            <div class="form-grid">
              <div class="input-wrapper">
                <label class="input-label">Username</label>
                <input
                  type="text"
                  v-model="newUser.username"
                  placeholder="Εισάγετε όνομα χρήστη..."
                  class="form-input-premium"
                >
              </div>

              <div class="input-wrapper">
                <label class="input-label">Κωδικός Πρόσβασης</label>
                <input
                  type="password"
                  v-model="newUser.password"
                  placeholder="Εισάγετε κωδικό πρόσβασης..."
                  class="form-input-premium"
                >
              </div>

              <div class="input-wrapper full-width-tablet">
                <label class="input-label">Ρόλος Χρήστη</label>
                <select v-model="newUser.role" class="form-input-premium select-premium">
                  <option value="AMATEUR_USER">Amateur User (Απλός Χρήστης)</option>
                  <option value="ADMIN">Administrator (Διαχειριστής)</option>
                </select>
              </div>
            </div>

            <div class="form-footer">
              <button @click="createUser" class="btn-premium btn-premium-primary">
                <span>Δημιουργία Χρήστη</span>
              </button>
            </div>

            <div v-if="createStatus" class="status-msg-box" :class="createStatusType">
              {{ createStatus }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>


  <div v-if="showApartmentsModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content animate-slide-down">
      <div class="modal-header">
        <div>
          <h3> Διαμερίσματα Χρήστη</h3>
          <p class="modal-subtitle">Ιδιοκτήτης: <strong>{{ selectedUserForModal?.username }}</strong></p>
        </div>
        <button @click="closeModal" class="btn-close">×</button>
      </div>

      <div class="modal-body">
        <div v-if="userApartments.length === 0" class="empty-state-modal">
          <p>Δεν βρέθηκαν καταχωρημένα διαμερίσματα για αυτόν τον χρήστη.</p>
        </div>
        <ApartmentList
          v-else
          :apartments="userApartments"
          :loading="false"
          @select="selectApartmentFromModal"
        />
      </div>
    </div>
  </div>
</template>

<script>
import ApartmentList from '@/components/ApartmentList.vue'

export default {
  name: 'UserManagement',
  components: { ApartmentList },
  props: ['allApartments'],
  data() {
    return {
      users: [],
      loading: false,
      searchQuery: '',
      showApartmentsModal: false,
      selectedUserForModal: null,
      userApartments: [],

      newUser: {
        username: '',
        password: '',
        role: 'AMATEUR_USER'
      },
      createStatus: '',
      createStatusType: '',
      roleFilter: 'ALL'
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    getRoleLabel(role) {
      if (role === 'AMATEUR_USER') return ' ΙΔΙΩΤΗΣ';
      if (role === 'CONSTRUCTION_COMPANY') return ' ΚΑΤΑΣΚΕΥΑΣΤΙΚΗ';
      if (role === 'REAL_ESTATE_AGENCY') return ' ΜΕΣΙΤΙΚΟ';
      if (role === 'ADMIN') return ' ΔΙΑΧΕΙΡΙΣΤΗΣ';
      return role;
    },
    loadUsers() {
      this.loading = true
      fetch('/api/admin/users')
        .then(res => {
          if (!res.ok) throw new Error('Failed to load users')
          return res.json()
        })
        .then(data => {
          this.users = data.map(u => ({ ...u, status: '', statusType: '' }))
          this.loading = false
        })
        .catch(err => {
          console.error(err)
          this.loading = false
        })
    },

    updateUsername(user) {
      if (!user.username) return

      const params = new URLSearchParams()
      params.append('username', user.username)

      fetch(`/api/admin/users/${user.id}/username`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(async res => {
        if (res.ok) {
          user.status = ' Το Username ενημερώθηκε επιτυχώς!'
          user.statusType = 'success'
          setTimeout(() => { user.status = '' }, 3000)
        } else if (res.status === 400) {
          user.status = ' Μη έγκυρο όνομα (κενό ή περιέχει κενά)'
          user.statusType = 'error'
        } else if (res.status === 409) {
          user.status = ' Το username χρησιμοποιείται ήδη'
          user.statusType = 'error'
        } else {
          throw new Error()
        }
      })
      .catch(() => {
        if (!user.statusType || user.statusType !== 'error') {
           user.status = ' Αποτυχία ενημέρωσης'
           user.statusType = 'error'
        }
      })
    },

    deleteUser(userId) {
      if (!confirm("Σίγουρα θέλετε να διαγράψετε οριστικά αυτόν τον χρήστη; Όλα τα δεδομένα του θα χαθούν.")) return

      fetch(`/api/admin/users/${userId}`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error()
        this.users = this.users.filter(u => u.id !== userId)
      })
      .catch(() => {
        alert(" Αποτυχία διαγραφής")
      })
    },

    createUser() {
      if (!this.newUser.username || !this.newUser.password) {
        this.createStatus = " Παρακαλώ συμπληρώστε όλα τα πεδία."
        this.createStatusType = "error"
        return
      }

      const params = new URLSearchParams()
      params.append('username', this.newUser.username)
      params.append('password', this.newUser.password)
      params.append('role', this.newUser.role)

      fetch("/api/admin/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: params
      })
      .then(async res => {
        if (res.ok) {
          this.createStatus = " Ο χρήστης δημιουργήθηκε με επιτυχία!"
          this.createStatusType = "success"
          this.newUser.username = ''
          this.newUser.password = ''
          this.newUser.role = 'AMATEUR_USER'
          this.loadUsers()
          return
        }

        const error = await res.text()
        if (error === "USERNAME_EXISTS") {
          this.createStatus = " Το όνομα χρήστη χρησιμοποιείται ήδη."
        } else {
          this.createStatus = " Σφάλμα συστήματος κατά τη δημιουργία."
        }
        this.createStatusType = "error"
      })
      .catch(() => {
        this.createStatus = " Αποτυχία επικοινωνίας με τον διακομιστή."
        this.createStatusType = "error"
      })
    },

    saveDocuments(user) {
      const params = new URLSearchParams();
      params.append('afm', user.afm || '');
      params.append('gemiNumber', user.gemiNumber || '');
      params.append('region', user.region || '');

      fetch(`/api/admin/management/override/${user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(res => {
        if (!res.ok) throw new Error();
        user.status = ' Τα στοιχεία εταιρείας ενημερώθηκαν!';
        user.statusType = 'success';
        setTimeout(() => { user.status = '' }, 3000);
      })
      .catch(() => {
        user.status = ' Αποτυχία ενημέρωσης εγγράφων';
        user.statusType = 'error';
      });
    },

    toggleVerification(user) {
      const params = new URLSearchParams();
      params.append('verified', user.verified);

      fetch(`/api/admin/management/verify/${user.id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(res => {
        if (!res.ok) throw new Error();
        user.status = user.verified ? ' Ο επαγγελματίας εγκρίθηκε!' : ' Η έγκριση αφαιρέθηκε.';
        user.statusType = 'success';
        setTimeout(() => { user.status = '' }, 3000);
      })
      .catch(() => {
        user.status = ' Αποτυχία αλλαγής κατάστασης έγκρισης';
        user.statusType = 'error';
        user.verified = !user.verified;
      });
    },

    viewUserApartments(user) {
      if (!this.allApartments) return

      this.selectedUserForModal = user

      this.userApartments = this.allApartments.filter(apt => {
        const owner = apt.user || apt.owner || apt.userId || apt.ownerId

        if (!owner) return false

        if (typeof owner === 'object' && owner.id) {
          return owner.id === user.id
        }

        return owner === user.id
      })

      this.showApartmentsModal = true
    },

    closeModal() {
      this.showApartmentsModal = false
      this.selectedUserForModal = null
      this.userApartments = []
    },
    selectApartmentFromModal(id) {
      this.$emit('select-apartment', id);
      this.closeModal();
    }
  },
  computed: {
    filteredUsers() {
      let res = this.users;
      if (this.roleFilter && this.roleFilter !== 'ALL') {
        res = res.filter(u => u.role === this.roleFilter);
      }
      if (this.searchQuery) {
        const lower = this.searchQuery.toLowerCase();
        res = res.filter(u => u.username && u.username.toLowerCase().includes(lower));
      }
      return res;
    }
  }
}
</script>

<style scoped>
.user-management-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.user-management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  gap: 24px;
  flex-wrap: wrap;
}

.title-section h2 {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.search-wrapper {
  position: relative;
  min-width: 300px;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  padding-left: 42px !important;
}

.user-management-content {
  flex: 1;
}

.section {
  margin-bottom: 40px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
  flex-wrap: wrap;
}

.section h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0;
}


.role-filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
}

.pill-group {
  display: flex;
  background: var(--primary-light);
  padding: 4px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  gap: 4px;
}

.pill-btn {
  display: flex;
  align-items: center;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
  user-select: none;
}

.pill-btn:hover {
  color: var(--primary-color);
  background: rgba(15, 41, 66, 0.05);
}

.pill-btn.active {
  background: var(--primary-color);
  color: #ffffff;
}

.pill-radio {
  display: none;
}


.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.user-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
  padding: 24px;
  background: var(--card-bg);
}

.user-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.user-role-badge {
  font-size: 10px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: var(--radius-full);
}

.user-role-badge.AMATEUR_USER {
  background: #f1f5f9;
  color: #475569;
}
.user-role-badge.CONSTRUCTION_COMPANY {
  background: #eff6ff;
  color: #1d4ed8;
}
.user-role-badge.REAL_ESTATE_AGENCY {
  background: #fdf4ff;
  color: #a21caf;
}
.user-role-badge.ADMIN {
  background: var(--accent-light);
  color: var(--primary-color);
  border: 1px solid var(--accent-color);
}

.user-card-actions {
  display: flex;
  gap: 6px;
}

.btn-action-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: var(--transition-fast);
}

.btn-save { background: #e0f2fe; color: #0369a1; }
.btn-save:hover { background: #bae6fd; transform: translateY(-2px); }
.btn-docs { background: #dcfce7; color: #166534; }
.btn-docs:hover { background: #bbf7d0; transform: translateY(-2px); }
.btn-view { background: #fef3c7; color: #b45309; }
.btn-view:hover { background: #fde68a; transform: translateY(-2px); }
.btn-delete { background: #fee2e2; color: #b91c1c; }
.btn-delete:hover { background: #fecaca; transform: translateY(-2px); }
.btn-profile-view { background: #f3e8ff; color: #a21caf; }
.btn-profile-view:hover { background: #fdf4ff; transform: translateY(-2px); }

.user-card-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-sm {
  padding: 8px 12px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
}

.company-fields {
  margin-top: 8px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fields-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.verification-toggle {
  margin-top: 4px;
}


.checkbox-container-premium {
  display: inline-flex;
  align-items: center;
  position: relative;
  padding-left: 28px;
  cursor: pointer;
  user-select: none;
}

.checkbox-container-premium input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark-premium {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  height: 18px;
  width: 18px;
  background-color: #e2e8f0;
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
  border: 1px solid #cbd5e1;
}

.checkbox-container-premium:hover input ~ .checkmark-premium {
  background-color: #cbd5e1;
}

.checkbox-container-premium input:checked ~ .checkmark-premium {
  background-color: var(--success-color);
  border-color: var(--success-color);
}

.checkmark-premium:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox-container-premium input:checked ~ .checkmark-premium:after {
  display: block;
}

.checkbox-container-premium .checkmark-premium:after {
  left: 6px;
  top: 2px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-main);
}

.status-alert {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.status-alert.success {
  background: var(--success-light);
  color: var(--success-color);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-alert.error {
  background: var(--danger-light);
  color: var(--danger-color);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.section-divider {
  border: 0;
  height: 1px;
  background: var(--border-color);
  margin: 40px 0;
}


.create-user-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 30px;
}

.create-header {
  margin-bottom: 24px;
}

.create-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.create-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.select-premium {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%235e6b7e' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 16px;
  padding-right: 40px !important;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
}

.status-msg-box {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
}

.status-msg-box.success {
  background: var(--success-light);
  color: var(--success-color);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-msg-box.error {
  background: var(--danger-light);
  color: var(--danger-color);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.loading-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-muted);
  gap: 16px;
}

.spinner-large {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: var(--primary-light);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--border-color);
  color: var(--text-muted);
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 41, 66, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  padding: 20px;
}

.modal-content {
  background: var(--card-bg);
  width: 100%;
  max-width: 700px;
  max-height: 80vh;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.modal-header h3 {
  font-size: 20px;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.modal-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: var(--text-muted);
  transition: var(--transition-fast);
  line-height: 1;
}

.btn-close:hover {
  color: var(--danger-color);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.empty-state-modal {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr 1fr;
  }
  .full-width-tablet {
    grid-column: span 2;
  }
}

@media (max-width: 600px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .full-width-tablet {
    grid-column: span 1;
  }
  .user-management-header {
    flex-direction: column;
    align-items: stretch;
  }
  .search-wrapper {
    min-width: unset;
    width: 100%;
  }
}
</style>
