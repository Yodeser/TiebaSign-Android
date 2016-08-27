### TiebaSign-Android
百度贴吧签到助手，一键签到，定时签到
### 下载地址
<img src="https://github.com/abcmmee/TiebaSign-Android/raw/master/picture/360.png"  width="120px" height="120px" />
https://yunpan.cn/cMm64hbL4WF8j  访问密码 c848
### 自己写安卓客户端的时候，参考过链接，会Python的话，直接看下面两个链接吧~
https://github.com/chaonet/baidu_tieba_auto_sign<br/>
https://www.v2ex.com/t/286771
### 公告
发现定时签到会有延迟现象
### 使用说明
发布APP的时候，我把最小的SDK调成了8，也就是Android2.2，我自己的手机是Android5.1，模拟器是Android M。<br />
咳咳、按道理软件是兼容Android2.2 ~ Android M。<br />
如果要定时签到的话，记得把软件添加到手机内存清理白名单
### 代码说明
核心代码在app/src/main/java/com/abcmmee/tieba/utils/文件夹里面<br />
BaiduTiebaUtils.java，用于获取贴吧列表还有签到等一些功能 <br />
HttpUtils.java是一个发送GET、POST的工具类、MyCookieJar.java是存贮cookies用的<br />
英文注释的那些代码是Android Studio生成的模板代码 <br />
### 应用截图
<img src="https://raw.githubusercontent.com/abcmmee/TiebaSign-Android/master/picture/2.png" width="300" height="600">
<img src="https://raw.githubusercontent.com/abcmmee/TiebaSign-Android/master/picture/3.png" width="300" height="600">
<br/>
还有些图片上传到GitHub上就显示不出来T_T，clone到本地就可以看到。
