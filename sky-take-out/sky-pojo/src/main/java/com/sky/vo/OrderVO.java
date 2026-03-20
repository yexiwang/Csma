package com.sky.vo;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {

    private String orderDishes;

    private List<OrderDetail> orderDetailList;

    private String volunteerName;

    private String volunteerPhone;

    private Integer volunteerStatus;

    private String diningPointName;

    private String elderName;

    private String elderPhone;

    private String elderAddress;

    private String elderSpecialNeeds;

    private String handoverStatus;
}
