<template>
  <div class="dish-card">
    <img
      :src="dish.image"
      class="dish-image"
      :alt="dish.name"
      loading="lazy"
    />
    <div class="dish-info">
      <h3 class="dish-name">{{ dish.name }}</h3>
      <p class="dish-description">{{ dish.description || '暂无菜品描述' }}</p>
      <div class="dish-footer">
        <span class="dish-price">￥{{ formatPrice(dish.price) }}</span>
        <slot name="actions" :dish="dish"></slot>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { Dish } from '@/api/family'

@Component({
  name: 'DishCard'
})
export default class DishCard extends Vue {
  @Prop({ required: true }) private readonly dish!: Dish

  private formatPrice(price: number) {
    return Number(price || 0).toFixed(2)
  }
}
</script>

<style lang="scss" scoped>
.dish-card {
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

.dish-image {
  width: 100%;
  height: 140px;
  object-fit: cover;
  background: #f5f7fa;
}

.dish-info {
  padding: 14px;
}

.dish-name {
  margin: 0 0 8px;
  font-size: 15px;
  color: #303133;
}

.dish-description {
  margin: 0 0 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  min-height: 36px;
}

.dish-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}
</style>
