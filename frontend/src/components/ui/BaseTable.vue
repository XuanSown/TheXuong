<template>
  <div class="base-table-container">
    <div class="table-wrapper">
      <table class="base-table">
        <thead>
          <tr>
            <th 
              v-for="col in columns" 
              :key="col.key"
              :class="['table-th', col.headerClass]"
              :style="{ width: col.width }"
            >
              {{ col.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td
              :colspan="columns.length"
              class="table-td text-center py-8"
            >
              <div class="flex justify-center items-center">
                <div class="spinner" />
                <span class="ml-2 text-gray-500">{{ t('common.loading') }}</span>
              </div>
            </td>
          </tr>
          <tr v-else-if="!data || data.length === 0">
            <td
              :colspan="columns.length"
              class="table-td text-center py-8 text-gray-500"
            >
              {{ emptyText || t('common.noData') }}
            </td>
          </tr>
          <template v-else>
            <tr 
              v-for="(row, index) in data" 
              :key="row.id || index"
              class="table-row"
            >
              <td 
                v-for="col in columns" 
                :key="col.key"
                :class="['table-td', col.cellClass]"
              >
                <!-- Allow custom rendering via scoped slots -->
                <slot
                  :name="`cell-${col.key}`"
                  :row="row"
                  :value="row[col.key]"
                >
                  {{ row[col.key] }}
                </slot>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

export interface TableColumn {
  key: string
  label: string
  width?: string
  headerClass?: string
  cellClass?: string
}

export interface Props {
  columns: TableColumn[]
  data: any[]
  loading?: boolean
  emptyText?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  emptyText: ''
})
</script>

<style scoped>
.base-table-container {
  width: 100%;
  background: #FFFFFF;
  border: 1px solid rgba(207, 196, 197, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.base-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.table-th {
  padding: 16px 24px;
  background-color: #F9FAFB;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #5E5F5C;
  border-bottom: 1px solid rgba(207, 196, 197, 0.5);
  white-space: nowrap;
}

.table-td {
  padding: 16px 24px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #111827;
  border-bottom: 1px solid rgba(207, 196, 197, 0.2);
  vertical-align: middle;
}

.table-row {
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #F9FAFB;
}

.table-row:last-child .table-td {
  border-bottom: none;
}

.text-center {
  text-align: center;
}

.py-8 {
  padding-top: 32px;
  padding-bottom: 32px;
}

.text-gray-500 {
  color: #6B7280;
}

.flex {
  display: flex;
}

.justify-center {
  justify-content: center;
}

.items-center {
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}

/* Spinner */
.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #E5E7EB;
  border-radius: 50%;
  border-top-color: #000000;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
