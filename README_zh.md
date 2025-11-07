# SAP PO/PI 对接招行CBS8系统

开发工具：NWDS
系统版本：NW750EXT_16_REL

## 验签&报文加密
总结：使用JAVA Mapping做sign计算及报文加密
<img width="1483" height="373" alt="image" src="https://github.com/user-attachments/assets/8ae5f7d8-c811-4789-989a-5fbb2ef7ad6e" />

### 创建JAVA Mapping
1. 创建JAVA Project
<img width="1220" height="681" alt="image" src="https://github.com/user-attachments/assets/931f6814-d14c-4b07-9389-9ca103885353" />
<img width="536" height="778" alt="image" src="https://github.com/user-attachments/assets/c3a25b1f-2c3a-4c15-a2d0-2258972cafb0" />
增加Library
<img width="763" height="666" alt="image" src="https://github.com/user-attachments/assets/13b4551f-9acc-4fbe-af65-3912464ce0a7" />
<img width="736" height="751" alt="image" src="https://github.com/user-attachments/assets/20165ef7-868d-4acb-bf37-b0c1ea632fcd" />
<img width="511" height="502" alt="image" src="https://github.com/user-attachments/assets/0d6f7472-d041-4820-bbb5-f0761edf6adc" />
XPI映射库是安装NWDS时自带的
增加第三方jar包
本例用到了json/org.apache.commons.codec/bcprov,直接到https://mvnrepository.com/下载即可
注意版本：
json-20230227.jar
org.apache.commons.codec-1.14.0.v20200818-1422.jar
bcprov-jdk15on-1.60.jar或者bcprov-jdk18on-1.78.1.jar
如果出现异常，请直接按截图版本下载

java代码详见https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/JAVA_MAPPING

2. 打包自开发java mapping包
<img width="929" height="553" alt="image" src="https://github.com/user-attachments/assets/991697f2-55f4-4a27-a1eb-41c17dd52511" />

3. 第三方jar包上传
因java mapping中使用了第三方包，此时需要上传相关包才能确保java mapping正常使用
<img width="895" height="584" alt="image" src="https://github.com/user-attachments/assets/0a0aa105-b00b-4bf7-b954-fc4f911bee79" />

3. 在OM中使用自开发java mapping
<img width="1486" height="620" alt="image" src="https://github.com/user-attachments/assets/f8320631-4aaf-455f-bee8-8900c3f85e59" />


## 报文解密
总结：使用Communication Channel Module做报文解密
<img width="1366" height="673" alt="image" src="https://github.com/user-attachments/assets/18fb0b4d-ee05-4934-b337-55a306578ca0" />
1. 创建EJB Project
<img width="1117" height="623" alt="image" src="https://github.com/user-attachments/assets/f6d6223d-cf83-4f0e-b6ab-48d933087375" />
<img width="511" height="761" alt="image" src="https://github.com/user-attachments/assets/34b8b2aa-bff8-4c24-b462-d3419f90fa2b" />
<img width="511" height="761" alt="image" src="https://github.com/user-attachments/assets/28c8cf2c-11e0-46cb-83f9-160b3c133605" />

2. 部署

注意：
- 部署时可能出现错误，有可能是第三方jar包版本冲突，此时为了验证，可以移除第三方包之后部署试试，没办法，只能下载不同版本的jar包尝试了，还在在https://mvnrepository.com/下载
