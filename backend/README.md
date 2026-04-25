# 诗韵雅集后端服务

## 技术栈
- Spring Boot 3.2+
- MySQL 8.0+
- MyBatis-Plus
- Spring Web
- Spring Data JPA
- Maven

## 项目结构

```
backend-spring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── poemweb/
│   │   │           ├── PoemWebApplication.java        # 应用启动类
│   │   │           ├── common/                        # 公共组件
│   │   │           │   ├── exception/                 # 异常处理
│   │   │           │   ├── response/                 # 统一响应
│   │   │           │   └── utils/                    # 工具类
│   │   │           ├── config/                       # 配置类
│   │   │           │   ├── MyBatisConfig.java        # MyBatis配置
│   │   │           │   └── WebConfig.java            # Web配置
│   │   │           ├── controller/                   # 控制器
│   │   │           │   ├── PoemController.java       # 诗词相关接口
│   │   │           │   ├── QAController.java         # 智能问答接口
│   │   │           │   ├── FeihualingController.java # 飞花令接口
│   │   │           │   ├── ReciteController.java      # 背诵默写接口
│   │   │           │   ├── NavController.java         # 分类导航接口
│   │   │           │   ├── GraphController.java       # 知识图谱接口
│   │   │           │   └── TrajectoryController.java  # 诗人行迹接口
│   │   │           ├── service/                      # 服务层
│   │   │           │   ├── PoemService.java          # 诗词服务
│   │   │           │   ├── QAService.java            # 智能问答服务
│   │   │           │   ├── FeihualingService.java    # 飞花令服务
│   │   │           │   ├── ReciteService.java         # 背诵默写服务
│   │   │           │   ├── NavService.java            # 分类导航服务
│   │   │           │   ├── GraphService.java          # 知识图谱服务
│   │   │           │   └── TrajectoryService.java     # 诗人行迹服务
│   │   │           ├── mapper/                       # 数据访问层
│   │   │           │   ├── PoemMapper.java           # 诗词Mapper
│   │   │           │   ├── AuthorMapper.java         # 作者Mapper
│   │   │           │   ├── DynastyMapper.java        # 朝代Mapper
│   │   │           │   └── TagMapper.java            # 题材Mapper
│   │   │           └── entity/                       # 实体类
│   │   │               ├── Poem.java                 # 诗词实体
│   │   │               ├── Author.java               # 作者实体
│   │   │               ├── Dynasty.java              # 朝代实体
│   │   │               └── Tag.java                  # 题材实体
│   │   ├── resources/
│   │   │   ├── application.yml                      # 应用配置
│   │   │   ├── mapper/                              # MyBatis映射文件
│   │   │   └── static/                              # 静态资源
│   │   └── webapp/                                  # Web资源
│   │       └── WEB-INF/
│   └── test/                                         # 测试代码
├── pom.xml                                           # Maven配置
└── data-migration/                                    # 数据迁移工具
    ├── DataMigration.java                             # 数据迁移主类
    └── utils/                                         # 数据迁移工具类
```

## 数据库表结构

### `poem` 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|-----|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 诗词ID |
| `title` | `VARCHAR(255)` | `NOT NULL` | 诗词标题 |
| `author_id` | `BIGINT` | `FOREIGN KEY REFERENCES author(id)` | 作者ID |
| `dynasty_id` | `BIGINT` | `FOREIGN KEY REFERENCES dynasty(id)` | 朝代ID |
| `tag_id` | `BIGINT` | `FOREIGN KEY REFERENCES tag(id)` | 题材ID |
| `content` | `TEXT` | `NOT NULL` | 诗词内容 |
| `annotation` | `TEXT` | | 注释 |
| `translation` | `TEXT` | | 译文 |
| `background` | `TEXT` | | 创作背景 |
| `emotion` | `TEXT` | | 情感主旨 |
| `allusion` | `TEXT` | | 典故意象 |
| `create_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### `author` 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|-----|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 作者ID |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 作者姓名 |
| `create_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### `dynasty` 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|-----|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 朝代ID |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 朝代名称 |
| `create_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### `tag` 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|-----|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 题材ID |
| `name` | `VARCHAR(100)` | `NOT NULL UNIQUE` | 题材名称 |
| `create_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

## 数据迁移

1. 运行 `data-migration/DataMigration.java` 类，将 `data/poems.json` 中的数据导入到 MySQL 数据库
2. 确保数据库连接配置正确

## 启动服务

1. 配置 `application.yml` 中的数据库连接信息
2. 运行 `PoemWebApplication.java` 启动服务
3. 服务默认运行在 `http://localhost:8080`

## API接口

所有API接口保持与原Flask后端一致，确保前端无需修改即可正常运行。
