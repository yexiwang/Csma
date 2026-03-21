package com.sky.mapper;

import com.sky.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderReviewMapper {

    void insert(OrderReview orderReview);

    OrderReview getByOrderId(Long orderId);

    List<OrderReview> listByOrderIds(List<Long> orderIds);

    BigDecimal avgScoreByVolunteerUserId(Long volunteerUserId);
}
