package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiningPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    //助餐点名称
    private String name;
    
    //详细地址
    private String address;
    
    //联系电话
    private String contactPhone;
    
    //营业时间
    private String operatingHours;
    
    //状态 0:休息 1:营业
    private Integer status;
    
    //助餐点图片
    private String image;
    
    //服务网格范围(JSON数组)
    private String gridCoverage;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Long createUser;
    
    private Long updateUser;
}
