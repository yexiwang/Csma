package com.sky.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserElderlyVO implements Serializable {

    private Long id;

    private String name;

    private String phone;

    private String address;

    private Long diningPointId;

    private String diningPointName;

    private Integer diningPointStatus;
}
