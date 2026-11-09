# 贡献指南 / Contributing

## 提交前

1. 用 **JDK 21** 构建：`gradlew build`
2. 确保 `build` 通过，没有编译错误
3. 如果改了配置项，**同时更新**：
   - `FirstAidConfig.java` 里的 `.translation(...)`
   - `assets/firstaid/lang/en_us.json`
   - `assets/firstaid/lang/zh_cn.json`

## 代码风格

- 4 空格缩进，UTF-8，LF
- 新增类请加简短 Javadoc，说明**为什么**这样做（尤其是绕过原版行为的代码）
- Mixin 注入点尽量写在注释里说明原因，方便下次升级 MC 版本时定位

## 许可证

提交代码即表示同意以 **GPL-3.0** 发布（与本项目一致）。

修改或分发时必须保留：
- 原作者署名（ichttt 及 First Aid 贡献者）
- GPL-3.0 许可证与源码可得性
