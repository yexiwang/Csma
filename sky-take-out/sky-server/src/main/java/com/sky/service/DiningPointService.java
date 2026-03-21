package com.sky.service;

import com.github.pagehelper.Page;
import com.sky.entity.DiningPoint;

import java.util.List;

public interface DiningPointService {
    void save(DiningPoint diningPoint);

    Page<DiningPoint> pageQuery(int page, int pageSize, String name, Integer status);

    List<DiningPoint> list(Integer status);

    DiningPoint getById(Long id);

    void update(DiningPoint diningPoint);

    void deleteById(Long id);
}
