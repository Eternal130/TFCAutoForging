# TFCAutoForging 更新说明

本 Release 包含全部支持版本的构建产物，请根据所用 Minecraft / TerraFirmaCraft 版本选择对应 jar：

| 文件 | MC 版本 | TFC 版本 |
|---|---|---|
| `tfcaf-tfcplus-1.7.10-*.jar` | 1.7.10 | TFC+ (TerraFirmaCraftPlus) |
| `tfcaf-tfc-1.7.10-*.jar` | 1.7.10 | TFC Classic 0.79 |
| `tfcaf-tfctng-1.12.2-*.jar` | 1.12.2 | TFC TNG |
| `tfcaf-tfctng-1.18.2-*.jar` | 1.18.2 | TFC TNG |
| `tfcaf-tfctng-1.20.1-*.jar` | 1.20.1 | TFC TNG |
| `tfcaf-tfctng-1.21.1-*.jar` | 1.21.1 | TFC TNG (NeoForge) |

## 主要变更（各版本同步）

- **发包模式升级为一次性连发**：锻造步骤一次性发包完成，显著改善高延迟环境下的表现
- **炸砧防护**：连发模式下增加防护，避免误操作炸砧
- **服务器响应检测**：自动锻造不再因网络延迟而出错，含等待超时兜底
- **单件完工停机**：完成单件锻造后自动停止，支持自动连锻场景

## 其他改进

- 构建系统全面升级（GTNH 2.0.29 / Gradle / Kotlin DSL），发布产物统一由 CI 构建
- 修复部分种子下锻造崩溃的问题
- 修复锻造配方中出现多个任意步骤时指引失效的问题
- 修复服务器响应检测卡死的问题
