package com.sky.mapper;

import com.sky.entity.VolunteerStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VolunteerStatsMapper {

    void insert(VolunteerStats volunteerStats);

    @Select("select * from volunteer_stats where user_id = #{userId}")
    VolunteerStats getByUserId(Long userId);

    void update(VolunteerStats volunteerStats);
}
