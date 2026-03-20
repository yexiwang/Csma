package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.FamilyProfilePageQueryDTO;
import com.sky.entity.FamilyProfile;
import com.sky.enumeration.OperationType;
import com.sky.vo.FamilyProfileOptionVO;
import com.sky.vo.FamilyProfileVO;
import com.sky.vo.FamilyUserOptionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FamilyProfileMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(FamilyProfile familyProfile);

    @AutoFill(value = OperationType.UPDATE)
    void update(FamilyProfile familyProfile);

    Page<FamilyProfileVO> pageQuery(FamilyProfilePageQueryDTO familyProfilePageQueryDTO);

    FamilyProfileVO getDetailById(Long id);

    FamilyProfile getById(Long id);

    FamilyProfile getByUserId(Long userId);

    FamilyProfile getEnabledByUserId(Long userId);

    List<FamilyProfileOptionVO> listEnabledOptions();

    List<FamilyUserOptionVO> listFamilyUsers();
}
