<template>
  <div class="bids-owner-container animate-fade-in">
    <div v-if="loading" class="loading-state">
      <div class="spinner-small"></div>
      <span>Φόρτωση προσφορών...</span>
    </div>

    <div v-else-if="bids.length === 0" class="empty-state-bids">
      <em>Δεν έχουν υποβληθεί προσφορές ακόμα για αυτό το ακίνητο.</em>
    </div>

    <div v-else class="bids-list">
      <div v-for="bid in bids" :key="bid.id" class="bid-card-owner" :class="bid.status">
        <div class="bid-header-owner">
          <div class="bid-meta">
            <span class="user-role-label">
              {{ bid.businessUser?.role === 'REAL_ESTATE_AGENCY' ? ' ΜΕΣΙΤΙΚΟ ΓΡΑΦΕΙΟ' : ' ΚΑΤΑΣΚΕΥΑΣΤΙΚΗ' }}
            </span>
            <h5>
              <router-link :to="'/profile/' + bid.businessUser?.id" class="profile-link">
                {{ bid.businessUser?.username }}
              </router-link>
            </h5>
          </div>
          <span class="badge-premium" :class="'badge-premium-' + getBadgeType(bid.status)">
            {{ getStatusText(bid.status) }}
          </span>
        </div>

        <div class="bid-details-owner">
          <div class="cost-strip">
            <div class="cost-item">
              <span class="cost-label">
                {{ bid.businessUser?.role === 'REAL_ESTATE_AGENCY' ? 'Προτεινόμενο Ενοίκιο:' : 'Οικονομική Προσφορά:' }}
              </span>
              <span class="cost-value">{{ bid.estimatedCost }} €</span>
              <span class="cost-subtext">
                {{ bid.businessUser?.role === 'REAL_ESTATE_AGENCY' ? '/ μήνα' : '(συμπ. ΦΠΑ 24%)' }}
              </span>
            </div>
          </div>
          <div class="details-text">
            <span class="notes-header">
              {{ bid.businessUser?.role === 'REAL_ESTATE_AGENCY' ? 'Προτεινόμενη Στρατηγική & Σημειώσεις' : 'Σημειώσεις & Χρονοδιάγραμμα' }}
            </span>
            <p>{{ bid.proposalDetails || 'Δεν υποβλήθηκαν επιπλέον λεπτομέρειες.' }}</p>
          </div>
        </div>

        <div v-if="bid.status === 'PENDING'" class="bid-actions-owner">
          <button @click="acceptBid(bid)" class="btn-premium btn-premium-primary btn-sm">
            <span>Έγκριση & Escrow</span>
          </button>
          <button @click="rejectBid(bid)" class="btn-premium btn-premium-danger-text btn-sm">
            <span>Απόρριψη</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BidsListForOwner',
  props: {
    projectId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      bids: [],
      loading: false
    }
  },
  methods: {
    getBadgeType(status) {
      if (status === 'ACCEPTED') return 'success';
      if (status === 'REJECTED') return 'danger';
      return 'pending';
    },
    getStatusText(status) {
      if (status === 'ACCEPTED') return 'ΕΓΚΡΙΘΗΚΕ';
      if (status === 'REJECTED') return 'ΑΠΟΡΡΙΦΘΗΚΕ';
      return 'ΕΚΚΡΕΜΕΙ';
    },
    fetchBids() {
      this.loading = true;
      fetch('/api/pool/bids')
        .then(res => {
          if (!res.ok) throw new Error();
          return res.json();
        })
        .then(data => {
          this.bids = data.filter(bid => bid.project?.id === this.projectId);
        })
        .catch(err => {
          console.error('Failed to load bids for project ' + this.projectId, err);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    acceptBid(bid) {
      const isRealEstate = bid.businessUser?.role === 'REAL_ESTATE_AGENCY';
      const msg = isRealEstate
        ? 'Θέλετε να εγκρίνετε αυτή την πρόταση ενοικίασης;'
        : 'Θέλετε να εγκρίνετε αυτή την προσφορά; Τα χρήματα θα δεσμευτούν στο Escrow Wallet.';
      if (!confirm(msg)) return;
      fetch(`/api/pool/bids/${bid.id}/accept`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.fetchBids();
        this.$emit('accepted');
      })
      .catch(() => {
        alert('Σφάλμα κατά την έγκριση της προσφοράς.');
      });
    },
    rejectBid(bid) {
      const isRealEstate = bid.businessUser?.role === 'REAL_ESTATE_AGENCY';
      const msg = isRealEstate
        ? 'Θέλετε να απορρίψετε αυτή την πρόταση;'
        : 'Θέλετε να απορρίψετε αυτή την προσφορά;';
      if (!confirm(msg)) return;
      fetch(`/api/pool/bids/${bid.id}/reject`, {
        method: 'POST'
      })
      .then(res => {
        if (!res.ok) throw new Error();
        this.fetchBids();
      })
      .catch(() => {
        alert('Σφάλμα κατά την απόρριψη της προσφοράς.');
      });
    }
  },
  watch: {
    projectId: {
      immediate: true,
      handler() {
        this.fetchBids();
      }
    }
  }
}
</script>

<style scoped>
.bids-owner-container {
  background: var(--primary-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.bids-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bid-card-owner {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: var(--transition-normal);
}

.bid-card-owner:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-hover);
}

.bid-card-owner.ACCEPTED {
  border-left: 5px solid var(--success-color);
}
.bid-card-owner.REJECTED {
  border-left: 5px solid var(--danger-color);
}
.bid-card-owner.PENDING {
  border-left: 5px solid var(--warning-color);
}

.bid-header-owner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.bid-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-role-label {
  font-size: 10px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}

.bid-header-owner h5 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--primary-color);
}

.profile-link {
  color: var(--primary-color);
  text-decoration: none;
  border-bottom: 2px solid transparent;
  transition: var(--transition-fast);
}

.profile-link:hover {
  color: var(--info-color);
  border-bottom-color: var(--info-color);
}

.bid-details-owner {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cost-strip {
  background: var(--primary-light);
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
}

.cost-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-wrap: wrap;
}

.cost-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
}

.cost-value {
  font-size: 20px;
  font-weight: 800;
  color: var(--success-color);
  font-family: var(--font-title);
}

.cost-subtext {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
}

.details-text {
  background: #f8fafc;
  border: 1px solid var(--border-color);
  padding: 14px;
  border-radius: var(--radius-md);
}

.notes-header {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.details-text p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-main);
  white-space: pre-wrap;
}

.bid-actions-owner {
  display: flex;
  gap: 12px;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 16px;
}

.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
}

.btn-premium-danger-text {
  background: transparent;
  color: var(--danger-color);
  border: 1px solid var(--border-color);
}

.btn-premium-danger-text:hover {
  background: var(--danger-light);
  border-color: var(--danger-color);
  transform: translateY(-2px);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  font-size: 14px;
  color: var(--text-muted);
}

.empty-state-bids {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  text-align: center;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 32px;
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
