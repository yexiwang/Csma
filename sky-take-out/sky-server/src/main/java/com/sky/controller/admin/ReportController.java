package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 数据统计
 */
@RestController//注解表示这个类是一个处理HTTP请求的控制器，并且会自动将方法返回值序列化为JSON格式直接写入HTTP响应体中，用于构建RESTful风格的Web服务。
@RequestMapping("/admin/report")//访问路径
//@Slf4j//日志记录器，用于记录日志信息
@Api(tags = "数据统计相关接口")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计  Query参数
     *
     * @param begin string-开始日期
     * @param end   string-结束日期
     *              业务规则：
     *              营业额指订单状态为已完成的订单金额合计
     *              基于可视化报表的折线图展示营业额数据，X轴为日期，Y轴为营业额
     *              根据时间选择区间，展示每天的营业额数据
     * 虽然前端传来的是 String，但使用 @DateTimeFormat 转换为 LocalDate 是最佳实践
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计：{}到{}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户统计
     *
     * @param begin string-开始日期
     * @param end   string-结束日期
     *              业务规则：
     * 基于可视化报表的折线图展示用户数据，X轴为日期，Y轴为用户数
     * 根据时间选择区间，展示每天的用户总量和新增用户量数据
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：{}到{}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 订单统计
     *
     * @param begin string-开始日期
     * @param end   string-结束日期
     *              业务规则：
     * 有效订单指状态为 “5已完成” 的订单
     * 基于可视化报表的折线图展示订单数据，X轴为日期，Y轴为订单数量
     * 根据时间选择区间，展示每天的订单总数和有效订单数
     * 展示所选时间区间内的有效订单数、总订单数、订单完成率，订单完成率 = 有效订单数 / 总订单数 * 100%
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计：{}到{}", begin, end);
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    /**
     * 查询销量排名top10
     *
     * @param begin string-开始日期
     * @param end   string-结束日期
     *              业务规则：
     * 根据时间选择区间，展示销量前10的商品（包括菜品和套餐）
     * 基于可视化报表的柱状图降序展示商品销量
     * 此处的销量为商品销售的份数
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("查询销量排名top10")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("查询销量排名top10：{}到{}", begin, end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    /**
     * 导出Excel
     *
     * 业务规则：
     * 导出Excel形式的报表文件
     * 导出最近30天的运营数据
     * 当前接口没有返回数据，因为报表导出功能本质上是文件下载，
     * 服务端会通过输出流将Excel文件下载到客户端浏览器
     * @return
     */
    @GetMapping("/export")
    @ApiOperation("导出Excel")
    public void export(HttpServletResponse  response){
        log.info("导出Excel");
        reportService.exportBusinessData(response);
    }
}
