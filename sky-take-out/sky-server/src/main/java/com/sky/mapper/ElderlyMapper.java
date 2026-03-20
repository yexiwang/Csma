package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Elderly;
import com.sky.vo.ElderlyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ElderlyMapper {

    void insert(Elderly elderly);

    Page<ElderlyVO> pageQueryWithDiningPoint(Elderly elderly);

    Page<Elderly> pageQuery(Elderly elderly);

    @Select("select * from elderly where id = #{id} and is_deleted = 0")
    Elderly getById(Long id);

    ElderlyVO getDetailById(Long id);

    @Select("select * from elderly where user_id = #{userId} and is_deleted = 0 order by create_time desc")
    List<Elderly> getByUserId(Long userId);

    void update(Elderly elderly);

    void deleteById(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);

    @Select("select count(1) from elderly where user_id = #{userId} and is_deleted = 0")
    Integer countActiveByUserId(Long userId);
}
