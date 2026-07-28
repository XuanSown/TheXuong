<template>
  <form
    class="flex flex-col gap-4"
    @submit.prevent="onSubmit"
  >
    <BaseInput
      v-model="form.label"
      label="Nhãn (vd: Nhà, Công ty)"
      placeholder="Nhà"
    />
    <div class="grid grid-cols-2 gap-3">
      <BaseInput
        v-model="form.recipientName"
        label="Tên người nhận"
        required
      />
      <BaseInput
        v-model="form.recipientPhone"
        type="tel"
        label="SĐT người nhận"
        required
      />
    </div>
    <div class="grid grid-cols-3 gap-3">
      <div>
        <label class="text-xs uppercase text-[#4C4546]">Tỉnh/Thành</label>
        <select
          v-model="form.provinceCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px]"
          @change="onProvinceChange"
        >
          <option value="">
            Chọn...
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
        <label class="text-xs uppercase text-[#4C4546]">Quận/Huyện</label>
        <select
          v-model="form.districtCode"
          :disabled="!form.provinceCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100"
          @change="onDistrictChange"
        >
          <option value="">
            Chọn...
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
        <label class="text-xs uppercase text-[#4C4546]">Phường/Xã</label>
        <select
          v-model="form.wardCode"
          :disabled="!form.districtCode"
          class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100"
        >
          <option value="">
            Chọn...
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
      <label class="text-xs uppercase text-[#4C4546]">Số nhà, tên đường</label>
      <input
        v-model="form.streetDetail"
        placeholder="Nhập số nhà, tên đường"
        class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-3 font-gelasio text-[16px] outline-none focus:border-black"
      >
      <button
        type="button"
        class="self-start text-xs flex items-center gap-1 text-gray-400 underline mt-1"
        @click="notifyDev"
      >
        Dùng vị trí của tôi
      </button>
      <p class="text-[11px] text-gray-400 italic">
        Gợi ý địa chỉ &amp; định vị tự động đang được phát triển
      </p>
    </div>
    <label class="flex items-center gap-2"><input
      v-model="form.isDefault"
      type="checkbox"
    > Đặt làm mặc định</label>
    <div class="flex justify-end gap-2">
      <BaseButton
        variant="outline"
        label="Hủy"
        @click="$emit('cancel')"
      />
      <BaseButton
        variant="primary"
        type="submit"
        label="Lưu"
      />
    </div>
  </form>
</template>
<script setup lang="ts">
import { reactive, computed } from 'vue'
import { getProvinces, getDistricts, getWards } from '@/utils/vn-regions'
import { useToast } from 'vue-toastification'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import type { Address } from '@/types'

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

const notifyDev = () => toast.info('Tính năng định vị tự động đang được phát triển')

const onSubmit = () => {
  if (!form.provinceCode || !form.districtCode || !form.wardCode) { toast.error('Vui lòng chọn đủ Tỉnh/Quận/Phường'); return }
  if (!form.recipientName || !form.recipientPhone) { toast.error('Vui lòng nhập tên và SĐT người nhận'); return }
  emit('submit', { ...form })
}
</script>
