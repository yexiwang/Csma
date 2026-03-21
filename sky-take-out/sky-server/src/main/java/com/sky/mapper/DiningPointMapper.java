package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.DiningPoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DiningPointMapper {

    void insert(DiningPoint diningPoint);

    Page<DiningPoint> pageQuery(String name, Integer status);

    List<DiningPoint> list(Integer status);

    @Select("select * from dining_point where id = #{id}")
    DiningPoint getById(Long id);

    void update(DiningPoint diningPoint);

    @Delete("delete from dining_point where id = #{id}")
    void deleteById(Long id);
}
