package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ElderlyDTO;
import com.sky.entity.DiningPoint;
import com.sky.entity.Elderly;
import com.sky.exception.BaseException;
import com.sky.mapper.DiningPointMapper;
import com.sky.mapper.ElderlyMapper;
import com.sky.result.PageResult;
import com.sky.service.ElderlyService;
import com.sky.vo.ElderlyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElderlyServiceImpl implements ElderlyService {

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private DiningPointMapper diningPointMapper;

    @Override
    public void save(ElderlyDTO elderlyDTO) {
        Elderly elderly = new Elderly();
        BeanUtils.copyProperties(elderlyDTO, elderly);
        if (elderly.getUserId() == null) {
            elderly.setUserId(BaseContext.getCurrentId());
        }
        validateDiningPoint(elderly.getDiningPointId());
        elderly.setCreateTime(LocalDateTime.now());
        elderly.setUpdateTime(LocalDateTime.now());
        elderlyMapper.insert(elderly);
    }

    @Override
    public PageResult pageQuery(int page, int pageSize, String name) {
        PageHelper.startPage(page, pageSize);
        Elderly elderly = new Elderly();
        elderly.setName(name);
        Page<ElderlyVO> pageResult = elderlyMapper.pageQueryWithDiningPoint(elderly);
        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    public List<Elderly> getByUserId(Long userId) {
        return elderlyMapper.getByUserId(userId);
    }

    @Override
    public ElderlyVO getById(Long id) {
        ElderlyVO elderlyVO = elderlyMapper.getDetailById(id);
        if (elderlyVO == null) {
            throw new BaseException("老人档案不存在");
        }
        return elderlyVO;
    }

    @Override
    public void update(ElderlyDTO elderlyDTO) {
        if (elderlyDTO.getId() == null) {
            throw new BaseException("老人档案ID不能为空");
        }
        if (elderlyMapper.getById(elderlyDTO.getId()) == null) {
            throw new BaseException("老人档案不存在");
        }
        Elderly elderly = new Elderly();
        BeanUtils.copyProperties(elderlyDTO, elderly);
        validateDiningPoint(elderly.getDiningPointId());
        elderly.setUpdateTime(LocalDateTime.now());
        elderlyMapper.update(elderly);
    }

    @Override
    public void deleteById(Long id) {
        elderlyMapper.deleteById(id);
    }

    private void validateDiningPoint(Long diningPointId) {
        if (diningPointId == null) {
            throw new BaseException("所属助餐点不能为空");
        }
        DiningPoint diningPoint = diningPointMapper.getById(diningPointId);
        if (diningPoint == null) {
            throw new BaseException("所属助餐点不存在");
        }
        if (!StatusConstant.ENABLE.equals(diningPoint.getStatus())) {
            throw new BaseException("所属助餐点已停用，请重新选择");
        }
    }
}
