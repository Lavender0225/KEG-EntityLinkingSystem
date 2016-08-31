# KEG-EntityLinkingSystem
The entity linking system of KEG, Tsinghua

update log:
- 2016-8-23, 合并了命名实体识别部分和实体链接部分的代码，可以得到初步结果。
            程序入口：TraditionalRanking.main， 可以使用循环输入再查看日志的方式调试
            命名实体部分负责人：hj
            实体链接传统方法负责人：zj
- 2016-8-31
	1. 更改了popularity的计算方式
	2. 更改了candidate ranking的方式——先进行相关度的排序，再根据popularity(commonness)值进行选取
	3. 增加了mention prune，去掉置信度比较低的mention
	P.S. 这一版基本实现了初步的功能，但是还有些可以改进的地方
		- 建Index过滤实体的时候可以不完全按照类别进行过滤，可以将一个mention对应的popularity值高的保留下来
		- MentionDiambiguation类中的disambugiation函数调用的toStringWithOutNature函数有时候报数组异常的错误，这个需要解决一下
	