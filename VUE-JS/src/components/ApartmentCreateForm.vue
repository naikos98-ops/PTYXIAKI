<template>
  <div class="create-form-container">
    <div class="form-header">
      <h3>Νέα Καταχώρηση Ακινήτου</h3>
      <p class="form-subtitle">Συμπληρώστε τα τεχνικά χαρακτηριστικά για την AI μελέτη βιωσιμότητας</p>
    </div>

    <div class="form-body animate-fade-in">
      <div class="form-grid">
        <div class="form-group">
          <label>Περιοχή / Συνοικία</label>
          <input type="text" v-model="area" placeholder="π.χ. Κολωνάκι, Αθήνα" class="form-input" />
        </div>

        <div class="form-group">
          <label>Δήμος / Περιφέρεια</label>
          <input type="text" v-model="region" placeholder="π.χ. Δήμος Αθηναίων" class="form-input" />
        </div>

        <div class="form-group">
          <label>Google Maps Pin (Link)</label>
          <input type="text" v-model="googleMapsPin" placeholder="https://maps.google.com/..." class="form-input" />
        </div>

        <div class="form-group">
          <label>Τετραγωνικά Μέτρα (τ.μ.)</label>
          <input type="number" v-model="squareMeters" placeholder="0" class="form-input" />
        </div>

        <div class="form-group">
          <label>Όροφος</label>
          <input type="number" v-model="floor" placeholder="0" class="form-input" />
        </div>

        <div class="form-group full-width">
          <label>Διαθέσιμο Budget Ανακαίνισης (€)</label>
          <div class="budget-input-wrapper">
            <input
              type="number"
              v-model="budget"
              placeholder="Εισάγετε το budget σας σε ευρώ"
              :disabled="noBudget"
              class="form-input budget-input"
            />
            <label class="checkbox-container-budget">
              <input type="checkbox" v-model="noBudget" @change="onNoBudgetChange">
              <span class="checkmark"></span>
              Δεν έχω συγκεκριμένο budget (Επιθυμώ το ελάχιστο κόστος για κατοίκηση)
            </label>
          </div>
        </div>
      </div>

      <div class="form-group full-width" style="margin-bottom: 24px;">
        <label>Φωτογραφία Ακινήτου (Πριν την ανακαίνιση)</label>
        <div class="file-upload-wrapper">
          <input type="file" id="apt-image" @change="onFileChange" class="file-input" />
          <label for="apt-image" class="file-label">
            <span class="file-text">{{ image ? image.name : 'Επιλέξτε αρχείο φωτογραφίας (JPG, PNG)...' }}</span>
          </label>
        </div>
      </div>

      <div v-if="squareMeters > 0" class="feasibility-widget-container animate-slide-down">
        <FeasibilityWidget :squareMeters="Number(squareMeters)" :budget="noBudget ? null : (budget ? Number(budget) : null)" />
      </div>

      <div class="form-actions">
        <button @click="createApartment" class="btn-create" :disabled="isProcessing">
          <span v-if="isProcessing" class="spinner-small"></span>
          <span v-else>+ Δημιουργία & AI Επεξεργασία</span>
        </button>
      </div>

      <div v-if="status" :class="['status-msg', status.startsWith('Σφάλμα:') ? 'error' : 'success', isProcessing ? 'loading' : '']">
        {{ status }}
      </div>
    </div>
  </div>
</template>

<script>
import FeasibilityWidget from '@/components/FeasibilityWidget.vue';

export default {
  name: 'ApartmentCreateForm',
  components: {
    FeasibilityWidget
  },
  data() {
    return {
      area: '',
      squareMeters: '',
      floor: '',
      googleMapsPin: '',
      region: '',
      budget: '',
      noBudget: false,
      image: null,
      status: null,
      isProcessing: false,
    }
  },
  methods: {
    onFileChange(event) {
      this.image = event.target.files[0]
    },
    onNoBudgetChange() {
      if (this.noBudget) {
        this.budget = '';
      }
    },
    async createApartment() {
      if (!this.area || !this.squareMeters || !this.image) {
        this.status = 'Σφάλμα: Συμπληρώστε όλα τα υποχρεωτικά πεδία (Περιοχή, Τετραγωνικά, Φωτογραφία).'
        return
      }

      this.isProcessing = true;
      this.status = 'Η AI επεξεργάζεται τα στοιχεία και παράγει visual rendering...';

      try {
        const formData = new FormData()
        formData.append('area', this.area)
        formData.append('squareMeters', this.squareMeters)
        formData.append('floor', this.floor)
        formData.append('image', this.image)
        formData.append('googleMapsPin', this.googleMapsPin)
        formData.append('region', this.region)
        if (!this.noBudget && this.budget !== '') {
          formData.append('budget', this.budget)
        }

        const res = await fetch('/api/apartments/createApartment', {
          method: 'POST',
          body: formData
        });

        if (!res.ok) throw new Error('Σφάλμα διακομιστή: ' + res.status);

        const data = await res.json();
        this.status = 'Το ακίνητο καταχωρήθηκε επιτυχώς!';
        this.$emit('created', data.id);

      } catch (err) {
        console.error(err);
        this.status = 'Σφάλμα: Αποτυχία κατά την καταχώρηση: ' + err.message;
      } finally {
        this.isProcessing = false;
      }
    }
  }
}
</script>

<style scoped>
.create-form-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.form-subtitle {
  color: var(--text-muted);
  font-size: 13px;
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary-color);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-input {
  width: 100%;
  padding: 11px 14px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 14px;
  transition: var(--transition-normal);
  background: #f8fafc;
  color: var(--text-main);
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(15, 41, 66, 0.08);
}

.budget-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.checkbox-container-budget {
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 28px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  user-select: none;
  margin-top: 4px;
}

.checkbox-container-budget input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkbox-container-budget .checkmark {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  height: 18px;
  width: 18px;
  background-color: var(--border-color);
  border-radius: 5px;
  transition: var(--transition-fast);
}

.checkbox-container-budget:hover input ~ .checkmark {
  background-color: var(--border-hover);
}

.checkbox-container-budget input:checked ~ .checkmark {
  background-color: var(--primary-color);
}

.checkbox-container-budget .checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox-container-budget input:checked ~ .checkmark:after {
  display: block;
}

.checkbox-container-budget .checkmark:after {
  left: 6px;
  top: 3px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.file-upload-wrapper {
  position: relative;
}

.file-input {
  position: absolute;
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
}

.file-label {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f8fafc;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition-normal);
}

.file-label:hover {
  background: var(--primary-light);
  border-color: var(--primary-color);
}

.file-icon {
  font-size: 20px;
}

.file-text {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 600;
}

.form-actions {
  margin-top: 16px;
}

.btn-create {
  width: 100%;
  padding: 14px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(15, 41, 66, 0.15);
}

.btn-create:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(15, 41, 66, 0.25);
}

.btn-create:disabled {
  background: var(--border-color);
  color: var(--text-muted);
  cursor: not-allowed;
  box-shadow: none;
}

.status-msg {
  margin-top: 16px;
  padding: 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 700;
  text-align: center;
  border: 1px solid transparent;
}

.status-msg.error {
  background: var(--danger-light);
  color: var(--danger-color);
  border-color: #fecaca;
}

.status-msg.success {
  background: var(--success-light);
  color: var(--success-color);
  border-color: #bbf7d0;
}

.status-msg.loading {
  background: var(--info-light);
  color: var(--info-color);
  border-color: #bfdbfe;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.feasibility-widget-container {
  margin: 10px 0;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
