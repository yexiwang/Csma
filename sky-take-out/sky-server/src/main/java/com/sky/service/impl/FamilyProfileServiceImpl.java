package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.FamilyProfileDTO;
import com.sky.dto.FamilyProfilePageQueryDTO;
import com.sky.entity.FamilyProfile;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.ElderlyMapper;
import com.sky.mapper.FamilyProfileMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.FamilyProfileService;
import com.sky.vo.FamilyProfileOptionVO;
import com.sky.vo.FamilyProfileVO;
import com.sky.vo.FamilyUserOptionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FamilyProfileServiceImpl implements FamilyProfileService {

    private static final String ROLE_FAMILY = "FAMILY";

    @Autowired
    private FamilyProfileMapper familyProfileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ElderlyMapper elderlyMapper;

    @Override
    public PageResult pageQuery(FamilyProfilePageQueryDTO familyProfilePageQueryDTO) {
        PageHelper.startPage(familyProfilePageQueryDTO.getPage(), familyProfilePageQueryDTO.getPageSize());
        Page<FamilyProfileVO> page = familyProfileMapper.pageQuery(familyProfilePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public FamilyProfileVO getById(Long id) {
        if (id == null) {
            throw new BaseException("家属档案ID不能为空");
        }
        FamilyProfileVO familyProfileVO = familyProfileMapper.getDetailById(id);
        if (familyProfileVO == null) {
            throw new BaseException("家属档案不存在");
        }
        return familyProfileVO;
    }

    @Override
    @Transactional
    public void save(FamilyProfileDTO familyProfileDTO) {
        String familyName = normalizeRequiredField(familyProfileDTO.getName(), "家属姓名不能为空");
        Long userId = resolveUserIdForSave(familyProfileDTO);
        LocalDateTime now = LocalDateTime.now();

        FamilyProfile familyProfile = FamilyProfile.builder()
                .userId(userId)
                .remark(normalizeOptionalField(familyProfileDTO.getRemark()))
                .status(normalizeStatus(familyProfileDTO.getStatus(), StatusConstant.ENABLE))
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        familyProfileMapper.insert(familyProfile);

        syncUserProfile(userId, familyName, normalizePhoneForUser(familyProfileDTO.getPhone()));
    }

    @Override
    public void update(FamilyProfileDTO familyProfileDTO) {
        if (familyProfileDTO.getId() == null) {
            throw new BaseException("家属档案ID不能为空");
        }
        if (Boolean.TRUE.equals(familyProfileDTO.getCreateUser())) {
            throw new BaseException("编辑家属档案时不支持同步创建新的FAMILY账号");
        }
        FamilyProfile current = requireProfile(familyProfileDTO.getId());

        FamilyProfile familyProfile = new FamilyProfile();
        familyProfile.setId(current.getId());

        Long targetUserId = familyProfileDTO.getUserId() == null ? current.getUserId() : familyProfileDTO.getUserId();
        requireFamilyUser(targetUserId);
        ensureUserIdAvailable(targetUserId, current.getId());
        familyProfile.setUserId(targetUserId);

        String targetName = null;
        if (familyProfileDTO.getName() != null) {
            targetName = normalizeRequiredField(familyProfileDTO.getName(), "家属姓名不能为空");
        }
        if (familyProfileDTO.getPhone() != null) {
            normalizePhoneForUser(familyProfileDTO.getPhone());
        }
        if (familyProfileDTO.getRemark() != null) {
            familyProfile.setRemark(normalizeOptionalField(familyProfileDTO.getRemark()));
        }
        if (familyProfileDTO.getStatus() != null) {
            familyProfile.setStatus(normalizeStatus(familyProfileDTO.getStatus(), current.getStatus()));
        }
        familyProfile.setUpdateTime(LocalDateTime.now());

        familyProfileMapper.update(familyProfile);
        if (targetName != null || familyProfileDTO.getPhone() != null) {
            User currentUser = requireFamilyUser(targetUserId);
            syncUserProfile(targetUserId,
                    targetName != null ? targetName : currentUser.getName(),
                    familyProfileDTO.getPhone() != null ? normalizePhoneForUser(familyProfileDTO.getPhone()) : currentUser.getPhone());
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BaseException("家属档案ID不能为空");
        }
        FamilyProfile current = requireProfile(id);
        FamilyProfile familyProfile = FamilyProfile.builder()
                .id(id)
                .status(normalizeStatus(status, current.getStatus()))
                .updateTime(LocalDateTime.now())
                .build();
        familyProfileMapper.update(familyProfile);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new BaseException("家属档案ID不能为空");
        }
        FamilyProfile familyProfile = requireProfile(id);
        Integer elderlyCount = elderlyMapper.countActiveByUserId(familyProfile.getUserId());
        if (elderlyCount != null && elderlyCount > 0) {
            throw new DeletionNotAllowedException("家属档案已关联老人，不能删除");
        }
        familyProfileMapper.update(FamilyProfile.builder()
                .id(id)
                .isDeleted(1)
                .updateTime(LocalDateTime.now())
                .build());
    }

    @Override
    public List<FamilyProfileOptionVO> listEnabledOptions() {
        return familyProfileMapper.listEnabledOptions();
    }

    @Override
    public List<FamilyUserOptionVO> listFamilyUsers() {
        return familyProfileMapper.listFamilyUsers();
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BaseException("家属关联的FAMILY用户不能为空");
        }
        return userId;
    }

    private Long resolveUserIdForSave(FamilyProfileDTO familyProfileDTO) {
        if (Boolean.TRUE.equals(familyProfileDTO.getCreateUser())) {
            return createFamilyUser(familyProfileDTO);
        }

        Long userId = requireUserId(familyProfileDTO.getUserId());
        requireFamilyUser(userId);
        ensureUserIdAvailable(userId, null);
        return userId;
    }

    private User requireFamilyUser(Long userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("关联的FAMILY用户不存在");
        }
        if (!ROLE_FAMILY.equalsIgnoreCase(user.getRole())) {
            throw new BaseException("只能绑定角色为FAMILY的用户");
        }
        return user;
    }

    private void ensureUserIdAvailable(Long userId, Long currentId) {
        FamilyProfile existed = familyProfileMapper.getByUserId(userId);
        if (existed != null && (currentId == null || !existed.getId().equals(currentId))) {
            throw new BaseException("该FAMILY用户已存在家属档案记录");
        }
    }

    private FamilyProfile requireProfile(Long id) {
        FamilyProfile familyProfile = familyProfileMapper.getById(id);
        if (familyProfile == null) {
            throw new BaseException("家属档案不存在");
        }
        return familyProfile;
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) {
        Integer targetStatus = status == null ? defaultStatus : status;
        if (!StatusConstant.ENABLE.equals(targetStatus) && !StatusConstant.DISABLE.equals(targetStatus)) {
            throw new BaseException("家属档案状态不合法");
        }
        return targetStatus;
    }

    private Long createFamilyUser(FamilyProfileDTO familyProfileDTO) {
        String username = normalizeRequiredField(familyProfileDTO.getUsername(), "新建FAMILY账号时用户名不能为空");
        String password = normalizeRequiredField(familyProfileDTO.getPassword(), "新建FAMILY账号时密码不能为空");

        if (userMapper.getByUsername(username) != null) {
            throw new BaseException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(username)
                .password(DigestUtils.md5DigestAsHex(password.getBytes()))
                .role(ROLE_FAMILY)
                .status(normalizeStatus(familyProfileDTO.getStatus(), StatusConstant.ENABLE))
                .name(normalizeRequiredField(familyProfileDTO.getName(), "家属姓名不能为空"))
                .phone(normalizePhoneForUser(normalizeOptionalField(familyProfileDTO.getPhone())))
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        return user.getId();
    }

    private String normalizeOptionalField(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String normalizeRequiredField(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BaseException(errorMessage);
        }
        return value.trim();
    }

    private void syncUserProfile(Long userId, String name, String phone) {
        User user = User.builder()
                .id(userId)
                .name(name)
                .phone(normalizePhoneForUser(phone))
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.updateProfileInfo(user);
    }

    private String normalizePhoneForUser(String phone) {
        String normalizedPhone = normalizeOptionalField(phone);
        if (!StringUtils.hasText(normalizedPhone)) {
            return null;
        }
        if (normalizedPhone.length() > 11) {
            throw new BaseException("联系电话长度不能超过11位");
        }
        return normalizedPhone;
    }
}
