package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.VolunteerDTO;
import com.sky.dto.VolunteerPageQueryDTO;
import com.sky.entity.User;
import com.sky.entity.VolunteerStats;
import com.sky.exception.BaseException;
import com.sky.mapper.VolunteerMapper;
import com.sky.mapper.VolunteerStatsMapper;
import com.sky.result.PageResult;
import com.sky.service.VolunteerService;
import com.sky.vo.VolunteerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VolunteerServiceImpl implements VolunteerService {

    private static final String VOLUNTEER_ROLE = "VOLUNTEER";

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private VolunteerStatsMapper volunteerStatsMapper;

    @Override
    public PageResult pageQuery(VolunteerPageQueryDTO volunteerPageQueryDTO) {
        PageHelper.startPage(volunteerPageQueryDTO.getPage(), volunteerPageQueryDTO.getPageSize());
        Page<VolunteerVO> page = volunteerMapper.pageQuery(volunteerPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void save(VolunteerDTO volunteerDTO) {
        validateCreate(volunteerDTO);

        User existingUser = volunteerMapper.getByUsername(volunteerDTO.getUsername().trim());
        if (existingUser != null) {
            throw new BaseException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User volunteer = User.builder()
                .username(volunteerDTO.getUsername().trim())
                .password(DigestUtils.md5DigestAsHex(volunteerDTO.getPassword().trim().getBytes()))
                .role(VOLUNTEER_ROLE)
                .status(normalizeStatus(volunteerDTO.getStatus(), StatusConstant.ENABLE))
                .name(volunteerDTO.getName().trim())
                .phone(volunteerDTO.getPhone().trim())
                .createTime(now)
                .updateTime(now)
                .build();
        volunteerMapper.insert(volunteer);

        VolunteerStats volunteerStats = VolunteerStats.builder()
                .userId(volunteer.getId())
                .totalOrders(0)
                .totalHours(new BigDecimal("0.0"))
                .rating(new BigDecimal("5.0"))
                .level(1)
                .createTime(now)
                .updateTime(now)
                .build();
        volunteerStatsMapper.insert(volunteerStats);
    }

    @Override
    public void update(VolunteerDTO volunteerDTO) {
        if (volunteerDTO.getId() == null) {
            throw new BaseException("志愿者ID不能为空");
        }
        if (!StringUtils.hasText(volunteerDTO.getName())) {
            throw new BaseException("姓名不能为空");
        }
        if (!StringUtils.hasText(volunteerDTO.getPhone())) {
            throw new BaseException("手机号不能为空");
        }

        User volunteer = requireVolunteer(volunteerDTO.getId());
        volunteer.setName(volunteerDTO.getName().trim());
        volunteer.setPhone(volunteerDTO.getPhone().trim());
        if (volunteerDTO.getStatus() != null) {
            volunteer.setStatus(normalizeStatus(volunteerDTO.getStatus(), volunteer.getStatus()));
        }
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.update(volunteer);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BaseException("志愿者ID不能为空");
        }

        User volunteer = requireVolunteer(id);
        volunteer.setStatus(normalizeStatus(status, volunteer.getStatus()));
        volunteer.setUpdateTime(LocalDateTime.now());
        volunteerMapper.update(volunteer);
    }

    @Override
    public VolunteerVO getById(Long id) {
        if (id == null) {
            throw new BaseException("志愿者ID不能为空");
        }

        VolunteerVO volunteerVO = volunteerMapper.getVolunteerById(id);
        if (volunteerVO == null) {
            throw new BaseException("志愿者不存在");
        }
        return volunteerVO;
    }

    @Override
    public List<VolunteerVO> listActiveVolunteers() {
        return volunteerMapper.listActiveVolunteers();
    }

    private void validateCreate(VolunteerDTO volunteerDTO) {
        if (!StringUtils.hasText(volunteerDTO.getUsername())) {
            throw new BaseException("用户名不能为空");
        }
        if (!StringUtils.hasText(volunteerDTO.getPassword())) {
            throw new BaseException("密码不能为空");
        }
        if (!StringUtils.hasText(volunteerDTO.getName())) {
            throw new BaseException("姓名不能为空");
        }
        if (!StringUtils.hasText(volunteerDTO.getPhone())) {
            throw new BaseException("手机号不能为空");
        }
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) {
        Integer targetStatus = status == null ? defaultStatus : status;
        if (targetStatus == null) {
            targetStatus = StatusConstant.ENABLE;
        }
        if (!StatusConstant.ENABLE.equals(targetStatus) && !StatusConstant.DISABLE.equals(targetStatus)) {
            throw new BaseException("志愿者状态不合法");
        }
        return targetStatus;
    }

    private User requireVolunteer(Long id) {
        User volunteer = volunteerMapper.getVolunteerEntityById(id);
        if (volunteer == null) {
            throw new BaseException("志愿者不存在");
        }
        return volunteer;
    }
}
