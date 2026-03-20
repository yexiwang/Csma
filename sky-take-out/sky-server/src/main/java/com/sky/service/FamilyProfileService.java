package com.sky.service;

import com.sky.dto.FamilyProfileDTO;
import com.sky.dto.FamilyProfilePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.FamilyProfileOptionVO;
import com.sky.vo.FamilyProfileVO;
import com.sky.vo.FamilyUserOptionVO;

import java.util.List;

public interface FamilyProfileService {

    PageResult pageQuery(FamilyProfilePageQueryDTO familyProfilePageQueryDTO);

    FamilyProfileVO getById(Long id);

    void save(FamilyProfileDTO familyProfileDTO);

    void update(FamilyProfileDTO familyProfileDTO);

    void startOrStop(Integer status, Long id);

    void deleteById(Long id);

    List<FamilyProfileOptionVO> listEnabledOptions();

    List<FamilyUserOptionVO> listFamilyUsers();
}
