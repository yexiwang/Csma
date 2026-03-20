package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.VolunteerPageQueryDTO;
import com.sky.entity.User;
import com.sky.vo.VolunteerVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VolunteerMapper {

    Page<VolunteerVO> pageQuery(VolunteerPageQueryDTO volunteerPageQueryDTO);

    User getByUsername(String username);

    User getVolunteerEntityById(Long id);

    VolunteerVO getVolunteerById(Long id);

    void insert(User user);

    void update(User user);

    List<VolunteerVO> listActiveVolunteers();
}
