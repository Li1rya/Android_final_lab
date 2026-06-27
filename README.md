### 项目名称：
动物科普安卓应用

### 简介：
一款面向安卓平台的轻量动物科普工具类 APP，以文字搜索 + 图像识别为核心能力，为用户提供权威、简洁、易读的动物知识服务。

###  运行环境要求
 最低 Android 版本：Android 7.0
 
 目标适配版本：Android 15
 
 开发语言：Kotlin
 
 UI框架：Jetpack Compose

 ### APK 下载链结
 https://github.com/Li1rya/Android_final_lab/releases/tag/v1.0.0

 ### 项目目录结构说明
| 层级      | 包/目录                    | 说明                       |
| ------- | ----------------------- | ------------------------ |
| 数据层     | `data/`                 | 负责所有数据的获取与存储             |
| 实体层     | `database/entity/`      | 定义数据库表结构（`@Entity`）      |
| 访问层     | `database/*Dao.kt`      | 定义数据库操作接口（`@Dao`）        |
| 构建层     | `database/*Database.kt` | 构建Room数据库实例（`@Database`） |
| 网络层     | `network/`              | Retrofit接口、API服务         |
| 仓库层     | `repository/`           | 整合多数据源，对外提供统一数据接口        |
| UI层      | `ui/`                   | 负责界面展示与用户交互              |
| 界面层     | `screens/`              | Compose页面（按功能模块划分）       |
| 主题层     | `theme/`                | Material Design 3主题定制    |
| 状态层     | `viewmodel/`            | 管理UI状态，处理业务逻辑            |

 ### 核心功能截图
 <p align="left">
  <img width="200" alt="Screenshot_20260627_112914" src="https://github.com/user-attachments/assets/1de0e3ed-f4f1-40af-8299-8cbe30ce74af" />
  <img width="200" alt="Screenshot_20260627_112930" src="https://github.com/user-attachments/assets/74a006b3-b1b9-475f-93d0-b00df970c3b8" />
  <img width="200" alt="Screenshot_20260627_113000" src="https://github.com/user-attachments/assets/6417389e-242e-46a0-a09f-d82d29696a59" />
  <img width="200" alt="Screenshot_20260627_113016" src="https://github.com/user-attachments/assets/4193c814-ced4-4e06-a651-c0fc07d6f37c" />
 </p>

