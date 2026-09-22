<template>

  <router-view v-if="auth.loaded" />
</template>

<script>
import { auth } from '@/stores/auth'

export default {
  name: 'App',

  data() {
    return {
      auth
    }
  },

  mounted() {
    fetch('/api/me')
      .then(res => {
        if (!res.ok) throw new Error()
        return res.json()
      })
      .then(data => {
        this.auth.user = data
        this.auth.loaded = true
      })
      .catch(() => {
        this.auth.user = null
        this.auth.loaded = true
      })
  }
}
</script>
