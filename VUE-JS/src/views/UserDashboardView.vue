<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-brand">
        <span class="brand-text">Estate<span class="highlight">Sol</span></span>
        <span class="brand-separator">|</span>
        <span class="brand-subtext">{{ auth.role === 'ADMIN' ? 'Διαχείριση' : 'Πίνακας Ελέγχου' }}</span>
      </div>

      <div class="nav-user">
        <span class="welcome-text">Γεια σου, <strong>{{ auth.user?.username || 'User' }}</strong></span>
        <span class="role-badge" :class="auth.role">{{ getRoleText() }}</span>
        <router-link v-if="auth.role === 'CONSTRUCTION_COMPANY' || auth.role === 'REAL_ESTATE_AGENCY'" :to="'/profile/' + auth.user?.id" class="btn-profile-nav">
          Το Προφίλ μου
        </router-link>
        <button @click="logout" class="btn-logout">Έξοδος </button>
      </div>
    </nav>

    <div class="main-content">
      <template v-if="auth.role === 'AMATEUR_USER'">

        <div class="dashboard-tabs">
          <button @click="subMode = 'my-properties'; mode = 'list'" :class="{ active: subMode === 'my-properties' }" class="tab-item">
             Τα Ακίνητά μου
          </button>
          <button @click="subMode = 'professionals'; fetchProfessionals()" :class="{ active: subMode === 'professionals' }" class="tab-item">
             Αναζήτηση Επαγγελματιών
          </button>
        </div>

        <template v-if="subMode === 'my-properties'">

          <div class="toolbar">
            <h2>Τα Ακίνητά μου</h2>
            <button @click="showCreate" class="btn-create-main">
              <span>+</span> Νέο Διαμέρισμα
            </button>
          </div>


          <div class="layout">

            <div class="left-panel">
              <div class="panel-header">
                <h3>ΛΙΣΤΑ ΑΚΙΝΗΤΩΝ</h3>
              </div>

              <div class="pool-filter-tabs">
                <button @click="aptFilter = 'ALL'" :class="{ active: aptFilter === 'ALL' }" class="filter-tab-btn">Όλα</button>
                <button @click="aptFilter = 'POOL_CONSTRUCTION'" :class="{ active: aptFilter === 'POOL_CONSTRUCTION' }" class="filter-tab-btn"> Κατασκευών</button>
                <button @click="aptFilter = 'POOL_REAL_ESTATE'" :class="{ active: aptFilter === 'POOL_REAL_ESTATE' }" class="filter-tab-btn"> Μεσιτών</button>
                <button @click="aptFilter = 'OTHER'" :class="{ active: aptFilter === 'OTHER' }" class="filter-tab-btn"> Λοιπά</button>
              </div>
              <div class="list-scroll-area">
                <ApartmentList
                  :apartments="filteredOwnerApartments"
                  :loading="loading"
                  @select="openDetails"
                />
              </div>
            </div>


            <div class="right-panel">

              <div v-if="mode === 'list'" class="empty-detail-state">
                <p>Επιλέξτε ένα διαμέρισμα από τη λίστα για να προβάλλετε τις λεπτομέρειες και τις AI εκτιμήσεις.</p>
              </div>


              <div v-else-if="mode === 'create'" class="form-wrapper animate-fade-in">
                <ApartmentCreateForm @created="onCreated" />
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
                        <button v-if="selectedApartment.status === 'COMPLETED' || selectedApartment.status === 'POOL_REAL_ESTATE' || (selectedApartment.status === 'CONTRACT_LOCKED' && acceptedBidRole === 'REAL_ESTATE_AGENCY')" @click="resetRenovation(selectedApartment.id)" class="btn-action btn-reset-custom"> Νέα Ανακαίνιση</button>
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


                    <div v-if="selectedApartment && (selectedApartment.status === 'POOL_CONSTRUCTION' || selectedApartment.status === 'POOL_REAL_ESTATE' || selectedApartment.status === 'CONTRACT_LOCKED')" class="bids-section">
                      <div class="section-title">
                        <h3> {{ selectedApartment.status === 'POOL_REAL_ESTATE' ? 'Προτάσεις Μεσιτικών Γραφείων' : 'Προσφορές Κατασκευαστικών Εταιρειών' }}</h3>
                      </div>
                      <BidsListForOwner :projectId="selectedApartment.id" @accepted="openDetails(selectedApartment.id)" />
                    </div>


                    <div v-if="selectedApartment && selectedApartment.status === 'CONTRACT_LOCKED' && acceptedBidRole === 'CONSTRUCTION_COMPANY'" class="construction-locked-info">
                      <h4> Οι εργασίες βρίσκονται σε εξέλιξη</h4>
                      <p>Εάν οι εργασίες ανακαίνισης έχουν ολοκληρωθεί, μπορείτε να επισημάνετε το έργο ως ολοκληρωμένο για να μεταβεί στη φάση προώθησης σε μεσιτικά γραφεία.</p>
                      <button @click="completeRenovation(selectedApartment.id)" class="btn-complete-project">
                        Σήμανση ως Ολοκληρωμένο
                      </button>
                    </div>


                    <div v-if="selectedApartment && selectedApartment.status === 'CONTRACT_LOCKED' && acceptedBidRole === 'REAL_ESTATE_AGENCY'" class="construction-locked-info real-estate-locked">
                      <h4> Ενεργή Συνεργασία με Μεσιτικό Γραφείο</h4>
                      <p>Το ακίνητό σας έχει ανατεθεί επιτυχώς στο μεσιτικό γραφείο <strong>{{ acceptedBidUser }}</strong> για ενοικίαση / πώληση.</p>
                    </div>


                    <div v-if="selectedApartment && (selectedApartment.status === 'VISUALIZED' || selectedApartment.status === 'DRAFT' || selectedApartment.status === 'COMPLETED')" class="publish-pool-card">
                      <template v-if="selectedApartment.status === 'COMPLETED'">
                        <h4> Προώθηση στο Pool Μεσιτικών</h4>
                        <p>Η ανακαίνιση ολοκληρώθηκε! Δημοσιεύστε το ακίνητό σας στο Pool Μεσιτών ώστε να λάβετε προτάσεις ενοικίασης / πώλησης.</p>
                        <button @click="publishToPool(selectedApartment.id, 'REAL_ESTATE')" class="btn-publish-pool">
                          Δημοσίευση στο Pool Μεσιτικών
                        </button>
                      </template>
                      <template v-else>
                        <h4> Δημοσίευση στο Pool</h4>
                        <p>Επιλέξτε πώς επιθυμείτε να προωθήσετε το ακίνητό σας:</p>
                        <div class="publish-options-buttons">
                          <button @click="publishToPool(selectedApartment.id, 'CONSTRUCTION')" class="btn-publish-pool construction-btn">
                             Ζήτηση Ανακαίνισης (Pool Κατασκευαστικών)
                          </button>
                          <button @click="publishToPool(selectedApartment.id, 'REAL_ESTATE')" class="btn-publish-pool realestate-btn">
                             Εύρεση Μεσιτικού (Χωρίς Ανακαίνιση)
                          </button>
                        </div>
                      </template>
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
                        <input type="number" v-model.number="editForm.estimatedRent" class="form-input-premium" disabled />
                        <small class="help-text">Υπολογίζεται αυτόματα από την AI</small>
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
        </template>

        <template v-else-if="subMode === 'professionals'">
          <div class="professionals-directory animate-fade-in">
            <div class="directory-header card-premium">
              <div class="directory-title">
                <h2> Αναζήτηση Επαγγελματιών</h2>
                <p>Βρείτε πιστοποιημένα μεσιτικά γραφεία και κατασκευαστικές εταιρείες σε όλη την Ελλάδα</p>
              </div>


              <div class="directory-filters">
                <div class="filter-search-wrapper">
                  <input
                    type="text"
                    v-model="profSearchQuery"
                    placeholder="Αναζήτηση με όνομα..."
                    class="form-input-premium prof-search-input"
                  />
                </div>

                <div class="filters-row">
                  <div class="filter-group">
                    <label class="input-label-custom">Κατηγορία</label>
                    <select v-model="profRoleFilter" class="form-input-premium select-premium select-sm">
                      <option value="ALL">Όλες οι Κατηγορίες</option>
                      <option value="CONSTRUCTION_COMPANY"> Κατασκευαστικές Εταιρείες</option>
                      <option value="REAL_ESTATE_AGENCY"> Μεσιτικά Γραφεία</option>
                    </select>
                  </div>

                  <div class="filter-group">
                    <label class="input-label-custom">Περιφέρεια</label>
                    <input
                      type="text"
                      v-model="profRegionFilter"
                      placeholder="π.χ. Αττική"
                      class="form-input-premium input-sm"
                    />
                  </div>
                </div>
              </div>
            </div>


            <div v-if="profLoading" class="loading-state card-premium">
              <div class="spinner-large"></div>
              <p>Φόρτωση επαγγελματιών...</p>
            </div>

            <div v-else-if="filteredProfessionals.length === 0" class="empty-state card-premium">
              <p>Δεν βρέθηκαν επαγγελματίες με τα συγκεκριμένα κριτήρια.</p>
            </div>

            <div v-else class="professionals-grid">
              <div v-for="prof in filteredProfessionals" :key="prof.id" class="card-premium professional-card">
                <div class="prof-header">
                  <div class="avatar-circle-sm">
                    {{ prof.username.substring(0, 2).toUpperCase() }}
                  </div>
                  <div class="prof-title-group">
                    <h4>{{ prof.username }}</h4>
                    <span class="role-badge" :class="prof.role">
                      {{ prof.role === 'REAL_ESTATE_AGENCY' ? ' ΜΕΣΙΤΙΚΟ' : ' ΚΑΤΑΣΚΕΥΑΣΤΙΚΗ' }}
                    </span>
                  </div>
                </div>

                <div class="prof-body">
                  <div class="rating-strip-sm">
                    <span class="avg-val-sm">{{ prof.averageRating > 0 ? prof.averageRating.toFixed(1) : 'N/A' }}</span>
                    <span class="review-count-sm">({{ prof.reviewCount }} αξιολογήσεις)</span>
                  </div>

                  <div class="prof-info-row">
                    <span class="info-text">{{ prof.region || 'Όλη την Ελλάδα' }}</span>
                  </div>

                  <div class="prof-info-row" v-if="prof.afm">
                    <span class="info-text">ΑΦΜ: {{ prof.afm }}</span>
                  </div>
                </div>

                <div class="prof-footer">
                  <span class="badge-premium badge-premium-success" v-if="prof.isVerified">
                     Verified
                  </span>
                  <span class="badge-premium badge-premium-danger" v-else>
                     Pending
                  </span>
                  <router-link :to="'/profile/' + prof.id" class="btn-premium btn-premium-primary btn-sm">
                    Προβολή Προφίλ
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>


      <template v-else-if="auth.role === 'CONSTRUCTION_COMPANY' || auth.role === 'REAL_ESTATE_AGENCY'">
        <div class="business-workspace animate-fade-in">
          <ConstructionPool />
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import ApartmentList from '@/components/ApartmentList.vue'
import ApartmentCreateForm from '@/components/ApartmentCreateForm.vue'
import ConstructionPool from '@/components/ConstructionPool.vue'
import BidsListForOwner from '@/components/BidsListForOwner.vue'
import { auth } from '@/stores/auth'

