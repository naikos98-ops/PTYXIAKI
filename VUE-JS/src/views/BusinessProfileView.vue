<template>
  <div class="profile-container animate-fade-in">

    <nav class="navbar">
      <div class="navbar-content">
        <button @click="goBack" class="btn-premium btn-premium-secondary btn-back-custom">
          ← Dashboard
        </button>
        <div class="nav-title">
          <h1>Επαγγελματικό Προφίλ</h1>
        </div>
        <div class="nav-spacer"></div>
      </div>
    </nav>

    <div class="main-content">
      <div v-if="loading" class="loading-state">
        <div class="spinner-large"></div>
        <p>Φόρτωση προφίλ...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <p> {{ error }}</p>
        <button @click="goBack" class="btn-premium btn-premium-primary" style="margin-top: 15px;">
          Επιστροφή
        </button>
      </div>

      <div v-else-if="profile" class="profile-layout">

        <div class="profile-left">
          <div class="card-premium profile-info-card">
            <div class="avatar-circle">
              {{ profile.username.substring(0, 2).toUpperCase() }}
            </div>

            <h2>{{ profile.username }}</h2>
            <span class="role-badge" :class="profile.role">
              {{ profile.role === 'REAL_ESTATE_AGENCY' ? ' ΜΕΣΙΤΙΚΟ ΓΡΑΦΕΙΟ' : ' ΚΑΤΑΣΚΕΥΑΣΤΙΚΗ ΕΤΑΙΡΕΙΑ' }}
            </span>

            <div class="rating-summary">
              <span class="avg-val">{{ profile.averageRating > 0 ? profile.averageRating.toFixed(1) : 'N/A' }}</span>
              <span class="total-reviews">/ 10 ({{ profile.reviews.length }} αξιολογήσεις)</span>
            </div>

            <hr class="divider" />

            <div class="details-list">
              <div class="details-item">
                <span class="details-label">Περιφέρεια/Δήμος:</span>
                <span class="details-value">{{ profile.region || 'Όλη την Ελλάδα' }}</span>
              </div>
              <div class="details-item">
                <span class="details-label">ΑΦΜ:</span>
                <span class="details-value">{{ profile.afm || 'N/A' }}</span>
              </div>
              <div class="details-item">
                <span class="details-label">ΓΕΜΗ:</span>
                <span class="details-value">{{ profile.gemiNumber || 'N/A' }}</span>
              </div>
              <div class="details-item">
                <span class="details-label">Κατάσταση:</span>
                <span class="badge-premium" :class="profile.isVerified ? 'badge-premium-success' : 'badge-premium-danger'">
                  {{ profile.isVerified ? ' Εγκεκριμένο' : ' Εκκρεμεί Έγκριση' }}
                </span>
              </div>
            </div>
          </div>


          <div v-if="isOwner" class="card-premium post-create-card">
            <h3> Νέα Δημοσίευση / Ενημέρωση</h3>
            <div class="form-group">
              <textarea
                v-model="postForm.content"
                placeholder="Τι νέο υπάρχει; Μοιραστείτε μια ενημέρωση ή ολοκληρωμένο έργο..."
                rows="4"
                class="form-input-premium textarea-custom"
              ></textarea>
            </div>

            <div class="form-group">
              <label class="file-upload-label-premium">
                <span class="file-text">{{ postForm.image ? postForm.image.name : 'Προσθήκη Φωτογραφίας...' }}</span>
                <input type="file" @change="onPostFileChange" class="file-input-hidden" />
              </label>
            </div>

            <button @click="submitPost" :disabled="posting" class="btn-premium btn-premium-primary full-width">
              {{ posting ? 'Δημοσίευση...' : 'Δημοσίευση ' }}
            </button>
            <p v-if="postError" class="error-msg">{{ postError }}</p>
          </div>
        </div>


        <div class="profile-right">

          <div v-if="profile.hasCooperated" class="card-premium review-submit-card animate-slide-down">
            <h3>⭐️ Αξιολόγηση Συνεργασίας</h3>
            <p class="review-intro">Επειδή έχετε συνεργαστεί με την {{ profile.username }}, μπορείτε να βαθμολογήσετε τις υπηρεσίες της.</p>

            <div class="review-row">
              <div class="form-group rating-select">
                <label class="input-label">Βαθμολογία</label>
                <select v-model.number="reviewForm.rating" class="form-input-premium select-custom">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} / 10</option>
                </select>
              </div>

              <div class="form-group comment-area">
                <label class="input-label">Σχόλιο / Εμπειρία Συνεργασίας</label>
                <textarea
                  v-model="reviewForm.comment"
                  placeholder="Πώς ήταν η επικοινωνία, η ποιότητα της δουλειάς και το χρονοδιάγραμμα;"
                  rows="2"
                  class="form-input-premium textarea-custom"
                ></textarea>
              </div>
            </div>

            <button @click="submitReview" :disabled="reviewing" class="btn-premium btn-premium-accent block-btn">
              {{ reviewing ? 'Υποβολή...' : 'Υποβολή Αξιολόγησης ⭐' }}
            </button>
            <p v-if="reviewError" class="error-msg">{{ reviewError }}</p>
          </div>


          <div class="feed-section">
            <h3 class="feed-title"> Ενημερώσεις & Έργα ({{ profile.posts.length }})</h3>
            <div v-if="profile.posts.length === 0" class="empty-state-feed">
              <p>Δεν έχουν γίνει δημοσιεύσεις ακόμα από αυτόν τον επαγγελματία.</p>
            </div>

            <div v-else class="posts-list">
              <div v-for="post in profile.posts" :key="post.id" class="card-premium post-card">
                <p class="post-content">{{ post.content }}</p>
                <div class="post-image-container animate-fade-in" v-if="post.imagePath">
                  <img
                    :src="backendUrl + post.imagePath.replace(/\\/g, '/')"
                    alt="Post Image"
                    class="post-image"
                  />
                </div>
                <div class="post-footer">
                  <span class="post-date"> {{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>


          <div class="reviews-section">
            <h3 class="feed-title"> Αξιολογήσεις Χρηστών ({{ profile.reviews.length }})</h3>
            <div v-if="profile.reviews.length === 0" class="empty-state-feed">
              <p>Δεν υπάρχουν αξιολογήσεις ακόμα για αυτόν τον επαγγελματία.</p>
            </div>

            <div v-else class="reviews-list">
              <div v-for="review in profile.reviews" :key="review.id" class="card-premium review-card">
                <div class="review-header">
                  <span class="review-author"> {{ review.authorName }}</span>
                  <span class="review-score"> {{ review.rating }} / 10</span>
                </div>
                <p class="review-comment">{{ review.comment }}</p>
                <div class="review-footer">
                  <span class="review-date"> {{ formatDate(review.createdAt) }}</span>
                </div>
              </div>
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
  name: 'BusinessProfileView',
  data() {
    return {
      auth,
      userId: null,
      profile: null,
      loading: true,
      error: null,
      backendUrl: 'http://localhost:8082',
      posting: false,
      postError: '',
      postForm: {
        content: '',
        image: null
      },
      reviewing: false,
      reviewError: '',
      reviewForm: {
        rating: 10,
        comment: ''
      }
    }
  },
  computed: {
    isOwner() {
      return this.profile && this.auth.user && this.profile.id === this.auth.user.id;
    }
  },
  methods: {
    goBack() {
      if (this.auth.role === 'ADMIN') {
        this.$router.push('/admin/dashboard');
      } else {
        this.$router.push('/dashboard');
      }
    },
    onPostFileChange(event) {
      this.postForm.image = event.target.files[0];
    },
    fetchProfile() {
      this.loading = true;
      this.error = null;
      fetch(`/api/business-profile/${this.userId}`)
        .then(res => {
          if (!res.ok) {
            throw new Error('Αποτυχία φόρτωσης του επαγγελματικού προφίλ.');
          }
          return res.json();
        })
        .then(data => {
          this.profile = data;
          this.loading = false;
        })
        .catch(err => {
          this.error = err.message;
          this.loading = false;
        });
    },
    submitPost() {
      if (!this.postForm.content.trim()) {
        this.postError = 'Παρακαλούμε πληκτρολογήστε περιεχόμενο.';
        return;
      }
      this.posting = true;
      this.postError = '';

      const formData = new FormData();
      formData.append('content', this.postForm.content);
      if (this.postForm.image) {
        formData.append('image', this.postForm.image);
      }

      fetch('/api/business-profile/posts', {
        method: 'POST',
        body: formData
      })
      .then(res => {
        if (!res.ok) throw new Error('Σφάλμα κατά τη δημιουργία δημοσίευσης.');
        this.postForm.content = '';
        this.postForm.image = null;
        this.fetchProfile();
      })
      .catch(err => {
        this.postError = err.message;
      })
      .finally(() => {
        this.posting = false;
      });
    },
    submitReview() {
      if (!this.reviewForm.comment.trim()) {
        this.reviewError = 'Παρακαλούμε γράψτε ένα σχόλιο.';
        return;
      }
      this.reviewing = true;
      this.reviewError = '';

      const params = new URLSearchParams();
      params.append('rating', this.reviewForm.rating);
      params.append('comment', this.reviewForm.comment);

      fetch(`/api/business-profile/${this.userId}/reviews`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
      })
      .then(res => {
        if (!res.ok) throw new Error('Σφάλμα κατά την υποβολή αξιολόγησης.');
        this.reviewForm.comment = '';
        this.reviewForm.rating = 10;
        this.fetchProfile();
      })
      .catch(err => {
        this.reviewError = err.message;
      })
      .finally(() => {
        this.reviewing = false;
      });
    },
    formatDate(dateStr) {
      if (!dateStr) return '';
      const d = new Date(dateStr);
      return d.toLocaleDateString('el-GR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  },
  mounted() {
    this.userId = this.$route.params.userId;
    this.fetchProfile();
  },
  watch: {
    '$route.params.userId'(newId) {
      if (newId) {
        this.userId = newId;
        this.fetchProfile();
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: var(--bg-color);
  color: var(--text-main);
  display: flex;
  flex-direction: column;
}

.navbar {
  background: var(--primary-color);
  padding: 16px 0;
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.btn-back-custom {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #ffffff !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.btn-back-custom:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: #ffffff !important;
}

.nav-title h1 {
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
  margin: 0;
}

.nav-spacer {
  width: 110px;
}

@media (max-width: 600px) {
  .navbar-content {
    padding: 0 16px;
  }
  .nav-spacer {
    display: none;
  }
}

.main-content {
  padding: 40px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
  flex: 1;
}

.profile-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 30px;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}


.profile-info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar-circle {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 20px;
  border: 4px solid var(--accent-color);
  box-shadow: var(--shadow-lg);
  font-family: var(--font-title);
}

.profile-info-card h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary-color);
  margin: 0 0 8px 0;
}

.role-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 6px 14px;
  border-radius: var(--radius-full);
  margin-bottom: 20px;
  letter-spacing: 0.5px;
}

.role-badge.CONSTRUCTION_COMPANY {
  background: #eff6ff;
  color: #1d4ed8;
}

.role-badge.REAL_ESTATE_AGENCY {
  background: #fdf4ff;
  color: #a21caf;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--accent-light);
  padding: 10px 18px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 209, 0, 0.3);
  margin-bottom: 24px;
}

.star-rating {
  font-size: 20px;
  color: var(--accent-color);
  line-height: 1;
}

.avg-val {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
  font-family: var(--font-title);
}

.total-reviews {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 600;
}

.divider {
  border: 0;
  height: 1px;
  background: var(--border-color);
  width: 100%;
  margin: 0 0 24px 0;
}

.details-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}

.details-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.details-label {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.details-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
}


.post-create-card {
  margin-top: 24px;
}

.post-create-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 16px 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.textarea-custom {
  resize: vertical;
  min-height: 100px;
}

.file-upload-label-premium {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--primary-light);
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 700;
  transition: var(--transition-normal);
}

