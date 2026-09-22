<template>
  <div class="apartment-list">

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Φόρτωση ακινήτων...</p>
    </div>


    <div v-else-if="apartments.length > 0" class="list-container">
      <div
        v-for="apt in apartments"
        :key="apt.id"
        class="apartment-card animate-fade-in"
        @click="selectApartment(apt.id)"
      >
        <div class="card-content">
          <div class="card-header">
            <span class="apt-id">#{{ apt.id }}</span>
            <span class="apt-area">{{ apt.area }}</span>
          </div>
          <div class="card-footer">
            <span class="apt-tag"> {{ apt.squareMeters }} τ.μ.</span>
            <span class="apt-badge" :class="(apt.status || 'DRAFT').toUpperCase()">
              {{ getStatusLabel(apt.status || 'DRAFT') }}
            </span>
          </div>
        </div>
      </div>
    </div>


    <div v-else class="empty-state">
      <p>Δεν βρέθηκαν διαμερίσματα</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ApartmentList',
  props: {
    apartments: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    selectApartment(id) {
      this.$emit('select', id)
    },
    getStatusLabel(status) {
      switch(status.toUpperCase()) {
        case 'DRAFT': return 'Draft';
        case 'VISUALIZED': return 'Visualized';
        case 'POOL_CONSTRUCTION': return 'Pool Κατασκευής';
        case 'CONTRACT_LOCKED': return 'Σε εξέλιξη';
        case 'COMPLETED': return 'Ολοκληρώθηκε';
        case 'POOL_REAL_ESTATE': return 'Pool Μεσιτών';
        default: return status;
      }
    }
  }
}
</script>

<style scoped>
.apartment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.apartment-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.apartment-card:hover {
  border-color: var(--primary-color);
  transform: translateX(4px);
  box-shadow: var(--shadow-md);
  background: var(--primary-light);
}

.card-icon {
  font-size: 20px;
  background: var(--bg-color);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: var(--transition-normal);
  color: var(--primary-color);
}

.apartment-card:hover .card-icon {
  background: #ffffff;
  transform: scale(1.05);
  box-shadow: var(--shadow-sm);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.apt-id {
  font-size: 10px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}

.apt-area {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-color);
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.apt-tag {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}

.apt-badge {
  font-size: 9px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}


.apt-badge.DRAFT { background: #e2e8f0; color: #475569; }
.apt-badge.VISUALIZED { background: var(--info-light); color: var(--info-color); }
.apt-badge.POOL_CONSTRUCTION { background: var(--warning-light); color: var(--warning-color); }
.apt-badge.CONTRACT_LOCKED { background: var(--danger-light); color: var(--danger-color); }
.apt-badge.COMPLETED { background: var(--success-light); color: var(--success-color); }
.apt-badge.POOL_REAL_ESTATE { background: #f3e8ff; color: #6b21a8; }

.card-arrow {
  font-size: 16px;
  color: #cbd5e1;
  transition: var(--transition-fast);
}

.apartment-card:hover .card-arrow {
  color: var(--primary-color);
  transform: translateX(3px);
}

.loading-state, .empty-state {
  text-align: center;
  padding: 30px 16px;
  color: var(--text-muted);
  background: var(--bg-color);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--border-color);
}

.empty-icon {
  font-size: 28px;
  margin-bottom: 10px;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