export default {
  name: 'UserDashboardView',
  components: {
    ApartmentList,
    ApartmentCreateForm,
    ConstructionPool,
    BidsListForOwner
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
      subMode: 'my-properties',
      aptFilter: 'ALL',
      profSearchQuery: '',
      profRoleFilter: 'ALL',
      profRegionFilter: '',
      professionals: [],
      profLoading: false,
      acceptedBidRole: null,
      acceptedBidUser: null
    }
  },
  methods: {
    getRoleText() {
      if (this.auth.role === 'CONSTRUCTION_COMPANY') return 'ΤΕΧΝΙΚΗ ΕΤΑΙΡΕΙΑ'
      if (this.auth.role === 'REAL_ESTATE_AGENCY') return 'ΜΕΣΙΤΙΚΟ ΓΡΑΦΕΙΟ'
      return 'ΙΔΙΩΤΗΣ'
    },
    showCreate() {
      this.mode = 'create'
    },
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
      this.acceptedBidRole = null
      this.acceptedBidUser = null

      fetch(`/api/apartments/${id}`)
        .then(res => res.json())
        .then(data => {
          this.selectedApartment = data
          this.detailsLoading = false
          if (data && !data.aiProcessed && !data.aiProcessingError) {
            this.startDetailsPolling(id)
          }
          return fetch('/api/pool/bids')
        })
        .then(res => {
          if (res) return res.json();
        })
        .then(bids => {
          if (bids) {
            const acceptedBid = bids.find(b => b.project?.id === id && b.status === 'ACCEPTED');
            if (acceptedBid) {
              this.acceptedBidRole = acceptedBid.businessUser?.role;
              this.acceptedBidUser = acceptedBid.businessUser?.username;
            }
          }
        })
        .catch(err => {
          console.error('Failed to load apartment details or bids', err)
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
        estimatedCost: this.editForm.estimatedCost
      }
      fetch(`/api/apartments/updateasuser/${id}`, {
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
    publishToPool(id, target) {
      const confirmMsg = target === 'REAL_ESTATE'
        ? 'Θέλετε να δημοσιεύσετε το ακίνητό σας απευθείας στο Pool Μεσιτικών (χωρίς ανακαίνιση);'
        : 'Θέλετε να δημοσιεύσετε το έργο σας στο Pool Κατασκευαστικών για ανακαίνιση;';
      if (!confirm(confirmMsg)) return;
      const url = target ? `/api/pool/publish/${id}?target=${target}` : `/api/pool/publish/${id}`;
      fetch(url, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.openDetails(id);
        this.reloadApartments();
      })
      .catch(() => {
        alert('Σφάλμα κατά τη δημοσίευση στο Pool.');
      });
    },
    completeRenovation(id) {
      if (!confirm('Θέλετε να δηλώσετε την ολοκλήρωση των εργασιών ανακαίνισης;')) return;
      fetch(`/api/apartments/${id}/complete-renovation`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.openDetails(id);
        this.reloadApartments();
      })
      .catch(() => {
        alert('Σφάλμα κατά την ενημέρωση του έργου.');
      });
    },
    deleteApartment(id) {
      if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το ακίνητο; Όλες οι προσφορές θα διαγραφούν επίσης.')) return;
      fetch(`/api/apartments/deleteasuser/${id}`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.selectedApartment = null;
        this.mode = 'list';
        this.reloadApartments();
      })
      .catch(() => {
        alert('Σφάλμα κατά τη διαγραφή του ακινήτου.');
      });
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
    },
    fetchProfessionals() {
      this.subMode = 'professionals';
      this.profLoading = true;
      fetch('/api/business-profile/list')
        .then(res => {
          if (!res.ok) throw new Error();
          return res.json();
        })
        .then(data => {
          this.professionals = data;
          this.profLoading = false;
        })
        .catch(err => {
          console.error('Failed to load professionals:', err);
          this.profLoading = false;
        });
    },
    resetRenovation(id) {
      if (!confirm('Είστε σίγουροι ότι θέλετε να ξαναδηλώσετε ανακαίνιση για αυτό το ακίνητο; Όλες οι τρέχουσες προσφορές και συμβόλαια θα διαγραφούν.')) return;
      fetch(`/api/apartments/${id}/reset-renovation`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.openDetails(id);
        this.reloadApartments();
      })
      .catch(() => {
        alert('Σφάλμα κατά την επαναφορά ανακαίνισης.');
      });
    }
  },
  computed: {
    filteredOwnerApartments() {
      if (!this.aptFilter || this.aptFilter === 'ALL') return this.apartments;
      if (this.aptFilter === 'POOL_CONSTRUCTION') {
        return this.apartments.filter(a => a.status === 'POOL_CONSTRUCTION');
      }
      if (this.aptFilter === 'POOL_REAL_ESTATE') {
        return this.apartments.filter(a => a.status === 'POOL_REAL_ESTATE');
      }
      if (this.aptFilter === 'OTHER') {
        return this.apartments.filter(a => a.status !== 'POOL_CONSTRUCTION' && a.status !== 'POOL_REAL_ESTATE');
      }
      return this.apartments;
    },
    filteredProfessionals() {
      let result = this.professionals;
      if (this.profRoleFilter && this.profRoleFilter !== 'ALL') {
        result = result.filter(p => p.role === this.profRoleFilter);
      }
      if (this.profSearchQuery) {
        const query = this.profSearchQuery.toLowerCase();
        result = result.filter(p => p.username && p.username.toLowerCase().includes(query));
      }
      if (this.profRegionFilter) {
        const query = this.profRegionFilter.toLowerCase();
        result = result.filter(p => p.region && p.region.toLowerCase().includes(query));
      }
      return result;
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

.btn-profile-nav {
  padding: 8px 14px;
  background: var(--accent-color);
  border: none;
  border-radius: 8px;
  color: var(--primary-color) !important;
  cursor: pointer;
  font-weight: 700;
  font-size: 12px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: var(--transition-fast);
}

.btn-profile-nav:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.25);
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.toolbar h2 {
  font-size: 22px;
  color: var(--primary-color);
}

.btn-create-main {
  background: var(--primary-color);
  color: #ffffff;
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: var(--transition-normal);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-create-main:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
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


.bids-section {
  margin-bottom: 28px;
}

.section-title {
  margin-bottom: 14px;
}

.section-title h3 {
  font-size: 15px;
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


.publish-pool-card, .construction-locked-info {
  background: var(--primary-light);
  border: 1px solid #d0e0f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 28px;
}

.publish-pool-card h4, .construction-locked-info h4 {
  font-size: 14px;
  margin-bottom: 6px;
}

.publish-pool-card p, .construction-locked-info p {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
  margin: 0 0 14px 0;
}

.btn-publish-pool, .btn-complete-project {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition-fast);
}

.btn-publish-pool:hover, .btn-complete-project:hover {
  background: var(--primary-hover);
}

.construction-locked-info {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.construction-locked-info h4 {
  color: #166534;
}

.construction-locked-info.real-estate-locked {
  background: #f3e8ff;
  border-color: #e9d5ff;
}

.construction-locked-info.real-estate-locked h4 {
  color: #6b21a8;
}

.btn-complete-project {
  background: #10b981;
}

.btn-complete-project:hover {
  background: #059669;
}

.publish-options-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.btn-publish-pool.construction-btn {
  background: var(--primary-color);
  color: white;
}

.btn-publish-pool.construction-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.btn-publish-pool.realestate-btn {
  background: var(--accent-color);
  color: var(--primary-color);
}

.btn-publish-pool.realestate-btn:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.25);
}


.details-edit-form .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.help-text {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
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

.business-workspace {
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 30px;
  box-shadow: var(--shadow-md);
}

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


.dashboard-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 12px;
}

.tab-item {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-muted);
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: var(--transition-normal);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-item:hover {
  background: var(--primary-light);
  color: var(--primary-color);
}

.tab-item.active {
  background: var(--primary-color);
  color: #ffffff;
}


.professionals-directory {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.directory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.directory-title h2 {
  font-size: 24px;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.directory-title p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.directory-filters {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-search-wrapper {
  position: relative;
  min-width: 250px;
}

.prof-search-input {
  padding-left: 40px !important;
}

.filters-row {
  display: flex;
  gap: 12px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.select-sm {
  padding: 8px 12px !important;
  font-size: 13px !important;
}

.input-sm {
  padding: 8px 12px !important;
  font-size: 13px !important;
}


.professionals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.professional-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: var(--transition-normal);
}

.prof-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-circle-sm {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  font-family: var(--font-title);
  border: 2px solid var(--accent-color);
}

.prof-title-group h4 {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 2px 0;
}

.prof-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rating-strip-sm {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--accent-light);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 209, 0, 0.2);
  align-self: flex-start;
}

.star-rating-sm {
  color: var(--accent-color);
  font-size: 16px;
  line-height: 1;
}

.avg-val-sm {
  font-size: 14px;
  font-weight: 800;
  color: var(--primary-color);
}

.review-count-sm {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
}

.prof-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-main);
}

.info-icon {
  font-size: 14px;
}

.info-text {
  font-weight: 500;
}

.prof-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  margin-top: auto;
}

.btn-reset-custom:hover {
  background: #eff6ff;
  border-color: var(--info-color);
  transform: translateY(-1px);
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
