package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.entity.DiningPoint;
import com.sky.mapper.DiningPointMapper;
import com.sky.service.DiningPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiningPointServiceImpl implements DiningPointService {

    @Autowired
    private DiningPointMapper diningPointMapper;

    @Override
    public void save(DiningPoint diningPoint) {
        diningPoint.setStatus(1);
        diningPoint.setCreateTime(LocalDateTime.now());
        diningPoint.setUpdateTime(LocalDateTime.now());
        diningPoint.setCreateUser(BaseContext.getCurrentId());
        diningPoint.setUpdateUser(BaseContext.getCurrentId());
        diningPointMapper.insert(diningPoint);
    }

    @Override
    public Page<DiningPoint> pageQuery(int page, int pageSize, String name, Integer status) {
        PageHelper.startPage(page, pageSize);
        return diningPointMapper.pageQuery(name, status);
    }

    @Override
    public List<DiningPoint> list(Integer status) {
        return diningPointMapper.list(status);
    }

    @Override
    public DiningPoint getById(Long id) {
        return diningPointMapper.getById(id);
    }

    @Override
    public void update(DiningPoint diningPoint) {
        diningPoint.setUpdateTime(LocalDateTime.now());
        diningPoint.setUpdateUser(BaseContext.getCurrentId());
        diningPointMapper.update(diningPoint);
    }

    @Override
    public void deleteById(Long id) {
        diningPointMapper.deleteById(id);
    }
}
