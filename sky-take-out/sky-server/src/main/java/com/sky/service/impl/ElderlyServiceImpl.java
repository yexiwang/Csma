package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.ElderlyDTO;
import com.sky.entity.DiningPoint;
import com.sky.entity.Elderly;
import com.sky.entity.FamilyProfile;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.DiningPointMapper;
import com.sky.mapper.ElderlyMapper;
import com.sky.mapper.FamilyProfileMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.ElderlyService;
import com.sky.vo.ElderlyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElderlyServiceImpl implements ElderlyService {

    private static final String ROLE_FAMILY = "FAMILY";

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Autowired
    private DiningPointMapper diningPointMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyProfileMapper familyProfileMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void save(ElderlyDTO elderlyDTO) {
        validateElderlyInput(elderlyDTO);

        Elderly elderly = new Elderly();
        BeanUtils.copyProperties(elderlyDTO, elderly);
        validateFamilyBinding(elderly.getUserId());
        validateDiningPoint(elderly.getDiningPointId());
        elderly.setCreateTime(LocalDateTime.now());
        elderly.setUpdateTime(LocalDateTime.now());
        elderly.setIsDeleted(0);
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
        if (id == null) {
            throw new BaseException("老人档案ID不能为空");
        }
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
        validateElderlyInput(elderlyDTO);
        if (elderlyMapper.getById(elderlyDTO.getId()) == null) {
            throw new BaseException("老人档案不存在");
        }

        Elderly elderly = new Elderly();
        BeanUtils.copyProperties(elderlyDTO, elderly);
        validateFamilyBinding(elderly.getUserId());
        validateDiningPoint(elderly.getDiningPointId());
        elderly.setUpdateTime(LocalDateTime.now());
        elderlyMapper.update(elderly);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new BaseException("老人档案ID不能为空");
        }
        if (elderlyMapper.getById(id) == null) {
            throw new BaseException("老人档案不存在");
        }
        Integer orderCount = orderMapper.countByElderId(id);
        if (orderCount != null && orderCount > 0) {
            throw new BaseException("老人档案已有关联订单，不能删除");
        }
        elderlyMapper.deleteById(id, LocalDateTime.now());
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

    private void validateElderlyInput(ElderlyDTO elderlyDTO) {
        if (elderlyDTO.getUserId() == null) {
            throw new BaseException("老人档案必须显式绑定FAMILY用户");
        }
        if (!StringUtils.hasText(elderlyDTO.getName())) {
            throw new BaseException("老人姓名不能为空");
        }
    }

    private void validateFamilyBinding(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("关联的FAMILY用户不存在");
        }
        if (!ROLE_FAMILY.equalsIgnoreCase(user.getRole())) {
            throw new BaseException("老人档案只能绑定角色为FAMILY的用户");
        }
        if (!StatusConstant.ENABLE.equals(user.getStatus())) {
            throw new BaseException("关联的FAMILY账号已停用，请重新选择");
        }
        FamilyProfile familyProfile = familyProfileMapper.getEnabledByUserId(userId);
        if (familyProfile == null) {
            throw new BaseException("关联的家属档案不存在或未启用");
        }
    }
}
