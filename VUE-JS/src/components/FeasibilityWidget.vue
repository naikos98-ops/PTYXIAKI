<template>
  <div class="feasibility-card">
    <div class="card-header">
      <h4>Μελέτη Βιωσιμότητας (Real-time AI)</h4>
    </div>


    <div v-if="budget === null" class="basic-mode-info animate-fade">
      <div class="info-content">
        <h5 class="info-title">Basic Habitability Mode</h5>
        <p class="info-desc">Ενεργοποιήθηκε η αυτόματη εκτίμηση για τις απολύτως απαραίτητες εργασίες κατοίκησης (μερεμέτια, βασικά δάπεδα, βάψιμο).</p>
        <div class="cost-estimate">
          <span class="cost-label">Εκτιμώμενο Baseline Κόστος:</span>
          <span class="cost-value">{{ squareMeters * 180 }} €</span>
        </div>
      </div>
    </div>


    <div v-else class="budget-mode-info animate-fade">
      <div class="metric-row">
        <div class="metric-item">
          <span class="metric-label">Συνολικό Budget</span>
          <span class="metric-value">{{ budget }} €</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">Κόστος ανά τ.μ.</span>
          <span class="metric-value" :class="statusClass">{{ costPerSqm.toFixed(0) }} €/τ.μ.</span>
        </div>
      </div>


      <div class="progress-bar-container">
        <div class="progress-bar">
          <div class="segment unfeasible" title="Unfeasible (<200€)"></div>
          <div class="segment cosmetic" title="Cosmetic (200€-450€)"></div>
          <div class="segment optimal" title="Optimal (450€-800€)"></div>
          <div class="segment premium" title="Premium (>=800€)"></div>


          <div class="needle" :style="{ left: needlePosition + '%' }" :class="statusClass">
            <span class="needle-label">{{ statusLabel }}</span>
          </div>
        </div>
      </div>


      <div class="explanation-box" :class="statusClass">
        <div class="explanation-icon">{{ explanationIcon }}</div>
        <div class="explanation-text">
          <strong>{{ statusHeader }}</strong>
          <p>{{ statusDescription }}</p>
        </div>
      </div>


      <div v-if="upsellRecommendation" class="upsell-box animate-slide-down">
        <div class="upsell-header">
          <h5>AI Πρόταση Αναβάθμισης Budget</h5>
        </div>
        <div class="upsell-body">
          <p class="upsell-intro">
            Αυξάνοντας το budget σας κατά <strong>{{ upsellRecommendation.extraBudget }} €</strong> (συνολικό budget: <strong>{{ upsellRecommendation.targetBudget }} €</strong>), μεταβαίνετε στην κατηγορία <strong>{{ upsellRecommendation.targetName }}</strong>.
          </p>

          <div class="benefits-grid">
            <div class="benefit-item">
              <div class="benefit-details">
                <span class="benefit-label">Επιπλέον Έσοδα</span>
                <span class="benefit-value">+{{ upsellRecommendation.extraRent }} € / μήνα</span>
              </div>
            </div>
            <div class="benefit-item">
              <div class="benefit-details">
                <span class="benefit-label">Απόσβεση Αναβάθμισης</span>
                <span class="benefit-value">~{{ upsellRecommendation.paybackMonths }} μήνες ({{ upsellRecommendation.paybackYears }} έτη)</span>
              </div>
            </div>
          </div>

          <div class="benefits-footer">
            <p class="upsell-benefits">
              <strong>Πλεονεκτήματα:</strong> {{ upsellRecommendation.benefitsMsg }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FeasibilityWidget',
  props: {
    squareMeters: {
      type: Number,
      required: true
    },
    budget: {
      type: Number,
      default: null
    }
  },
  computed: {
    costPerSqm() {
      if (!this.squareMeters || this.squareMeters <= 0) return 0;
      return (this.budget || 0) / this.squareMeters;
    },
    needlePosition() {
      const val = this.costPerSqm;
      if (val <= 0) return 0;
      let percentage = (val / 1000) * 100;
      if (percentage > 96) percentage = 96;
      if (percentage < 2) percentage = 2;
      return percentage;
    },
    status() {
      const val = this.costPerSqm;
      if (val < 200) return 'unfeasible';
      if (val >= 200 && val < 450) return 'cosmetic';
      if (val >= 450 && val < 800) return 'optimal';
      return 'premium';
    },
    statusClass() {
      return `status-${this.status}`;
    },
    statusLabel() {
      switch (this.status) {
        case 'unfeasible': return 'Μη Βιώσιμο';
        case 'cosmetic': return 'Cosmetic';
        case 'optimal': return 'Optimal';
        case 'premium': return 'Premium';
        default: return '';
      }
    },
    statusHeader() {
      switch (this.status) {
        case 'unfeasible': return ' Μη Βιώσιμο Budget';
        case 'cosmetic': return ' Cosmetic Only Mode';
        case 'optimal': return ' Optimal Renovation';
        case 'premium': return ' Premium Smart Home';
        default: return '';
      }
    },
    explanationIcon() {
      switch (this.status) {
        case 'unfeasible': return '';
        case 'cosmetic': return '';
        case 'optimal': return '';
        case 'premium': return '';
        default: return 'ℹ️';
      }
    },
    statusDescription() {
      switch (this.status) {
        case 'unfeasible': return 'Το budget είναι πολύ χαμηλό (< 200€/τ.μ.). Η AI δεν μπορεί να παράγει visual rendering. Αυξήστε το budget.';
        case 'cosmetic': return 'Το budget σας (200€ - 450€/τ.μ.) επαρκεί για επιφανειακές αλλαγές (βάψιμο, απλά δάπεδα, φωτιστικά).';
        case 'optimal': return 'Εξαιρετικό budget (450€ - 800€/τ.μ.) για ριζική ανακαίνιση (υδραυλικά, γκρεμίσματα, νέα κουζίνα/μπάνιο).';
        case 'premium': return 'Premium budget (>= 800€/τ.μ.). Επιτρέπει χρήση πολυτελών υλικών, smart home συστήματα και custom ξυλουργικές εργασίες.';
        default: return '';
      }
    },
    upsellRecommendation() {
      if (!this.budget || !this.squareMeters || this.squareMeters <= 0) return null;

      const currentSqmCost = this.budget / this.squareMeters;
      let targetSqmCost = 0;
      let targetName = '';
      let benefitsMsg = '';
      let extraRent = 0;

      if (currentSqmCost < 200) {
        targetSqmCost = 200;
        targetName = 'Cosmetic (Αισθητική Αναβάθμιση)';
        extraRent = Math.max(50, Math.round(0.6 * this.squareMeters));
        benefitsMsg = 'Θα επιτρέψει το AI visual rendering, θα βελτιώσει την εμφάνιση του σπιτιού και θα ξεκλειδώσει την εκτίμηση ενοικίου.';
      } else if (currentSqmCost >= 200 && currentSqmCost < 450) {
        targetSqmCost = 450;
        targetName = 'Optimal (Ριζική Ανακαίνιση)';
        extraRent = Math.max(70, Math.round(1.1 * this.squareMeters));
        benefitsMsg = 'Επιτρέπει ριζικές αλλαγές (υδραυλικά, μπάνιο, κουζίνα). Θα προσελκύσει ενοικιαστές υψηλότερης ποιότητας.';
      } else if (currentSqmCost >= 450 && currentSqmCost < 800) {
        targetSqmCost = 800;
        targetName = 'Premium (Πολυτελές Smart Home)';
        extraRent = Math.max(100, Math.round(1.8 * this.squareMeters));
        benefitsMsg = 'Θα ενσωματώσει smart home αυτοματισμούς, custom ξυλουργικά και πολυτελή υλικά, μεγιστοποιώντας την αξία μεταπώλησης/ενοικίασης.';
      } else {
        const targetBudget = Math.round(this.budget * 1.25);
        const extraBudget = targetBudget - this.budget;
        extraRent = Math.max(60, Math.round(0.8 * this.squareMeters));
        const paybackMonths = Math.round(extraBudget / extraRent);
        const paybackYears = (paybackMonths / 12).toFixed(1);

        return {
          currentTier: 'Premium',
          targetName: 'Ultra-Premium Automation & Energy Class A+',
          targetBudget,
          extraBudget,
          extraRent,
          paybackMonths,
          paybackYears,
          benefitsMsg: 'Προσθήκη αντλίας θερμότητας, φωτοβολταϊκών net-metering και πλήρους αυτοματισμού IoT για μέγιστη εξοικονόμηση ενέργειας.'
        };
      }

      const targetBudget = Math.round(targetSqmCost * this.squareMeters);
      const extraBudget = targetBudget - this.budget;
      const paybackMonths = Math.round(extraBudget / extraRent);
      const paybackYears = (paybackMonths / 12).toFixed(1);

      return {
        targetName,
        targetBudget,
        extraBudget,
        extraRent,
        paybackMonths,
        paybackYears,
        benefitsMsg
      };
    }
  }
}
</script>

