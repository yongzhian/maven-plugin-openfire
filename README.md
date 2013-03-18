maven-plugin-openfire
=====================

主要功能：
1. 生成openfire插件目录结构
（1）在任何maven工程中引入该插件，如下：
	<build>
		<plugins>
			<plugin>
				<groupId>net.yanrc.openfire</groupId>
				<artifactId>maven-plugin-openfire</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>ofplugingen</goal>
						</goals>
						<phase>prepare-package</phase>
					</execution>
				</executions>
				<configuration>
					<includeMaps>
						<includeMap>
							<groupId>net.yanrc.ofplugin</groupId>
							<artifactId>demo</artifactId>
							<version>1.0.0</version>
							<targetDir>${project.build.directory}</targetDir>
						</includeMap>
					</includeMaps>
				</configuration>
			</plugin>
		</plugins>
	</build>
（2）在该工程目录下执行：mvn package,就在target目录中生存一个如下结构的openfire插件。
	.demo
	├── pom.xml
	└── src
	    └── main
		├── database
		│   └── demo.sql
		├── i18n
		│   └── demo_i18n.properties
		├── java
		│   └── net
		│       └── yanrc
		│           └── ofplugin
		│               └── openfire
		│                   └── plugin
		│                       └── timer
		│                           ├── handler
		│                           │   └── IQTimerHandler.java
		│                           └── TimerPlugin.java
		├── openfire
		│   ├── changelog.html
		│   ├── logo_large.gif
		│   ├── logo_small.gif
		│   ├── plugin.xml
		│   └── readme.html
		├── resources
		│   └── demo.properties
		└── webapp
		    ├── images
		    │   └── demo.gif
		    ├── index.jsp
		    ├── scripts
		    │   └── demo.js
		    ├── style
		    │   └── demo.css
		    └── WEB-INF
		        └── web.xml

2. 打包openfire插件
（1）进入到demo工程，执行：mvn package。可以在target目录下看到生成如下结构jar包。
	demo-1.0.0
	├── changelog.html
	├── classes
	│   ├── demo.properties
	│   ├── net
	│   │   └── yanrc
	│   │       └── ofplugin
	│   │           └── openfire
	│   │               └── plugin
	│   │                   └── timer
	│   │                       ├── handler
	│   │                       │   └── IQTimerHandler.class
	│   │                       └── TimerPlugin.class
	│   └── openfire
	│       └── jsp
	│           └── demo
	│               └── index_jsp.class
	├── database
	│   └── demo.sql
	├── i18n
	│   └── demo_i18n.properties
	├── lib
	│   └── commons-lang-2.6.jar
	├── logo_large.gif
	├── logo_small.gif
	├── META-INF
	├── plugin.xml
	├── readme.html
	└── web
	    ├── images
	    │   └── demo.gif
	    ├── scripts
	    │   └── demo.js
	    ├── style
	    │   └── demo.css
	    └── WEB-INF
		└── web.xml
这时一个openfire插件包结构

3. 提供一个插件包重新命名功能
(1) 一般来说，我们采用了maven来管理openfire插件，打包出来的插件jar包的名字一般为 finalname-version.jar（demo-1.0.0.jar）,而我们openfire最终的插件jar包格式是简单的（demo.jar），所以该mojo就提供一个重命名的功能。配置如下：

....
	<build>
		<plugins>
			<plugin>
				<groupId>net.yanrc.openfire</groupId>
				<artifactId>maven-plugin-openfire</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>ofplugingen</goal>
						</goals>
						<phase>prepare-package</phase>
					</execution>
				</executions>
				<includeMaps>
					<includeMap>
						<srcDirPath>/home/yanrc/test7</srcDirPath>
						<srcDescription>im.yanrc.plugin.*-*.jar</srcDescription>
						<toDirPath>/home/yanrc/test7</toDirPath>
						<toDescription>-s=-;-f=0|-s=.;-f=3</toDescription>
						<suffix>jar</suffix>
						<keepSource>true</keepSource>
					</includeMap>
				</includeMaps>
				</configuration>
			</plugin>
		</plugins>
	</build>
....

以上配置的功能就是将/home/yanrc/test7/im.yanrc.plugin.*-*.jar格式文件拷贝到：/home/yanrc/test7/x.jar,如：
可以将/home/yanrc/test7/im.yanrc.plugin.vcard-1.0.0.jar 拷贝到/home/yanrc/test7/vcard.jar

4. 提供模板转换成文件功能
....
	<build>
		<plugins>
			<plugin>
				<groupId>net.yanrc.openfire</groupId>
				<artifactId>maven-plugin-openfire</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<goals>
							<goal>ofplugingen</goal>
						</goals>
						<phase>prepare-package</phase>
					</execution>
				</executions>
					<includeMaps>
						<includeMap>
							<templateSrcDir>${basedir}/src/main/autoconfig</templateSrcDir>
							<fileTargetDir>${basedir}/target/classes</fileTargetDir>
							<fileSuffix>xml</fileSuffix>
							<url>http://192.168.100.75:8080/uic/cfs</url>
							<templateType>velocity</templateType>
							<userName>rr</userName>
							<password>44</password>
							<timeout>2000</timeout>
						</includeMap>
						<includeMap>
							<templateSrcDir>${basedir}/src/main/autoconfig</templateSrcDir>
							<fileTargetDir>${basedir}/target/classes</fileTargetDir>
							<fileSuffix>xml</fileSuffix>
							<url>http://192.168.100.75:8080/uic/cfs</url>
							<templateType>freemarker</templateType>
						</includeMap>
					</includeMaps>
				</configuration>
			</plugin>
		</plugins>
	</build>
....

说明：从http://192.168.100.75:8080/uic/cfs 拿到一个json格式的配置数据，如下：
[
    {
        "key": "user.name",
        "value": "yrc_dev"
    },
    {
        "key": "user.psd",
        "value": "yrc_psd_dev"
    },
    {
        "key": "username",
        "value": "yanricheng"
    }
]

拿到这个数据源去渲染模板，输出成相应文件。
