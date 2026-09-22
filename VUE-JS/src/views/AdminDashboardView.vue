<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-brand">
        <span class="brand-text">Estate<span class="highlight">Sol</span></span>
        <span class="brand-separator">|</span>
        <span class="brand-subtext">Διαχείριση Συστήματος</span>
      </div>

      <div class="nav-user">
        <span class="welcome-text">Γεια σου, <strong>{{ auth.user?.username || 'Admin' }}</strong></span>
        <span class="role-badge ADMIN">ADMINISTRATOR</span>
        <button @click="logout" class="btn-logout">Έξοδος </button>
      </div>
    </nav>

    <div class="main-content">
      <div class="toolbar">
        <h2>Όλα τα Ακίνητα Συστήματος</h2>
      </div>


      <div class="layout">

        <div class="left-panel">
          <div class="panel-header">
            <h3>ΛΙΣΤΑ ΑΚΙΝΗΤΩΝ</h3>
          </div>

          <div class="search-filter-wrapper">
            <input
              type="text"
              v-model="searchQuery"
              placeholder=" Αναζήτηση ID ή Περιοχής..."
              class="form-input-premium search-bar"
            />
          </div>


          <div class="pool-filter-tabs">
            <button @click="aptFilter = 'ALL'" :class="{ active: aptFilter === 'ALL' }" class="filter-tab-btn">Όλα</button>
            <button @click="aptFilter = 'POOL_CONSTRUCTION'" :class="{ active: aptFilter === 'POOL_CONSTRUCTION' }" class="filter-tab-btn"> Κατασκευών</button>
            <button @click="aptFilter = 'POOL_REAL_ESTATE'" :class="{ active: aptFilter === 'POOL_REAL_ESTATE' }" class="filter-tab-btn"> Μεσιτών</button>
            <button @click="aptFilter = 'OTHER'" :class="{ active: aptFilter === 'OTHER' }" class="filter-tab-btn"> Λοιπά</button>
          </div>

          <div class="list-scroll-area">
            <ApartmentList
              :apartments="filteredApartments"
              :loading="loading"
              @select="openDetails"
            />
          </div>
        </div>


        <div class="right-panel">

          <div v-if="mode === 'list'" class="empty-detail-state">
            <p>Επιλέξτε ένα διαμέρισμα από τη λίστα για να προβάλλετε τις λεπτομέρειες και τις AI εκτιμήσεις.</p>
          </div>


          <div v-else-if="mode === 'details'" class="details-wrapper animate-fade-in">
            <div v-if="detailsLoading" class="loading-details">
              <div class="spinner"></div>
              <p>Φόρτωση λεπτομερειών ακινήτου...</p>
            </div>

            <div v-else-if="selectedApartment">
              <div class="details-header-card">
                <div class="details-title-group">
                  <div class="title-top">
                    <span class="id-tag">ΑΚΙΝΗΤΟ #{{ selectedApartment.id }}</span>
                    <span class="status-badge" :class="selectedApartment.status">{{ selectedApartment.status }}</span>
                  </div>
                  <h2>{{ selectedApartment.area }}</h2>
                  <p class="details-subtitle"> {{ selectedApartment.squareMeters }} τ.μ. •  Όροφος: {{ selectedApartment.floor }}ος</p>
                </div>

                <div class="details-actions">
                  <div v-if="!editing" class="action-btn-group">
                    <button @click="startEdit" class="btn-action btn-edit"> Επεξεργασία</button>
                    <button @click="deleteApartment(selectedApartment.id)" class="btn-action btn-delete"> Διαγραφή</button>
                  </div>
                  <div v-else class="action-btn-group">
                    <button @click="saveEdit" class="btn-action btn-save"> Αποθήκευση</button>
                    <button @click="cancelEdit" class="btn-action btn-cancel"> Άκυρο</button>
                  </div>
                </div>
              </div>


              <div v-if="!editing" class="details-body">
                <div class="detail-info-grid">
                  <div class="info-card">
                    <span class="info-label">Ιδιοκτήτης</span>
                    <span class="info-value">{{ getOwnerId() }}</span>
                  </div>
                  <div class="info-card">
                    <span class="info-label">Περιοχή (Region)</span>
                    <span class="info-value">{{ selectedApartment.region || 'N/A' }}</span>
                  </div>
                  <div class="info-card">
                    <span class="info-label">Google Maps Link</span>
                    <span class="info-value">
                      <a v-if="selectedApartment.googleMapsPin" :href="selectedApartment.googleMapsPin" target="_blank" class="maps-link"> Προβολή Χάρτη</a>
                      <span v-else class="text-muted">N/A</span>
                    </span>
                  </div>
                  <div class="info-card">
                    <span class="info-label">Budget Ιδιοκτήτη</span>
                    <span class="info-value budget-highlight">{{ selectedApartment.budget ? selectedApartment.budget + ' €' : 'Basic Habitability' }}</span>
                  </div>
                </div>


                <div class="ai-pricing-section">
                  <div class="ai-section-header">
                    <h3>Εκτιμήσεις AI & Ανακαίνιση</h3>
                  </div>


                  <div class="cost-estimate-card">
                    <h4> Εκτίμηση Κόστους Ανακαίνισης</h4>
                    <p class="cost-value-desc">{{ selectedApartment.estimatedCost || 'Υπολογισμός...' }}</p>
                  </div>


                  <div v-if="selectedApartment.aiProcessingError" class="ai-error-box">
                    <span>{{ selectedApartment.aiProcessingError }}</span>
                    <button @click="retryAiProcessing(selectedApartment.id)" class="btn-retry-ai">
                       Επανάληψη δημιουργίας εικόνας
                    </button>
                  </div>
                  <div v-if="!selectedApartment.aiProcessed && !selectedApartment.aiProcessingError" class="ai-processing-box">
                    <div class="spinner-small"></div>
                    <span>Γίνεται επεξεργασία δεδομένων από την AI...</span>
                  </div>
                  <div v-if="selectedApartment.aiProcessed || selectedApartment.aiProcessingError" class="valuation-cards">
                    <div class="val-card rent-card">
                      <span class="val-label">Προτεινόμενο Μηνιαίο Ενοίκιο (AI)</span>
                      <span class="val-amount">{{ selectedApartment.estimatedRent ? selectedApartment.estimatedRent + ' €' : 'N/A' }}</span>
                    </div>
                    <div class="val-card sale-card">
                      <span class="val-label">Εκτιμώμενη Αξία Πώλησης (AI)</span>
                      <span class="val-amount">{{ selectedApartment.estimatedPrice ? selectedApartment.estimatedPrice + ' €' : 'N/A' }}</span>
                    </div>
                  </div>
                </div>


                <div v-if="selectedApartment.imagePath || selectedApartment.renovatedImagePath" class="images-section">
                  <h3> Φωτογραφίες Χώρου</h3>
                  <div class="images-grid">
                    <div v-if="selectedApartment.imagePath" class="img-container">
                      <span class="img-badge original">Πριν</span>
                      <img
                        :src="backendUrl + selectedApartment.imagePath.replace(/\\/g, '/')"
                        alt="Αρχική Κατάσταση"
                        class="apartment-image"
                      />
                    </div>
                    <div v-if="selectedApartment.renovatedImagePath" class="img-container">
                      <span class="img-badge renovated">Μετά (AI)</span>
                      <img
                        :src="backendUrl + selectedApartment.renovatedImagePath.replace(/\\/g, '/')"
                        alt="AI Πρόταση Ανακαίνισης"
                        class="apartment-image"
                      />
                    </div>
                  </div>
                </div>
              </div>


              <div v-else class="details-edit-form">
                <div class="form-grid">
                  <div class="form-group">
                    <label>Περιοχή</label>
                    <input v-model="editForm.area" class="form-input-premium" />
                  </div>
                  <div class="form-group">
                    <label>Τετραγωνικά</label>
                    <input type="number" v-model.number="editForm.squareMeters" class="form-input-premium" />
                  </div>
                  <div class="form-group">
                    <label>Όροφος</label>
                    <input type="number" v-model.number="editForm.floor" class="form-input-premium" />
                  </div>
                  <div class="form-group">
                    <label>Ενοίκιο (€)</label>
                    <input type="number" v-model.number="editForm.estimatedRent" class="form-input-premium" />
                  </div>
                  <div class="form-group">
                    <label>Πώληση (€)</label>
                    <input type="number" v-model.number="editForm.estimatedPrice" class="form-input-premium" />
                  </div>
                  <div class="form-group">
                    <label>Κατάσταση Έργου (Status)</label>
                    <select v-model="editForm.status" class="form-input-premium select-control">
                      <option value="DRAFT">Draft</option>
                      <option value="VISUALIZED">Visualized</option>
                      <option value="POOL_CONSTRUCTION">Pool Construction</option>
                      <option value="CONTRACT_LOCKED">Contract Locked</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="POOL_REAL_ESTATE">Pool Real Estate</option>
                    </select>
                  </div>
                  <div class="form-group full-width">
                    <label>Εκτίμηση Κόστους Ανακαίνισης</label>
                    <textarea v-model="editForm.estimatedCost" class="form-input-premium" rows="4"></textarea>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>

      <hr class="section-divider" />


      <div class="user-management-panel animate-fade-in">
        <UserManagement :allApartments="apartments" @select-apartment="openDetails" />
      </div>
    </div>
  </div>
