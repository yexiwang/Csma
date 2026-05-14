<template>
  <div class="setmeal-card" @click="$emit('detail', setmeal)">
    <img
      :src="setmeal.image"
      class="setmeal-image"
      :alt="setmeal.name"
      loading="lazy"
    />
    <div class="setmeal-info">
      <h3 class="setmeal-name">{{ setmeal.name }}</h3>
      <p class="setmeal-description">{{ setmeal.description || '暂无套餐描述' }}</p>
      <div v-if="setmeal.setmealDishes && setmeal.setmealDishes.length > 0" class="setmeal-dishes">
        <span
          v-for="sd in setmeal.setmealDishes"
          :key="sd.id"
          class="setmeal-dish-tag"
        >{{ sd.name }} x{{ sd.copies }}</span>
      </div>
      <div class="setmeal-footer">
        <span class="setmeal-price">￥{{ formatPrice(setmeal.price) }}</span>
        <span class="setmeal-slot" @click.stop>
          <slot name="actions" :setmeal="setmeal"></slot>
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { Setmeal } from '@/api/family'

@Component({
  name: 'SetmealCard'
})
export default class SetmealCard extends Vue {
  @Prop({ required: true }) private readonly setmeal!: Setmeal

  private formatPrice(price: number) {
    return Number(price || 0).toFixed(2)
  }
}
</script>

<style lang="scss" scoped>
.setmeal-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
  }
}

.setmeal-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
  background: #f5f7fa;
}

.setmeal-info {
  padding: 14px;
}

.setmeal-name {
  margin: 0 0 8px;
  font-size: 15px;
  color: #303133;
}

.setmeal-description {
  margin: 0 0 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  min-height: 36px;
}

.setmeal-dishes {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.setmeal-dish-tag {
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 11px;
  line-height: 1.6;
}

.setmeal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setmeal-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}
</style>
