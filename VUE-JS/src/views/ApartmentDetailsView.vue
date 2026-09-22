<template>
  <div class="details-container animate-fade-in">
    <div class="details-header-card card-premium">
      <div class="header-left">
        <button @click="goBack" class="btn-premium btn-premium-secondary btn-sm">
          ← Επιστροφή
        </button>
        <h2> Λεπτομέρειες Διαμερίσματος</h2>
      </div>

      <div class="header-actions" v-if="!loading">
        <button v-if="!editing && canEdit()" @click="startEdit" class="btn-premium btn-premium-primary btn-sm">
          Επεξεργασία
        </button>
        <div v-else-if="editing" class="edit-actions-group">
          <button @click="saveEdit" class="btn-premium btn-premium-primary btn-sm">
            Αποθήκευση
          </button>
          <button @click="cancelEdit" class="btn-premium btn-premium-secondary btn-sm">
            Ακύρωση
          </button>
        </div>
      </div>
    </div>


    <div class="details-body">
      <div v-if="loading" class="loading-state card-premium">
        <div class="spinner-large"></div>
        <p>Φόρτωση στοιχείων διαμερίσματος...</p>
      </div>

      <div v-else class="details-grid">

        <div class="card-premium characteristics-card">
          <h3> Βασικά Χαρακτηριστικά</h3>

          <div v-if="!editing" class="info-grid">
            <div class="info-item">
              <span class="info-label">Περιοχή / Τοποθεσία</span>
              <span class="info-value">{{ apartments.area || 'Μη Καθορισμένη' }}</span>
            </div>

            <div class="info-item">
              <span class="info-label">Εμβαδόν (τ.μ.)</span>
              <span class="info-value">{{ apartments.squareMeters }} τ.μ.</span>
            </div>

            <div class="info-item">
              <span class="info-label">Όροφος</span>
              <span class="info-value">{{ getFloorLabel(apartments.floor) }}</span>
            </div>
          </div>

          <div v-else class="form-grid-custom">
            <div class="form-group-custom">
              <label class="input-label-custom">Περιοχή</label>
              <input v-model="editForm.area" class="form-input-premium" placeholder="π.χ. Χαλάνδρι" />
            </div>

            <div class="form-group-custom">
              <label class="input-label-custom">Τετραγωνικά Μέτρα</label>
              <input type="number" v-model.number="editForm.squareMeters" class="form-input-premium" />
            </div>

            <div class="form-group-custom">
              <label class="input-label-custom">Όροφος</label>
              <input v-model="editForm.floor" class="form-input-premium" placeholder="π.χ. 3ος ή 3" />
            </div>
          </div>
        </div>


        <div class="card-premium evaluation-card">
          <div class="evaluation-header">
            <h3> Εκτίμηση AI</h3>

            <span class="badge-premium" :class="apartments && apartments.aiProcessed ? 'badge-premium-success' : 'badge-premium-pending pulse-animation'">
              {{ apartments && apartments.aiProcessed ? 'ΟΛΟΚΛΗΡΩΘΗΚΕ' : 'ΣΕ ΕΞΕΛΙΞΗ...' }}
            </span>
          </div>

          <div v-if="!editing" class="valuation-strip-container">
            <div v-if="apartments && !apartments.aiProcessed" class="processing-notice">
              <div class="spinner-small"></div>
              <p>Γίνεται αυτόματη επεξεργασία και υπολογισμός των βέλτιστων τιμών από την AI...</p>
            </div>

            <div v-else class="valuation-results animate-fade-in">
              <div class="valuation-box rent">
                <span class="val-label">Προτεινόμενο Μηνιαίο Ενοίκιο</span>
                <span class="val-number">{{ apartments.estimatedRent }} €</span>
                <span class="val-desc">Υπολογισμένο βάσει περιοχής & ζήτησης</span>
              </div>

              <div class="valuation-box sale">
                <span class="val-label">Εκτιμώμενη Αξία Πώλησης</span>
                <span class="val-number">{{ apartments.estimatedPrice }} €</span>
                <span class="val-desc">Τρέχουσα εμπορική αξία ακινήτου</span>
              </div>
            </div>
          </div>


          <div v-else class="form-grid-custom animate-fade-in">
            <div class="form-group-custom">
              <label class="input-label-custom">Μηνιαίο Ενοίκιο (€)</label>
              <input type="number" v-model.number="editForm.estimatedRent" class="form-input-premium" />
            </div>

            <div class="form-group-custom">
              <label class="input-label-custom">Αξία Πώλησης (€)</label>
              <input type="number" v-model.number="editForm.estimatedPrice" class="form-input-premium" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { auth } from '@/stores/auth'

