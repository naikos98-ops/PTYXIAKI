<template>
  <div class="pool-container animate-fade">
    <div class="pool-header">
      <h2> {{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Μεσιτικό Pool (Leads Marketplace)' : 'Κατασκευαστικό Pool (Leads Marketplace)' }}</h2>
      <p class="pool-subtitle">
        {{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Βρείτε ανακαινισμένα ακίνητα έτοιμα προς ενοικίαση / ανάληψη και καταθέστε προτάσεις.' : 'Βρείτε ενεργά έργα στην περιοχή σας και καταθέστε οικονομικές προσφορές.' }}
      </p>
    </div>


    <div class="filters-card">
      <div class="filters-grid">
        <div class="form-group">
          <label> Αναζήτηση Δήμου / Περιοχής</label>
          <input
            type="text"
            v-model="searchRegion"
            placeholder="π.χ. Δήμος Αθηναίων"
            class="form-input-premium"
            @keyup.enter="applyFilters"
          />
        </div>
        <div class="form-group">
          <label> Ελάχιστα τ.μ.</label>
          <input
            type="number"
            v-model.number="minSqm"
            placeholder="π.χ. 50"
            class="form-input-premium"
            @keyup.enter="applyFilters"
          />
        </div>
        <div class="form-group">
          <label>{{ auth.role === 'REAL_ESTATE_AGENCY' ? ' Μέγιστο Ενοίκιο (€)' : ' Μέγιστο Budget (€)' }}</label>
          <input
            type="number"
            v-model.number="maxBudget"
            placeholder="π.χ. 50000"
            class="form-input-premium"
            @keyup.enter="applyFilters"
          />
        </div>
      </div>
      <div class="filters-actions">
        <button @click="applyFilters" class="btn-search">Αναζήτηση </button>
        <button @click="clearFilters" class="btn-clear-filters">Καθαρισμός </button>
      </div>
    </div>


    <div class="leads-section">
      <h3>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Διαθέσιμα Ακίνητα' : 'Διαθέσιμα Leads' }} ({{ filteredLeads.length }})</h3>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Φόρτωση διαθέσιμων ακινήτων...</p>
      </div>

      <div v-else-if="filteredLeads.length === 0" class="empty-state-placeholder">
        <p> Δεν βρέθηκαν διαθέσιμα ακίνητα στο Pool αυτή τη στιγμή.</p>
      </div>

      <div v-else class="leads-grid">
        <div v-for="lead in filteredLeads" :key="lead.id" class="lead-card" :class="{ 'is-unlocked': lead.unlocked }">
          <div class="lead-card-header">
            <span class="badge-region"> {{ lead.area && lead.region ? lead.area + ', ' + lead.region : (lead.region || lead.area || 'Άγνωστη Περιοχή') }}</span>
            <span class="lead-status" :class="lead.status">{{ getLeadStatusLabel(lead.status) }}</span>
          </div>


          <div v-if="lead.imagePath || lead.renovatedImagePath" class="pool-images-section">
            <div class="pool-images-grid">
              <div v-if="lead.imagePath" class="pool-image-wrapper">
                <span class="pool-image-label">Πριν</span>
                <img
                  :src="backendUrl + lead.imagePath.replace(/\\/g, '/')"
                  alt="Original"
                  class="pool-apartment-image"
                />
              </div>
              <div v-if="lead.renovatedImagePath" class="pool-image-wrapper">
                <span class="pool-image-label">Μετά (AI)</span>
                <img
                  :src="backendUrl + lead.renovatedImagePath.replace(/\\/g, '/')"
                  alt="Renovated"
                  class="pool-apartment-image"
                />
              </div>
            </div>
          </div>

          <div class="lead-details">
            <div class="detail-row">
              <span class="label">Μέγεθος:</span>
              <span class="value">{{ lead.squareMeters }} τ.μ.</span>
            </div>
            <div class="detail-row">
              <span class="label">Όροφος:</span>
              <span class="value">{{ lead.floor }}ος</span>
            </div>
            <div class="detail-row">
              <span class="label">{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Εκτιμώμενο Ενοίκιο:' : 'Budget Ιδιοκτήτη:' }}</span>
              <span class="value budget-value">{{ lead.budget ? lead.budget + ' €' : (auth.role === 'REAL_ESTATE_AGENCY' ? 'N/A' : 'Basic Habitability') }}</span>
            </div>
          </div>

          <div class="gdpr-box">
            <div class="gdpr-item">
              <span class="label">Τοποθεσία:</span>
              <span class="value masked" v-if="!lead.unlocked"> {{ lead.googleMapsPin }}</span>
              <a :href="lead.googleMapsPin" target="_blank" class="value link" v-else> Google Maps Pin</a>
            </div>
            <div class="gdpr-item">
              <span class="label">Ιδιοκτήτης:</span>
              <span class="value masked" v-if="!lead.unlocked"> {{ lead.ownerName }}</span>
              <span class="value" v-else> {{ lead.ownerName }} (Accepted Bid!)</span>
            </div>
          </div>

          <div class="lead-actions">
            <button @click="openBidModal(lead)" class="btn-bid-primary" :disabled="hasSubmittedBid(lead.id)">
              {{ hasSubmittedBid(lead.id) ? (auth.role === 'REAL_ESTATE_AGENCY' ? 'Υποβλήθηκε Πρόταση' : 'Υποβλήθηκε Προσφορά') : (auth.role === 'REAL_ESTATE_AGENCY' ? 'Πρόταση Ενοικίασης / Ανάληψης ' : 'Υποβολή Προσφοράς Κατασκευής ') }}
            </button>
          </div>
        </div>
      </div>
    </div>


    <hr class="pool-divider" />
    <div class="my-bids-section">
      <h3>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Οι Προτάσεις μου' : 'Οι Προσφορές μου' }}</h3>
      <div v-if="myBids.length === 0" class="empty-state-bids">
        <p>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Δεν έχετε υποβάλει ακόμα καμία πρόταση.' : 'Δεν έχετε υποβάλει ακόμα καμία προσφορά.' }}</p>
      </div>
      <div v-else class="bids-list">
        <div v-for="bid in myBids" :key="bid.id" class="bid-item-card" :class="bid.status">
          <div class="bid-header">
            <h4>Έργο #{{ bid.project?.id }} ({{ bid.project?.region || bid.project?.area }})</h4>
            <span class="bid-badge-status" :class="bid.status">{{ bid.status }}</span>
          </div>
          <div class="bid-body">
            <p><strong>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Προτεινόμενο Ενοίκιο:' : 'Οικονομική Προσφορά:' }}</strong> {{ bid.estimatedCost }} € (συμπεριλαμβανομένου ΦΠΑ 24%)</p>
            <p><strong>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Λεπτομέρειες Πρότασης:' : 'Σημειώσεις Προσφοράς:' }}</strong> {{ bid.proposalDetails }}</p>

            <div v-if="bid.status === 'ACCEPTED' && bid.project?.status === 'CONTRACT_LOCKED'" style="margin-top: 12px; border-top: 1px solid var(--border-color); padding-top: 12px;">
              <button @click="completeRenovation(bid.project.id)" class="btn-complete-renovation">
                Σήμανση ως Ολοκληρωμένο
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>


    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal animate-slide-down">
        <div class="modal-header">
          <h3>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Πρόταση Ενοικίασης / Ανάληψης για το Ακίνητο #' + selectedLead?.id : 'Κατάθεση Προσφοράς για το Έργο #' + selectedLead?.id }}</h3>
          <button @click="closeModal" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Προτεινόμενο Ενοίκιο (€ / μήνα)' : 'Οικονομική Προσφορά (€)' }}</label>
            <input
              type="number"
              v-model="bidForm.estimatedCost"
              placeholder="π.χ. 650"
              class="form-input-premium"
            />
            <small class="vat-info">{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Συμπεριλαμβάνει ΦΠΑ 24% (αν εφαρμόζεται).' : 'Συμπεριλαμβάνει ΦΠΑ 24% στις ελληνικές κατασκευαστικές τιμολογήσεις.' }}</small>
          </div>
          <div class="form-group">
            <label>{{ auth.role === 'REAL_ESTATE_AGENCY' ? 'Λεπτομέρειες Πρότασης (Διάρκεια συμβολαίου, εγγύηση, σχόλια)' : 'Λεπτομέρειες Πρότασης (Χρονοδιάγραμμα, υλικά, σχόλια)' }}</label>
            <textarea
              v-model="bidForm.proposalDetails"
              placeholder="Περιγράψτε την πρότασή σας..."
              rows="5"
              class="form-input-premium"
            ></textarea>
          </div>
          <div class="modal-actions">
            <button @click="submitBid" class="btn-submit" :disabled="submitting">
              {{ submitting ? 'Υποβολή...' : (auth.role === 'REAL_ESTATE_AGENCY' ? 'Υποβολή Πρότασης' : 'Υποβολή Προσφοράς') }}
            </button>
          </div>
          <p v-if="modalError" class="modal-error-msg">{{ modalError }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { auth } from '@/stores/auth'

export default {
  name: 'ConstructionPool',
  data() {
    return {
      auth,
      leads: [],
      myBids: [],
      loading: false,
      submitting: false,
      searchRegion: '',
      minSqm: '',
      maxBudget: '',
      appliedSearchRegion: '',
      appliedMinSqm: '',
      appliedMaxBudget: '',
      showModal: false,
      selectedLead: null,
      bidForm: {
        estimatedCost: '',
        proposalDetails: ''
      },
      modalError: '',
      backendUrl: 'http://localhost:8082/'
    }
  },
  computed: {
    filteredLeads() {
      return this.leads.filter(lead => {
        if (this.appliedSearchRegion) {
          const search = this.appliedSearchRegion.toLowerCase();
          const region = (lead.region || '').toLowerCase();
          const area = (lead.area || '').toLowerCase();
          if (!region.includes(search) && !area.includes(search)) return false;
        }
        if (this.appliedMinSqm && lead.squareMeters < this.appliedMinSqm) {
          return false;
        }
        if (this.appliedMaxBudget && lead.budget && lead.budget > this.appliedMaxBudget) {
          return false;
        }
        return true;
      });
    }
  },
  methods: {
    applyFilters() {
      this.appliedSearchRegion = this.searchRegion;
      this.appliedMinSqm = this.minSqm;
      this.appliedMaxBudget = this.maxBudget;
    },
    clearFilters() {
      this.searchRegion = '';
      this.minSqm = '';
      this.maxBudget = '';
      this.applyFilters();
    },
    fetchLeadsAndBids() {
      this.loading = true;
      Promise.all([
        fetch('/api/pool').then(res => {
          if (!res.ok) throw new Error();
          return res.json();
        }),
        fetch('/api/pool/bids').then(res => {
          if (!res.ok) throw new Error();
          return res.json();
        })
      ])
      .then(([leadsData, bidsData]) => {
        this.leads = leadsData;
        this.myBids = bidsData;
        this.applyFilters();
      })
      .catch(err => {
        console.error('Failed to load marketplace pool leads/bids', err);
      })
      .finally(() => {
        this.loading = false;
      });
    },
    hasSubmittedBid(projectId) {
      return this.myBids.some(bid => bid.project?.id === projectId);
    },
    openBidModal(lead) {
      this.selectedLead = lead;
      this.bidForm.estimatedCost = '';
      this.bidForm.proposalDetails = '';
      this.modalError = '';
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
      this.selectedLead = null;
    },
    submitBid() {
      if (!this.bidForm.estimatedCost || !this.bidForm.proposalDetails) {
        this.modalError = 'Παρακαλούμε συμπληρώστε όλα τα πεδία.';
        return;
      }
      this.submitting = true;
      this.modalError = '';

      const params = new URLSearchParams();
      params.append('estimatedCost', this.bidForm.estimatedCost);
      params.append('proposalDetails', this.bidForm.proposalDetails);

      fetch(`/api/pool/${this.selectedLead.id}/bid`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(res => {
        if (res.status === 403) {
          throw new Error('Η εγγραφή σας εκκρεμεί έγκριση AFM/GEMI από τους διαχειριστές.');
        }
        if (!res.ok) throw new Error('Αποτυχία υποβολής προσφοράς.');
        this.closeModal();
        this.fetchLeadsAndBids();
      })
      .catch(err => {
        this.modalError = err.message;
      })
      .finally(() => {
        this.submitting = false;
      });
    },
    completeRenovation(projectId) {
      if (!confirm('Θέλετε να δηλώσετε την ολοκλήρωση των εργασιών ανακαίνισης;')) return;
      fetch(`/api/apartments/${projectId}/complete-renovation`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        alert('Το έργο επισημάνθηκε ως ολοκληρωμένο!');
        this.fetchLeadsAndBids();
      })
      .catch(() => {
        alert('Σφάλμα κατά την ενημέρωση του έργου.');
      });
    },
    getLeadStatusLabel(status) {
      if (status === 'POOL_CONSTRUCTION') return ' Pool Κατασκευής';
      if (status === 'POOL_REAL_ESTATE') return ' Pool Μεσιτών';
      return status;
    }
  },
  mounted() {
    this.fetchLeadsAndBids();
  }
}
</script>

<style scoped>
.pool-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pool-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 6px;
}

.pool-subtitle {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}


.filters-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}


.leads-section h3, .my-bids-section h3 {
  font-size: 16px;
  font-weight: 800;
  color: var(--primary-color);
  margin: 0 0 16px 0;
}

.leads-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.lead-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: var(--transition-normal);
}

.lead-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: var(--border-hover);
}

.lead-card.is-unlocked {
  border-color: var(--success-color);
  background: #f0fdf4;
}

.lead-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge-region {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary-color);
  background: var(--primary-light);
  padding: 6px 12px;
  border-radius: var(--radius-full);
}

.lead-status {
  font-size: 9px;
  font-weight: 800;
  text-transform: uppercase;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
}

.lead-status.POOL_CONSTRUCTION { background: var(--info-light); color: var(--info-color); }
.lead-status.POOL_REAL_ESTATE { background: var(--warning-light); color: var(--warning-color); }

.lead-details {
  background: var(--bg-color);
  border-radius: var(--radius-md);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid var(--border-color);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.detail-row .label {
  color: var(--text-muted);
  font-weight: 600;
}

.detail-row .value {
  color: var(--text-main);
  font-weight: 700;
}

.detail-row .budget-value {
  color: var(--success-color);
}

.gdpr-box {
  border-top: 1px dashed var(--border-color);
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gdpr-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.gdpr-item .label {
  color: var(--text-muted);
  font-weight: 600;
}

.gdpr-item .value.masked {
  color: var(--text-muted);
  font-style: italic;
  font-weight: 600;
}

.gdpr-item .value.link {
  color: var(--info-color);
  text-decoration: underline;
  font-weight: 700;
}

.btn-bid-primary {
  width: 100%;
  padding: 12px;
  border: none;
  background: var(--primary-color);
  color: white;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-normal);
  box-shadow: 0 4px 10px rgba(15, 41, 66, 0.15);
}

.btn-bid-primary:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(15, 41, 66, 0.25);
}

.btn-bid-primary:disabled {
  background: var(--border-color);
  color: var(--text-muted);
  cursor: not-allowed;
  box-shadow: none;
}


.pool-divider {
  border: 0;
  height: 1px;
  background: var(--border-color);
  margin: 32px 0;
}

.bids-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.bid-item-card {
  background: white;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  box-shadow: var(--shadow-sm);
}

.bid-item-card.ACCEPTED { border-left: 5px solid var(--success-color); }
.bid-item-card.REJECTED { border-left: 5px solid var(--danger-color); }
.bid-item-card.PENDING { border-left: 5px solid var(--warning-color); }

.bid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bid-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-color);
}

