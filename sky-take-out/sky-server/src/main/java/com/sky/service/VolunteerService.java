package com.sky.service;

import com.sky.dto.VolunteerDTO;
import com.sky.dto.VolunteerPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.VolunteerOverviewVO;
import com.sky.vo.VolunteerVO;

import java.util.List;

public interface VolunteerService {

    PageResult pageQuery(VolunteerPageQueryDTO volunteerPageQueryDTO);

    void save(VolunteerDTO volunteerDTO);

    void update(VolunteerDTO volunteerDTO);

    void startOrStop(Integer status, Long id);

    VolunteerVO getById(Long id);

    List<VolunteerVO> listActiveVolunteers();

    VolunteerOverviewVO getCurrentOverview();
}
