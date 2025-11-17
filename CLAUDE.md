# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个Minecraft 1.20.X Forge模组开发课程项目，包含自定义方块、物品、工具、盔甲等功能。主要开发模组名为"MC Course Mod"，添加了紫水晶（Alexandrite）相关的游戏内容。

## 构建命令

- **构建模组**: `./gradlew build`
- **运行客户端**: `./gradlew runClient`
- **运行服务器**: `./gradlew runServer`
- **生成数据**: `./gradlew runData`
- **运行游戏测试**: `./gradlew runGameTestServer`
- **清理构建**: `./gradlew clean`

## 开发环境配置

- Java 17（项目要求）
- Minecraft版本: 1.20.1
- Forge版本: 47.0.1
- 映射: Parchment 1.19.3-2023.03.12-1.20.1
- 模组ID: `mccourse`
- 基础包: `net.kaupenjoe.mccourse`

## 代码架构

### 核心模组文件
- `MCCourseMod.java`: 主模组类，处理模组初始化和事件总线注册
- 模组ID: `mccourse`，位于包 `net.kaupenjoe.mccourse`

### 主要包结构
- `block/`: 自定义方块注册和实现
  - `ModBlocks.java`: 统一管理所有自定义方块注册
  - `custom/`: 自定义方块实现（如AlexandriteLampBlock, SoundBlock）
- `item/`: 自定义物品、工具、盔甲
  - `ModItems.java`: 统一管理所有自定义物品注册
  - `custom/`: 自定义物品实现（工具、盔甲等）
- `datagen/`: 数据生成器，自动生成JSON文件
  - `DataGenerators.java`: 数据生成事件处理
  - 包含方块状态、物品模型、配方、战利品表生成器
- `network/`: 网络数据包处理
- `talent/`: 天赋系统
- `battleroyale/`: 大逃杀模式功能
- `enchantment/`: 自定义附魔

### 数据生成系统
项目使用Forge的数据生成系统自动生成资源文件：
- 方块状态和模型
- 物品模型
- 配方文件
- 战利品表
- 标签文件

生成的文件位于 `src/generated/resources/`

### 资源文件结构
- `assets/mccourse/`: 客户端资源
  - `blockstates/`: 方块状态JSON
  - `models/`: 3D模型文件
  - `textures/`: 纹理文件
  - `lang/`: 本地化文件
- `data/mccourse/`: 服务端数据
  - `recipes/`: 合成配方
  - `loot_tables/`: 战利品表
  - `tags/`: 物品/方块标签

### 特殊功能模块
- **天赋系统**: 自定义RPG天赋树界面
- **大逃杀模式**: 游戏模式管理和命令系统
- **自定义工具**: Paxel（三合一工具）、Hammer（锤子）等
- **金属探测器**: 可以探测附近矿物的自定义物品
- **自定义附魔**: Lightning Striker等

## 开发注意事项

- 使用DeferredRegister模式注册游戏对象
- 数据生成器会在runData任务时自动更新生成的JSON文件
- 自定义方块需要同时注册方块和对应的BlockItem
- 网络包使用ModMessages系统进行客户端-服务端通信
- 项目支持多种运行配置（客户端、服务端、数据生成、游戏测试）