.bid-badge-status {
  font-size: 9px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
}

.bid-badge-status.ACCEPTED { background: var(--success-light); color: var(--success-color); }
.bid-badge-status.REJECTED { background: var(--danger-light); color: var(--danger-color); }
.bid-badge-status.PENDING { background: var(--warning-light); color: var(--warning-color); }

.bid-body p {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: var(--text-main);
  line-height: 1.5;
}

.btn-complete-renovation {
  background: var(--success-color);
  color: white;
  border: none;
  font-size: 11px;
  font-weight: 700;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition-fast);
}

.btn-complete-renovation:hover {
  background: #059669;
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  width: 90%;
  max-width: 500px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.modal-header {
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--primary-light);
}

.modal-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: var(--primary-color);
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
}

.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.vat-info {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
  display: block;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  border: none;
  background: var(--success-color);
  color: white;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-normal);
}

.btn-submit:hover {
  background: #059669;
}

.modal-error-msg {
  color: var(--danger-color);
  font-size: 12px;
  margin: 0;
  text-align: center;
  font-weight: 600;
}

.loading-state, .empty-state-placeholder, .empty-state-bids {
  text-align: center;
  padding: 40px;
  color: var(--text-muted);
  background: var(--bg-color);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--border-color);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.filters-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  border-top: 1px solid var(--border-color);
  padding-top: 16px;
}

.btn-search {
  padding: 10px 24px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-fast);
  box-shadow: 0 4px 10px rgba(15, 41, 66, 0.15);
}

.btn-search:hover {
  background: var(--primary-hover);
}

.btn-clear-filters {
  padding: 10px 20px;
  background: #ffffff;
  color: var(--text-muted);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-fast);
}

.btn-clear-filters:hover {
  background: var(--bg-color);
  border-color: var(--primary-color);
  color: var(--primary-color);
}


.pool-images-section {
  margin: 4px 0;
}

.pool-images-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.pool-image-wrapper {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-color);
}

.pool-image-label {
  position: absolute;
  top: 6px;
  left: 6px;
  background: rgba(15, 41, 66, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: 9px;
  font-weight: 800;
  text-transform: uppercase;
  z-index: 2;
}

.pool-apartment-image {
  width: 100%;
  height: 110px;
  object-fit: cover;
  display: block;
  transition: transform 0.3s;
}

.pool-image-wrapper:hover .pool-apartment-image {
  transform: scale(1.04);
}
</style>
