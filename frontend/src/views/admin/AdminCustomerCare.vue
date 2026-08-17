<template>
  <div class="customer-care-manager">
    <main class="main-content">
      <section class="page-header">
        <h2>Quản Lý Chăm Sóc Khách Hàng</h2>
        <p class="page-subtitle">
          Theo dõi hoạt động Telegram Chatbot: FAQ, hội thoại và lịch sử tư vấn khách hàng.
        </p>
      </section>

      <nav class="tab-nav">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-btn"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </nav>

      <section class="tab-content">
        <CustomerCareOverview v-if="activeTab === 'overview'" />
        <FaqManagement v-else-if="activeTab === 'faq'" />
        <ConversationManagement v-else-if="activeTab === 'conversations'" />
        <ChatLogManagement v-else-if="activeTab === 'logs'" />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CustomerCareOverview from '@/components/admin/customer-care/CustomerCareOverview.vue'
import FaqManagement from '@/components/admin/customer-care/FaqManagement.vue'
import ConversationManagement from '@/components/admin/customer-care/ConversationManagement.vue'
import ChatLogManagement from '@/components/admin/customer-care/ChatLogManagement.vue'

type TabId = 'overview' | 'faq' | 'conversations' | 'logs'

const tabs: { id: TabId; label: string }[] = [
  { id: 'overview', label: 'TỔNG QUAN' },
  { id: 'faq', label: 'FAQ' },
  { id: 'conversations', label: 'HỘI THOẠI' },
  { id: 'logs', label: 'CHAT LOGS' },
]

const activeTab = ref<TabId>('overview')
</script>

<style scoped>
.customer-care-manager {
  display: flex;
  min-height: 100vh;
  background: #F9F9F9;
}

.main-content {
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #848484;
  margin: 0;
}

.tab-nav {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid #E8E8E8;
  margin-bottom: 24px;
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 12px 20px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #5E5F5C;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #000000;
}

.tab-btn.active {
  color: #000000;
  border-bottom-color: #000000;
  font-weight: 700;
}

.tab-content {
  min-height: 320px;
}
</style>