</template>

<script>
import ApartmentList from '@/components/ApartmentList.vue'
import ApartmentCreateForm from '@/components/ApartmentCreateForm.vue'
import UserManagement from '@/components/UserManagement.vue'
import { auth } from '@/stores/auth'

export default {
  name: 'AdminDashboardView',
  components: {
    ApartmentList,
    ApartmentCreateForm,
    UserManagement
  },
  data() {
    return {
      mode: 'list',
      apartments: [],
      loading: true,
      backendUrl: 'http://localhost:8082/',
      selectedApartment: null,
      detailsLoading: false,
      listPollingId: null,
      detailsPollingId: null,
      auth,
      editing: false,
      editForm: {},
      searchQuery: '',
      aptFilter: 'ALL'
    }
  },
  computed: {
    filteredApartments() {
      let result = this.apartments;
      if (this.aptFilter && this.aptFilter !== 'ALL') {
        if (this.aptFilter === 'POOL_CONSTRUCTION') {
          result = result.filter(a => a.status === 'POOL_CONSTRUCTION');
        } else if (this.aptFilter === 'POOL_REAL_ESTATE') {
          result = result.filter(a => a.status === 'POOL_REAL_ESTATE');
        } else if (this.aptFilter === 'OTHER') {
          result = result.filter(a => a.status !== 'POOL_CONSTRUCTION' && a.status !== 'POOL_REAL_ESTATE');
        }
      }
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(a => {
          const idMatch = a.id && String(a.id).includes(query);
          const regionMatch = a.region && a.region.toLowerCase().includes(query);
          const areaMatch = a.area && a.area.toLowerCase().includes(query);
          return idMatch || regionMatch || areaMatch;
        });
      }

      return result;
    }
  },
  methods: {
    logout() {
      fetch('/logout', { method: 'POST' })
        .finally(() => {
           window.location.href = '/login'
        })
    },
    openDetails(id) {
      this.mode = 'details'
      this.detailsLoading = true
      this.selectedApartment = null
      this.editing = false
      this.editForm = {}

      fetch(`/api/apartments/${id}`)
        .then(res => res.json())
        .then(data => {
          this.selectedApartment = data
          this.detailsLoading = false
          if (data && !data.aiProcessed && !data.aiProcessingError) {
            this.startDetailsPolling(id)
          }
        })
        .catch(err => {
          console.error('Failed to load apartment details', err)
          this.detailsLoading = false
        })
    },
    onCreated(id) {
      this.openDetails(id)
      this.reloadApartments()
    },
    startEdit() {
      this.editing = true
      this.editForm = Object.assign({}, this.selectedApartment)
    },
    cancelEdit() {
      this.editing = false
      this.editForm = {}
    },
    getOwnerId() {
      if (!this.selectedApartment) return 'N/A'
      const val = this.selectedApartment.user_id ||
                  this.selectedApartment.userId ||
                  this.selectedApartment.user ||
                  this.selectedApartment.ownerId ||
                  this.selectedApartment.owner
      if (!val) return 'N/A'
      if (typeof val === 'object') {
        return val.username || val.name || 'N/A'
      }
      if (typeof val === 'string' && val.includes('username=')) {
        const match = val.match(/username=([^,\s)}\]]+)/)
        if (match && match[1]) return match[1]
      }
      return val
    },
    saveEdit() {
      const id = this.selectedApartment.id
      const dto = {
        area: this.editForm.area,
        squareMeters: this.editForm.squareMeters,
        floor: this.editForm.floor,
        estimatedPrice: this.editForm.estimatedPrice,
        estimatedRent: this.editForm.estimatedRent,
        estimatedCost: this.editForm.estimatedCost,
        status: this.editForm.status
      }
      fetch(`/api/apartments/updateasadmin/${id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
      })
        .then(res => {
          if (!res.ok) throw new Error()
          return fetch(`/api/apartments/${id}`)
        })
        .then(r => {
          if (!r.ok) throw new Error()
          return r.json()
        })
        .then(data => {
          this.selectedApartment = data
          this.editing = false
          this.editForm = {}
          this.reloadApartments()
          if (data && !data.aiProcessed && !data.aiProcessingError) this.startDetailsPolling(id)
        })
        .catch(err => {
          console.error('Failed to save apartment', err)
          this.editing = false
        })
    },
    deleteApartment(id) {
      if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το ακίνητο;')) return
      fetch(`/api/apartments/delete/${id}`, {
        method: 'POST'
      })
        .then(res => {
          if (!res.ok) throw new Error()
          this.selectedApartment = null
          this.mode = 'list'
          this.reloadApartments()
        })
        .catch(err => {
          console.error('Failed to delete apartment', err)
          alert('Σφάλμα κατά τη διαγραφή του ακινήτου.')
        })
    },
    reloadApartments() {
      fetch('/api/apartments')
        .then(res => res.json())
        .then(data => {
          this.apartments = data
          this.loading = false
        })
        .catch(err => {
          console.error('Failed to reload apartments', err)
          this.loading = false
        })
    },
    startDetailsPolling(id) {
      if (this.detailsPollingId) {
        clearInterval(this.detailsPollingId)
      }
      this.detailsPollingId = setInterval(() => {
        fetch(`/api/apartments/${id}`)
          .then(res => res.json())
          .then(data => {
            this.selectedApartment = data
            if ((data.aiProcessed || data.aiProcessingError) && this.detailsPollingId) {
              clearInterval(this.detailsPollingId)
              this.detailsPollingId = null
            }
          })
      }, 2000)
    },
    retryAiProcessing(id) {
      this.selectedApartment.aiProcessingError = null
      this.selectedApartment.aiProcessed = false
      fetch(`/api/apartments/${id}/retry-ai`, { method: 'POST' })
        .then(res => {
          if (!res.ok) throw new Error()
          this.startDetailsPolling(id)
        })
        .catch(() => {
          this.selectedApartment.aiProcessingError = 'Η επανάληψη της AI επεξεργασίας δεν ξεκίνησε.'
        })
    }
  },
  mounted() {
    this.reloadApartments()
    this.listPollingId = setInterval(() => {
      this.reloadApartments()
    }, 5000)
  },
  beforeUnmount() {
    if (this.listPollingId) {
      clearInterval(this.listPollingId)
    }
    if (this.detailsPollingId) {
      clearInterval(this.detailsPollingId)
    }
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: var(--bg-color);
  display: flex;
  flex-direction: column;
}

.navbar {
  background: var(--primary-color);
  padding: 14px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  font-size: 24px;
}

.brand-text {
  font-family: var(--font-title);
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.5px;
}

.brand-text .highlight {
  color: var(--accent-color);
}

.brand-separator {
  color: rgba(255, 255, 255, 0.2);
  margin: 0 8px;
}

.brand-subtext {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.5px;
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.welcome-text strong {
  color: #ffffff;
}

.role-badge {
  font-size: 10px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.1);
  color: var(--accent-color);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  letter-spacing: 0.5px;
}

.btn-logout {
  padding: 8px 14px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-weight: 700;
  font-size: 12px;
  transition: var(--transition-fast);
}

.btn-logout:hover {
  background: var(--danger-color);
  border-color: var(--danger-color);
  color: #ffffff;
}

.main-content {
  padding: 30px 40px;
  flex: 1;
  max-width: 1600px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.toolbar {
  margin-bottom: 24px;
}

.toolbar h2 {
  font-size: 22px;
  color: var(--primary-color);
}


.layout {
  display: flex;
  gap: 30px;
  height: calc(100vh - 180px);
}

.left-panel {
  width: 380px;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  background: #fafbfc;
}

.panel-header h3 {
  font-size: 11px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 1px;
}

.search-filter-wrapper {
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid var(--border-color);
}

.search-bar {
  font-size: 13px;
  padding: 8px 12px;
}

.list-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.right-panel {
  flex: 1;
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  overflow-y: auto;
  padding: 30px;
  position: relative;
}


.empty-detail-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
}

.empty-detail-state .empty-icon {
  font-size: 44px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-detail-state p {
  font-size: 14px;
  line-height: 1.6;
}


.details-header-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 24px;
  margin-bottom: 24px;
  gap: 20px;
}

.details-title-group {
  flex: 1;
}

.title-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.id-tag {
  font-size: 11px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}

.details-title-group h2 {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 6px;
}

.details-subtitle {
  font-size: 14px;
  color: var(--text-muted);
}

.details-actions {
  display: flex;
  align-items: center;
}

.action-btn-group {
  display: flex;
  gap: 10px;
}

.btn-action {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-fast);
  border: none;
}

.btn-edit {
  background: var(--primary-light);
  color: var(--primary-color);
}

.btn-edit:hover {
  background: #e2eaf4;
}

.btn-delete {
  background: var(--danger-light);
  color: var(--danger-color);
}

.btn-delete:hover {
  background: #fee2e2;
}

.btn-save {
  background: var(--success-color);
  color: #ffffff;
}

.btn-save:hover {
  background: #059669;
}

.btn-cancel {
  background: var(--border-color);
  color: var(--text-main);
}

.btn-cancel:hover {
  background: var(--border-hover);
}


.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.info-card {
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
}

.info-label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.info-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
}

.maps-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 700;
}

.maps-link:hover {
  text-decoration: underline;
}

.budget-highlight {
  color: var(--success-color);
}


.ai-pricing-section {
  background: linear-gradient(135deg, #132435 0%, #0a1420 100%);
  border-radius: var(--radius-lg);
  padding: 24px;
  color: #ffffff;
  margin-bottom: 28px;
  box-shadow: var(--shadow-md);
}

.ai-section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 12px;
}

.ai-sparkle {
  font-size: 20px;
}

.ai-section-header h3 {
  color: #ffffff;
  font-size: 16px;
  margin: 0;
}

.cost-estimate-card h4 {
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.cost-value-desc {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent-color);
  margin: 0 0 16px 0;
}

.ai-processing-box {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  padding: 10px 0;
}

.ai-error-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  border: 1px solid #fecaca;
  border-radius: var(--radius-md);
  background: #fef2f2;
  color: #991b1b;
  font-size: 13px;
  font-weight: 600;
}

.btn-retry-ai {
  border: 0;
  border-radius: var(--radius-sm);
  padding: 9px 12px;
  background: #b91c1c;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.valuation-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.val-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
}

.val-label {
  display: block;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 600;
  margin-bottom: 6px;
}

.val-amount {
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
}


.images-section h3 {
  font-size: 15px;
  margin-bottom: 14px;
}

.images-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.img-container {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.img-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  font-size: 10px;
  font-weight: 800;
  border-radius: var(--radius-sm);
  color: white;
  text-transform: uppercase;
}

.img-badge.original {
  background: rgba(15, 41, 66, 0.7);
}

.img-badge.renovated {
  background: var(--success-color);
}

.apartment-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}

.img-container:hover .apartment-image {
  transform: scale(1.03);
}


.details-edit-form .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.select-control {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23475569' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 16px;
  padding-right: 40px;
}


.section-divider {
  border: 0;
  height: 1px;
  background: var(--border-color);
  margin: 40px 0;
}


.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 10px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.DRAFT { background: #e2e8f0; color: #475569; }
.status-badge.VISUALIZED { background: var(--info-light); color: var(--info-color); }
.status-badge.POOL_CONSTRUCTION { background: var(--warning-light); color: var(--warning-color); }
.status-badge.CONTRACT_LOCKED { background: var(--danger-light); color: var(--danger-color); }
.status-badge.COMPLETED { background: var(--success-light); color: var(--success-color); }
.status-badge.POOL_REAL_ESTATE { background: #f3e8ff; color: #6b21a8; }

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 20px auto;
}

.spinner-small {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-details {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
}

.pool-filter-tabs {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  margin: 10px 16px 0;
  border-radius: 8px;
  gap: 4px;
  border: 1px solid var(--border-color);
}

.filter-tab-btn {
  flex: 1;
  padding: 6px 4px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  color: var(--text-muted);
  transition: var(--transition-fast);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.filter-tab-btn:hover {
  color: var(--primary-color);
  background: rgba(255, 255, 255, 0.5);
}

.filter-tab-btn.active {
  background: #ffffff;
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}
</style>
