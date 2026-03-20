package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Elderly;
import com.sky.vo.ElderlyVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ElderlyMapper {

    void insert(Elderly elderly);

    Page<ElderlyVO> pageQueryWithDiningPoint(Elderly elderly);

    Page<Elderly> pageQuery(Elderly elderly);

    @Select("select * from elderly where id = #{id}")
    Elderly getById(Long id);

    ElderlyVO getDetailById(Long id);

    @Select("select * from elderly where user_id = #{userId}")
    List<Elderly> getByUserId(Long userId);

    void update(Elderly elderly);

    @Delete("delete from elderly where id = #{id}")
    void deleteById(Long id);
}