export default {
  name: 'ApartmentDetailsView',
  data() {
    return {
      apartments: null,
      loading: true,
      pollingId: null,
      auth,
      editing: false,
      editForm: {}
    }
  },

  methods: {
    getFloorLabel(floor) {
      if (floor === null || floor === undefined || floor === '') return 'Μη Καθορισμένος';
      if (floor == 0 || floor === '0' || floor === 'Ισόγειο' || floor === 'ισόγειο') return 'Ισόγειο';
      if (!isNaN(floor)) return `${floor}ος Όροφος`;
      return floor;
    },
    goBack() {
      if (this.auth.role === 'ADMIN') {
        this.$router.push('/admin/dashboard');
      } else {
        this.$router.push('/dashboard');
      }
    },
    fetchApartment() {
      const id = this.$route.params.id

      fetch(`/api/apartments/${id}`)
        .then(res => res.json())
        .then(data => {
          this.apartments = data
          this.loading = false

          if (data.aiProcessed && this.pollingId) {
            clearInterval(this.pollingId)
            this.pollingId = null
          }
        })
        .catch(err => {
          console.error('Error fetching apartment:', err);
          this.loading = false;
        })
    },
    canEdit() {
      if (!this.auth.user || !this.apartments) return false
      const userId = this.auth.user.id
      const ownerCandidates = [
        this.apartments.userId,
        this.apartments.ownerId,
        this.apartments.owner,
        this.apartments.user && this.apartments.user.id
      ]
      const isOwner = ownerCandidates.some(v => v && v === userId)
      return isOwner
    },
    startEdit() {
      if (!this.canEdit()) return
      this.editing = true
      this.editForm = Object.assign({}, this.apartments)
    },
    cancelEdit() {
      this.editing = false
      this.editForm = {}
    },
    saveEdit() {
      const payload = Object.assign({}, this.editForm)
      if (!this.apartments) return
      const id = this.apartments.id

      fetch(`/api/apartments/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(res => {
          if (!res.ok) throw new Error()
          return res.json()
        })
        .then(data => {
          this.apartments = data
          this.editing = false
          this.editForm = {}
        })
        .catch(err => {
          console.error('Failed to save apartment', err)
          this.editing = false
        })
    }
  },

  mounted() {
    this.fetchApartment()

    this.pollingId = setInterval(() => {
      this.fetchApartment()
    }, 2000)
  },

  beforeUnmount() {
    if (this.pollingId) {
      clearInterval(this.pollingId)
    }
  }
}
</script>

<style scoped>
.details-container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.details-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  font-size: 22px;
  margin: 0;
  color: var(--primary-color);
}

.edit-actions-group {
  display: flex;
  gap: 10px;
}

.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
}

.details-body {
  width: 100%;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

@media (max-width: 800px) {
  .details-grid {
    grid-template-columns: 1fr;
  }
}

.characteristics-card h3, .evaluation-card h3 {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary-color);
  margin: 0 0 20px 0;
  border-bottom: 2px solid var(--primary-light);
  padding-bottom: 8px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-main);
}

.form-grid-custom {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group-custom {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label-custom {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
}


.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid var(--primary-light);
  padding-bottom: 8px;
}

.evaluation-header h3 {
  border-bottom: none;
  margin: 0;
  padding: 0;
}

.valuation-strip-container {
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.processing-notice {
  text-align: center;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.processing-notice p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.valuation-results {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.valuation-box {
  border-radius: var(--radius-md);
  padding: 16px 20px;
  border: 1px solid var(--border-color);
  transition: var(--transition-normal);
}

.valuation-box:hover {
  transform: translateX(4px);
}

.valuation-box.rent {
  background: var(--success-light);
  border-left: 5px solid var(--success-color);
}

.valuation-box.sale {
  background: var(--info-light);
  border-left: 5px solid var(--info-color);
}

.val-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.val-number {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: var(--primary-color);
  font-family: var(--font-title);
  margin-bottom: 2px;
}

.val-desc {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
}

.pulse-animation {
  animation: pulse 1.5s infinite ease-in-out;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-muted);
  gap: 16px;
  text-align: center;
}

.spinner-large {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-small {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
