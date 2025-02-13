# F50Client

更好的管理你的F50！😘

## 目录

- [项目介绍](#项目介绍)
- [功能特性](#功能特性)
- [安装和配置](#安装和配置)
- [使用说明](#使用说明)
- [贡献](#贡献)
- [许可证](#许可证)

## 项目介绍

本项目为Kotlin Multiplatform项目，可在Android和iOS上使用。本客户端旨在弥补官方在短信提醒和消息推送等方面的欠缺。

## 功能特性

- [x] 支持消息的查看
- [x] 用户自定义设置，如主题切换、后台地址
- [ ] 支持通知提醒
- [ ] 支持状态的查看
- [ ] 支持管理F50的设置

## 安装和配置

### 环境要求

- Android 5.0 及以上

### 安装步骤

1. 克隆仓库：

   ```bash
   git clone https://github.com/RUSHHE/F50Client.git
自行编译即可

2. 从release页下载：
   
This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
