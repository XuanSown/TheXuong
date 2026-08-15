<template>
  <form
    class="flex flex-col gap-4"
    @submit.prevent="onSubmit"
  >
    <BaseInput
      v-model="form.label"
      :label="t('address.label')"
      :placeholder="t('address.labelPlaceholder')"
    />
    <div class="grid grid-cols-2 gap-3">
      <BaseInput
        v-model="form.recipientName"
        :label="t('address.recipientName')"
        required
      />
      <BaseInput
        v-model="form.recipientPhone"
        type="tel"
        :label="t('address.recipientPhone')"
        required
      />
    </div>
    <div class="grid grid-cols-3 gap-3">
      <div>
        <label class="text-xs uppercase text-[#4C4546]">{{ t('address.province') }}</label>
        <select
          v-model="form.provinceCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px]"
          @change="onProvinceChange"
        >
          <option value="">
            {{ t('address.select') }}
          </option>
          <option
            v-for="p in provinces"
            :key="p.code"
            :value="p.code"
          >
            {{ p.nameWithType }}
          </option>
        </select>
      </div>
      <div>
        <label class="text-xs uppercase text-[#4C4546]">{{ t('address.district') }}</label>
        <select
          v-model="form.districtCode"
          :disabled="!form.provinceCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100"
          @change="onDistrictChange"
        >
          <option value="">
            {{ t('address.select') }}
          </option>
          <option
            v-for="d in districts"
            :key="d.code"
            :value="d.code"
          >
            {{ d.nameWithType }}
          </option>
        </select>
      </div>
      <div>
        <label class="text-xs uppercase text-[#4C4546]">{{ t('address.ward') }}</label>
        <select
          v-model="form.wardCode"
          :disabled="!form.districtCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100"
        >
          <option value="">
            {{ t('address.select') }}
          </option>
          <option
            v-for="w in wards"
            :key="w.code"
            :value="w.code"
          >
            {{ w.nameWithType }}
          </option>
        </select>
      </div>
    </div>
    <div class="flex flex-col gap-1">
      <label class="text-xs uppercase text-[#4C4546]">{{ t('address.street') }}</label>
      <input
        v-model="form.streetDetail"
        :placeholder="t('address.streetPlaceholder')"
        class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-3 font-gelasio text-[16px] outline-none focus:border-black"
      >
      <button
        type="button"
        class="self-start text-xs flex items-center gap-1 text-gray-400 underline mt-1"
        @click="notifyDev"
      >
        {{ t('address.useMyLocation') }}
      </button>
      <p class="text-[11px] text-gray-400 italic">
        {{ t('address.locatingComingSoon') }}
      </p>
    </div>
    <label class="flex items-center gap-2"><input
      v-model="form.isDefault"
      type="checkbox"
    > {{ t('address.setDefault') }}</label>
    <div class="flex justify-end gap-2">
      <BaseButton
        variant="outline"
        :label="t('common.cancel')"
        @click="$emit('cancel')"
      />
      <BaseButton
        variant="primary"
        type="submit"
        :label="t('common.save')"
      />
    </div>
  </form>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import { getProvinces, getDistricts, getWards } from '@/utils/vn-regions'
import { useToast } from 'vue-toastification'
import { useI18n } from 'vue-i18n'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import type { Address } from '@/types'

const { t } = useI18n()

const props = defineProps<{ modelValue?: Partial<Address> }>()
const emit = defineEmits<{ submit: [data: any]; cancel: [] }>()
const toast = useToast()
const provinces = getProvinces()
const form = reactive({
  id: props.modelValue?.id,
  label: props.modelValue?.label || '',
  recipientName: props.modelValue?.recipientName || '',
  recipientPhone: props.modelValue?.recipientPhone || '',
  provinceCode: props.modelValue?.provinceCode || '',
  districtCode: props.modelValue?.districtCode || '',
  wardCode: props.modelValue?.wardCode || '',
  streetDetail: props.modelValue?.streetDetail || '',
  latitude: props.modelValue?.latitude,
  longitude: props.modelValue?.longitude,
  isDefault: props.modelValue?.isDefault || false
})
const districts = computed(() => form.provinceCode ? getDistricts(form.provinceCode) : [])
const wards = computed(() => (form.provinceCode && form.districtCode) ? getWards(form.provinceCode, form.districtCode) : [])
const onProvinceChange = () => { form.districtCode = ''; form.wardCode = '' }
const onDistrictChange = () => { form.wardCode = '' }

const notifyDev = () => toast.info(t('address.locatingDev'))

const onSubmit = () => {
  if (!form.provinceCode || !form.districtCode || !form.wardCode) { toast.error(t('address.selectRequired')); return }
  if (!form.recipientName || !form.recipientPhone) { toast.error(t('address.recipientRequired')); return }
  emit('submit', { ...form })
}
</script>
