package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "下单时传递的数据模型")
public class OrdersSubmitDTO implements Serializable {

    private Long elderId;

    private Long addressBookId;

    private int payMethod;

    private String remark;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    private Integer deliveryStatus;

    private Integer tablewareNumber;

    private Integer tablewareStatus;

    private Integer packAmount;

    private BigDecimal dishAmount;

    private BigDecimal deliveryFee;

    private BigDecimal tablewareFee;

    private BigDecimal subsidyAmount;

    private BigDecimal payAmount;

    private BigDecimal amount;
}
