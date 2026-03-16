# 项目规范文档

## 1. 项目概述

本项目是一个基于 Spring Boot 的外卖点餐系统后端服务系统(https://gitee.com/wang-yexi1/sky-take-out/tree/main/)制定。本文档旨在定义项目的技术架构、代码规范、开发流程及最佳实践，以确保代码质量、可维护性和团队协作的一致性,新项目初始化及现有项目迭代请严格遵循本规范。

## 2. 技术架构

### 2.1 技术栈

- **核心框架**: Spring Boot
- **持久层**: MyBatis + MyBatis-Spring-Boot-Starter
- **数据库**: MySQL 5.7+
- **连接池**: Alibaba Druid
- **缓存**: Redis
- **JSON处理**: Jackson / Fastjson
- **工具库**: Lombok, Apache Commons
- **API文档**: Swagger / Knife4j
- **对象存储**: 阿里云 OSS
- **构建工具**: Maven

### 2.2 架构分层

项目采用经典的三层架构模式：

- **Controller 层 (`com.sky.controller`)**: 负责接收 HTTP 请求，参数校验，调用 Service 层，并封装统一返回结果。
- **Service 层 (`com.sky.service`)**: 负责业务逻辑处理，事务控制。
- **DAO/Mapper 层 (`com.sky.mapper`)**: 负责与数据库交互，使用 MyBatis XML 映射文件。

### 2.3 模块划分

- **sky-common**: 公共模块。包含通用的工具类、常量、异常定义、JSON处理、Result 统一返回结果等。
- **sky-pojo**: 数据模型模块。包含 Entity（实体）、DTO（数据传输对象）、VO（视图对象）。
- **sky-server**: 核心业务模块。包含 Controller、Service、Mapper、配置类、启动类等。

## 3. 目录结构与命名规范

### 3.1 目录结构

```text
sky-take-out
├── sky-common          # 公共模块
├── sky-pojo            # 领域模型
└── sky-server          # 核心业务
    ├── src/main/java/com/sky
    │   ├── annotation  # 自定义注解
    │   ├── aspect      # AOP 切面
    │   ├── config      # 配置类
    │   ├── controller  # 控制器 (分 admin/user/notify)
    │   ├── handler     # 全局处理器 (如异常处理)
    │   ├── interceptor # 拦截器
    │   ├── mapper      # DAO 接口
    │   ├── service     # 业务接口
    │   │   └── impl    # 业务实现
    │   ├── task        # 定时任务
    │   └── websocket   # WebSocket 服务
    └── src/main/resources
        ├── mapper      # MyBatis XML 文件
        └── application.yml # 配置文件
```

### 3.2 命名规范

- **类名**: 使用 `PascalCase`（大驼峰），如 `EmployeeController`。
- **方法名/变量名**: 使用 `camelCase`（小驼峰），如 `getEmployeeById`。
- **常量**: 使用 `UPPER_SNAKE_CASE`（全大写下划线），如 `JwtClaimsConstant.EMP_ID`。
- **包名**: 全小写，多级包用点分隔，如 `com.sky.controller.admin`。
- **接口实现类**: 接口名 + `Impl` 后缀，如 `EmployeeServiceImpl`。
- **DTO/VO**: 必须带后缀，如 `EmployeeLoginDTO`, `EmployeeLoginVO`。

## 4. 开发规范与最佳实践

### 4.1 统一 API 响应

所有 API 接口必须返回 `com.sky.result.Result<T>` 对象。

```java
@Data
public class Result<T> implements Serializable {
    private Integer code; // 1:成功, 0:失败
    private String msg;   // 错误信息
    private T data;       // 返回数据
    // ...
}
```

### 4.2 异常处理

- 使用 `@RestControllerAdvice` + `@ExceptionHandler` 进行全局异常捕获。
- 业务异常应抛出 `BaseException` 的子类（如 `AccountNotFoundException`）。
- 禁止在 Controller 层直接吞掉异常，应抛出给全局异常处理器处理。

### 4.3 数据模型使用

- **Entity**: 对应数据库表结构，严禁直接暴露给前端。
- **DTO**: 用于接收前端传递的参数。
- **VO**: 用于向前端返回数据，屏蔽敏感字段（如密码）。
- 禁止在 Controller/Service 层使用 Map 传递参数，必须定义 DTO/VO。

### 4.4 数据库交互

- 优先使用 MyBatis XML 方式编写 SQL，复杂 SQL 避免在注解中编写。
- 数据库字段命名为 `snake_case`，Java 实体属性为 `camelCase`，在 `application.yml` 中开启自动映射：
  ```yaml
  mybatis:
    configuration:
      map-underscore-to-camel-case: true
  ```
- 公共字段（create\_time, update\_time, create\_user, update\_user）使用 AOP (`AutoFillAspect`) 自动填充。

### 4.5 安全规范

- **认证**: 使用 JWT 令牌认证。
- **鉴权**:
  - 管理端接口路径 `/admin/**`，由 `JwtTokenAdminInterceptor` 拦截。
  - 用户端接口路径 `/user/**`，由 `JwtTokenUserInterceptor` 拦截。
- **上下文**: 使用 `ThreadLocal` (`BaseContext`) 存储当前登录用户 ID，确保线程安全。

### 4.6 日志规范

- 使用 `@Slf4j` 注解。
- 关键业务操作（如增删改）必须打印日志。
- 异常捕获时必须打印堆栈信息或错误描述。

## 5. 代码质量与自动化



### 5.1 常用注解

- `@Autowired`: 依赖注入
- `@RestController`: 声明控制器
- `@RequestMapping` / `@GetMapping` / `@PostMapping`: 路径映射
- `@Service`: 声明业务类
- `@Mapper`: 声明 DAO 接口
- `@Transactional`: 声明事务（注意仅在 public 方法生效）
- `@RequestBody`: 接收 JSON 参数
- `@PathVariable`: 接收路径参数

## 6. 配置管理

- **application.yml**: 主配置文件。
- **application-dev.yml**: 开发环境配置。
- **application-prod.yml**: 生产环境配置（需创建）。
- 敏感信息（如数据库密码、密钥）在生产环境中应通过环境变量或配置中心注入，避免明文硬编码。

