# SAP PO/PI 对接招行CBS8系统

开发工具：NWDS
系统版本：NW750EXT_16_REL

已打包好可直接部署的代码
- java mapping--> https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/cbsSign.jar
- ejb -> https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/cbs8Decryption_EAR.ear

## 验签&报文加密
总结：使用JAVA Mapping做sign计算及报文加密
<img width="1483" height="373" alt="image" src="https://github.com/user-attachments/assets/8ae5f7d8-c811-4789-989a-5fbb2ef7ad6e" />

### 1. 创建JAVA Project
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
-  json-20230227.jar
-  org.apache.commons.codec-1.14.0.v20200818-1422.jar
-  cprov-jdk15on-1.60.jar或者bcprov-jdk18on-1.78.1.jar
-  如果出现异常，请直接按截图版本下载

java代码详见https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/JAVA_MAPPING

### 2. 打包自开发java mapping包
<img width="929" height="553" alt="image" src="https://github.com/user-attachments/assets/991697f2-55f4-4a27-a1eb-41c17dd52511" />

### 3. 第三方jar包上传
因java mapping中使用了第三方包，此时需要上传相关包才能确保java mapping正常使用
<img width="895" height="584" alt="image" src="https://github.com/user-attachments/assets/0a0aa105-b00b-4bf7-b954-fc4f911bee79" />

### 4. 在OM中使用自开发java mapping
<img width="1486" height="620" alt="image" src="https://github.com/user-attachments/assets/f8320631-4aaf-455f-bee8-8900c3f85e59" />

### 5. DT入参说明
<img width="1491" height="573" alt="image" src="https://github.com/user-attachments/assets/c1c7d80b-f455-4183-ab0f-8fab6f6f0acf" />
bodyEncryptionKey 平台公钥，报文加密使用
EncryptionPrivateKey 签名私钥
Token 通过接口获取到的token
addSquare 是否增加最外层[],有些报文是是{ }， 有些报文是[{ }]，用于增加最外层的[]
data  这里要固定，因为java mapping中是默认取data中的数据作为发送报文，有特殊需求自行修改代码
注意大小写


## 报文解密
总结：使用Communication Channel Module做报文解密
<img width="1366" height="673" alt="image" src="https://github.com/user-attachments/assets/18fb0b4d-ee05-4934-b337-55a306578ca0" />
### 1. 创建EJB Project
<img width="1117" height="623" alt="image" src="https://github.com/user-attachments/assets/f6d6223d-cf83-4f0e-b6ab-48d933087375" />
<img width="511" height="761" alt="image" src="https://github.com/user-attachments/assets/592a22a7-0d2b-435c-9b00-a6b5fb02b503" />
<img width="511" height="761" alt="image" src="https://github.com/user-attachments/assets/28c8cf2c-11e0-46cb-83f9-160b3c133605" />
<img width="761" height="708" alt="image" src="https://github.com/user-attachments/assets/d8b2de32-18a5-4b87-97d6-9127db015f77" />
<img width="511" height="505" alt="image" src="https://github.com/user-attachments/assets/c3f41201-0ba2-465d-8c2e-b6ede9ab1294" />

修改这3个文件
<img width="345" height="547" alt="image" src="https://github.com/user-attachments/assets/688c3d22-4b61-46fe-886c-f71a5b97609d" />
https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/application-j2ee-engine.xml
https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/ejb-j2ee-engine.xml
https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/ejb-jar.xml
增加Library
<img width="1063" height="791" alt="image" src="https://github.com/user-attachments/assets/d906edd3-d712-43dd-998e-0dc36fe3f2e2" />
第三方Library
json-20230227.jar
org.apache.commons.io_2.2.0.v201405211200.jar
bcprov-jdk15on-1.60.jar
下载地址https://mvnrepository.com/

新增官方加密解密工具包
直接新增class，把官方代码贴进去即可，有log输出相关报错的，直接注释即可
<img width="375" height="303" alt="image" src="https://github.com/user-attachments/assets/2c284227-9b26-47d0-8c06-2eb1c287741a" />

tooltest.java核心代码https://github.com/JeremyGuan/sap-po-to-cbs/blob/main/tooltest.java

### 2. 部署

#### 测试系统部署
添加SAP AS Java服务
<img width="808" height="746" alt="image" src="https://github.com/user-attachments/assets/cfb4ed4a-7066-4a99-8340-ed1914dca268" />

<img width="790" height="592" alt="image" src="https://github.com/user-attachments/assets/17be507e-b9ae-4c15-aa0f-0ccc0f551fe9" />
<img width="543" height="697" alt="image" src="https://github.com/user-attachments/assets/4d8736ea-57f2-4b20-be7a-19c5dac17097" />
<img width="543" height="697" alt="image" src="https://github.com/user-attachments/assets/e688d1f2-8ca2-4190-9b5d-66c74e198a1b" />


#### 生产系统部署
打包
<img width="675" height="341" alt="image" src="https://github.com/user-attachments/assets/a93aa879-6a22-4349-a8d3-32f423797a07" />
<img width="685" height="445" alt="image" src="https://github.com/user-attachments/assets/21a7d495-4989-4804-bb8f-6f6515c3dcea" />

上传至生产系统服务器
命令：telnet localhost 50008
<img width="922" height="310" alt="image" src="https://github.com/user-attachments/assets/a42a6b6a-2371-442c-b6cc-e1fd23db1b31" />

命令：deploy 文件路径 version_rule=all
<img width="894" height="298" alt="image" src="https://github.com/user-attachments/assets/b2dba386-b74c-4718-8a28-cc0fd488a561" />

### 3. 使用方式
在CC中增加module，这里我设置了3个参数，分别是
bodyDecryptionKey  解密使用
nameSpace  命名空间
nodeName  最外层添加的xml标签名
注意大小写
<img width="1541" height="688" alt="image" src="https://github.com/user-attachments/assets/6cb01ea6-70d0-4d1e-b21c-7fd9059fc749" />


注意：
- 部署时可能出现错误，有可能是第三方jar包版本冲突，此时为了验证，可以移除第三方包之后部署试试，没办法，只能下载不同版本的jar包尝试了，还在在https://mvnrepository.com/下载

如果以上信息对你有帮助，欢迎交流
![9c0f0750-ce1b-4c80-ad0e-65c3eae3c2a0](https://github.com/user-attachments/assets/d18b09ed-b053-44d6-a782-36946d518b48)