<style scoped>
.feasibility-card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.card-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--primary-color);
}

.widget-icon {
  font-size: 18px;
}


.basic-mode-info {
  display: flex;
  gap: 16px;
  background: var(--info-light);
  border-radius: var(--radius-md);
  padding: 18px;
  border-left: 4px solid var(--info-color);
}

.info-icon {
  font-size: 24px;
}

.info-content {
  flex: 1;
}

.info-title {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--info-color);
}

.info-desc {
  margin: 0 0 14px 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
}

.cost-estimate {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid #bfdbfe;
}

.cost-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
}

.cost-value {
  font-size: 16px;
  font-weight: 800;
  color: var(--info-color);
}


.metric-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 10px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
}

.metric-value.status-unfeasible { color: var(--danger-color); }
.metric-value.status-cosmetic { color: var(--warning-color); }
.metric-value.status-optimal { color: var(--success-color); }
.metric-value.status-premium { color: #8b5cf6; }


.progress-bar-container {
  margin: 30px 0;
  position: relative;
}

.progress-bar {
  display: flex;
  height: 8px;
  border-radius: var(--radius-full);
  position: relative;
  background: #f1f5f9;
}

.segment {
  height: 100%;
}

.segment.unfeasible { width: 20%; background: #fee2e2; border-top-left-radius: 4px; border-bottom-left-radius: 4px; }
.segment.cosmetic { width: 25%; background: #fef3c7; }
.segment.optimal { width: 35%; background: #d1fae5; }
.segment.premium { width: 20%; background: #ede9fe; border-top-right-radius: 4px; border-bottom-right-radius: 4px; }


.needle {
  position: absolute;
  top: -8px;
  width: 4px;
  height: 24px;
  background: var(--primary-color);
  border-radius: var(--radius-full);
  transform: translateX(-50%);
  transition: var(--transition-normal);
}

.needle::after {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px 6px 0 6px;
  border-style: solid;
  border-color: var(--primary-color) transparent transparent transparent;
}

.needle.status-unfeasible { background: var(--danger-color); }
.needle.status-unfeasible::after { border-top-color: var(--danger-color); }
.needle.status-cosmetic { background: var(--warning-color); }
.needle.status-cosmetic::after { border-top-color: var(--warning-color); }
.needle.status-optimal { background: var(--success-color); }
.needle.status-optimal::after { border-top-color: var(--success-color); }
.needle.status-premium { background: #8b5cf6; }
.needle.status-premium::after { border-top-color: #8b5cf6; }

.needle-label {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--primary-color);
  color: white;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: var(--shadow-sm);
}

.needle.status-unfeasible .needle-label { background: var(--danger-color); }
.needle.status-cosmetic .needle-label { background: var(--warning-color); }
.needle.status-optimal .needle-label { background: var(--success-color); }
.needle.status-premium .needle-label { background: #8b5cf6; }


.explanation-box {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid;
  font-size: 13px;
  line-height: 1.55;
  transition: var(--transition-normal);
}

.explanation-box.status-unfeasible { background: var(--danger-light); border-color: #fecaca; color: #991b1b; }
.explanation-box.status-cosmetic { background: var(--warning-light); border-color: #fef3c7; color: #92400e; }
.explanation-box.status-optimal { background: var(--success-light); border-color: #bbf7d0; color: #166534; }
.explanation-box.status-premium { background: #faf5ff; border-color: #e9d5ff; color: #5b21b6; }

.explanation-icon {
  font-size: 20px;
}

.explanation-text strong {
  display: block;
  margin-bottom: 4px;
  font-size: 14px;
}

.explanation-text p {
  margin: 0;
}


.upsell-box {
  margin-top: 24px;
  background: linear-gradient(135deg, #f0fdfa 0%, #e6fffa 100%);
  border: 1px solid #99f6e4;
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: 0 4px 15px rgba(13, 148, 136, 0.04);
}

.upsell-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  border-bottom: 1px solid #ccfbf1;
  padding-bottom: 8px;
}

.upsell-header h5 {
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  color: #0f766e;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sparkle-icon {
  font-size: 16px;
}

.upsell-intro {
  margin: 0 0 16px 0;
  font-size: 13px;
  color: #115e59;
  line-height: 1.5;
}

.benefits-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.benefit-item {
  background: #ffffff;
  border: 1px solid #ccfbf1;
  border-radius: var(--radius-md);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.benefit-icon {
  font-size: 20px;
}

.benefit-details {
  display: flex;
  flex-direction: column;
}

.benefit-label {
  font-size: 9px;
  color: #64748b;
  font-weight: 700;
  text-transform: uppercase;
}

.benefit-value {
  font-size: 13px;
  font-weight: 800;
  color: #0f766e;
}

.benefits-footer {
  background: rgba(255, 255, 255, 0.4);
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(204, 251, 241, 0.5);
}

.upsell-benefits {
  margin: 0;
  font-size: 12px;
  color: #134e4a;
  line-height: 1.5;
}

.animate-fade {
  animation: fadeIn 0.4s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
