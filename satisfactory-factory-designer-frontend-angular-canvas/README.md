# Satisfactory Factory Designer - Angular Canvas Frontend

画布版 Angular 前端。后端继续使用 Spring Boot + MySQL 项目，默认 API 地址为 `http://localhost:8080/api`。

## 启动

```powershell
npm config set registry https://registry.npmjs.org/
npm install
npm run dev
```

浏览器打开：

```text
http://localhost:4200
```

## 画布特性

- 左侧生产桶可以拖动，位置会保存到后端 `positionX / positionY`。
- 右侧显示本厂物资线 / 总线。
- SVG 连线显示桶和物资线关系：绿色为产出到总线，红色虚线为从总线取料。
- 点击生产桶或物资线打开右侧详情面板。
- 仍然保留工厂启用、桶启用、生产节点、offset、外联输入、实时计算和等级建议。

## 后端地址

修改：

```text
src/environments/environment.ts
```
