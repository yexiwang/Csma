package com.sky.service;

import com.sky.dto.ElderlyDTO;
import com.sky.entity.Elderly;
import com.sky.result.PageResult;
import com.sky.vo.ElderlyVO;

import java.util.List;

public interface ElderlyService {
    void save(ElderlyDTO elderlyDTO);

    PageResult pageQuery(int page, int pageSize, String name, Long diningPointId);

    List<Elderly> getByUserId(Long userId);

    ElderlyVO getById(Long id);

    void update(ElderlyDTO elderlyDTO);

    void deleteById(Long id);
}