.file-upload-label-premium:hover {
  background: #e2e8f0;
  border-color: var(--primary-color);
}

.file-input-hidden {
  display: none;
}

.full-width {
  width: 100%;
}

.block-btn {
  display: block;
  width: 100%;
}

.error-msg {
  color: var(--danger-color);
  font-size: 12px;
  margin: 8px 0 0 0;
  text-align: center;
  font-weight: 600;
}


.review-submit-card {
  margin-bottom: 24px;
}

.review-submit-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  margin: 0 0 8px 0;
}

.review-intro {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 20px 0;
}

.review-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 600px) {
  .review-row {
    grid-template-columns: 1fr;
  }
}

.input-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.select-custom {
  appearance: none;
  background-image: url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%235e6b7e' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 9l6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 16px;
  padding-right: 40px !important;
}


.feed-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-color);
  margin: 0 0 20px 0;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 10px;
}

.posts-list, .reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 40px;
}

.post-card {
  border-radius: var(--radius-lg);
  padding: 24px;
}

.post-content {
  font-size: 15px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  color: var(--text-main);
  white-space: pre-wrap;
}

.post-image-container {
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.post-image {
  width: 100%;
  max-height: 380px;
  object-fit: cover;
  display: block;
}

.post-footer, .review-footer {
  display: flex;
  justify-content: flex-end;
}

.post-date, .review-date {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}


.review-card {
  border-radius: var(--radius-lg);
  padding: 24px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-author {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary-color);
}

.review-score {
  font-size: 13px;
  font-weight: 800;
  color: var(--primary-color);
  background: var(--accent-light);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 209, 0, 0.3);
}

.review-comment {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main);
  margin: 0 0 12px 0;
}


.loading-state, .error-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.empty-state-feed {
  text-align: center;
  padding: 40px 20px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  border: 2px dashed var(--border-color);
  color: var(--text-muted);
}

.empty-feed-icon {
  font-size: 36px;
  display: block;
  margin-bottom: 12px;
}

.spinner-large {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(15, 41, 66, 0.1);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
