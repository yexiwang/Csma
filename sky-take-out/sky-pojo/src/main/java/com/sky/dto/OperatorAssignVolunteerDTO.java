package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperatorAssignVolunteerDTO implements Serializable {

    private Long orderId;

    private Long volunteerId;
}
