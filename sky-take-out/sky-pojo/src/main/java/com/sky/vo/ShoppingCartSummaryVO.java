package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartSummaryVO implements Serializable {

    private Long elderId;

    private Long diningPointId;

    private Integer totalCount;

    private BigDecimal dishAmount;

    private BigDecimal deliveryFee;

    private BigDecimal tablewareFee;

    private BigDecimal subsidyAmount;

    private BigDecimal payAmount;

    private Integer effectiveTablewareNumber;
}
