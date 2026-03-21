package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.VolunteerDTO;
import com.sky.dto.VolunteerPageQueryDTO;
import com.sky.entity.User;
import com.sky.entity.VolunteerStats;
import com.sky.exception.BaseException;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.VolunteerMapper;
import com.sky.mapper.VolunteerStatsMapper;
import com.sky.result.PageResult;
import com.sky.service.VolunteerService;
import com.sky.service.support.VolunteerLevelSupport;
import com.sky.vo.VolunteerOverviewVO;
import com.sky.vo.VolunteerVO;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VolunteerServiceImpl implements VolunteerService {

    private static final String VOLUNTEER_ROLE = "VOLUNTEER";
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter EXPORT_FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private VolunteerStatsMapper volunteerStatsMapper;

    @Autowired
    private OrderMapper orderMapper;

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
                .rating(null)
                .level(VolunteerLevelSupport.calculateLevel(0))
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

    @Override
    public VolunteerOverviewVO getCurrentOverview() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        if (!VOLUNTEER_ROLE.equalsIgnoreCase(BaseContext.getCurrentRole())) {
            throw new BaseException("当前角色无权查看志愿者概览");
        }

        User volunteer = requireVolunteer(currentUserId);
        VolunteerStats volunteerStats = volunteerStatsMapper.getByUserId(currentUserId);
        Integer completedOrders = orderMapper.countByMap(buildCompletedOrderCountMap(currentUserId));

        int statsTotalOrders = volunteerStats != null && volunteerStats.getTotalOrders() != null
                ? volunteerStats.getTotalOrders()
                : 0;
        int completedOrderCount = completedOrders == null ? 0 : completedOrders;
        int effectiveTotalOrders = Math.max(statsTotalOrders, completedOrderCount);

        return VolunteerOverviewVO.builder()
                .name(volunteer.getName())
                .status(volunteer.getStatus())
                .totalOrders(effectiveTotalOrders)
                .totalHours(volunteerStats != null && volunteerStats.getTotalHours() != null ? volunteerStats.getTotalHours() : BigDecimal.ZERO)
                .rating(volunteerStats == null ? null : volunteerStats.getRating())
                .level(VolunteerLevelSupport.calculateLevel(effectiveTotalOrders))
                .build();
    }

    @Override
    public void exportCurrentOverview(HttpServletResponse response) {
        VolunteerOverviewVO overview = getCurrentOverview();
        LocalDateTime now = LocalDateTime.now();
        String fileName = "志愿者个人概览_" + EXPORT_FILE_TIME_FORMATTER.format(now) + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", buildAttachmentHeader(fileName));

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("个人概览");
            writeOverviewHeader(sheet, workbook);
            writeOverviewData(sheet, overview, now);
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new BaseException("导出志愿者个人概览失败");
        }
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

    private Map<String, Object> buildCompletedOrderCountMap(Long volunteerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("volunteerId", volunteerId);
        map.put("status", 6);
        return map;
    }

    private void writeOverviewHeader(XSSFSheet sheet, XSSFWorkbook workbook) {
        String[] headers = {
                "志愿者姓名",
                "当前服务状态",
                "累计服务单量",
                "累计服务时长（小时）",
                "综合评分",
                "等级",
                "导出时间"
        };

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
            headerRow.getCell(i).setCellStyle(headerStyle);
            sheet.setColumnWidth(i, i == 0 ? 20 * 256 : 22 * 256);
        }
    }

    private void writeOverviewData(XSSFSheet sheet, VolunteerOverviewVO overview, LocalDateTime exportTime) {
        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(overview.getName() == null ? "--" : overview.getName());
        dataRow.createCell(1).setCellValue(resolveVolunteerStatusText(overview.getStatus()));
        dataRow.createCell(2).setCellValue(overview.getTotalOrders() == null ? 0 : overview.getTotalOrders());
        dataRow.createCell(3).setCellValue(formatDecimal(overview.getTotalHours()));
        dataRow.createCell(4).setCellValue(formatDecimal(overview.getRating()));
        dataRow.createCell(5).setCellValue(formatLevel(overview.getLevel()));
        dataRow.createCell(6).setCellValue(EXPORT_TIME_FORMATTER.format(exportTime));
    }

    private String resolveVolunteerStatusText(Integer status) {
        if (StatusConstant.ENABLE.equals(status)) {
            return "启用";
        }
        if (StatusConstant.DISABLE.equals(status)) {
            return "停用";
        }
        return "--";
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "--" : value.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatLevel(Integer level) {
        return level == null ? "--" : "Lv." + level;
    }

    private String buildAttachmentHeader(String fileName) {
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            encodedFileName = fileName;
        }
        return "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }

    private User requireVolunteer(Long id) {
        User volunteer = volunteerMapper.getVolunteerEntityById(id);
        if (volunteer == null) {
            throw new BaseException("志愿者不存在");
        }
        return volunteer;
    }
}
