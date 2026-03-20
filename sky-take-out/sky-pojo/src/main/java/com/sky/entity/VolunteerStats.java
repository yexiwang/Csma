package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerStats implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    //志愿者用户ID
    private Long userId;
    
    //累计服务单量
    private Integer totalOrders;
    
    //累计服务时长(小时)
    private BigDecimal totalHours;
    
    //综合评分
    private BigDecimal rating;
    
    //等级
    private Integer level;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
