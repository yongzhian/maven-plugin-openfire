package net.yanrc.openfire;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.yanrc.util.template2file.io.Resources;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.codehaus.plexus.util.FileUtils;

/**
 * @Mojo( name = "ofplugingen")
 * @goal ofplugingen
 */
public class OpenfirePluginGenerateMojo extends AbstractMojo {

	String S = File.separator;

	VelocityContext context = null;

	/**
	 * 输出目录.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @parameter expression = "${project.build.sourceDirectory}"
	 * @required
	 * @readonly
	 */
	private File sourceDirectory;

	/**
	 * @parameter expression = "${project.build.resources}"
	 * @required
	 * @readonly
	 */
	private List<Resource> resources;

	/**
	 * @parameter expression = "${project.build.testSourceDirectory}"
	 * @required
	 * @readonly
	 */
	private File testSourceDirectory;

	/**
	 * @parameter expression = "${project.build.testResources}"
	 * @required
	 * @readonly
	 */
	private List<Resource> testResources;

	/**
	 * @parameter
	 */
	private List<Map> includeMaps;

	private String targetDir;

	private String groupId;

	private String artifactId;

	private String version;

	private String targetDirPath;

	private String baseDir;

	private String srcDir;

	private String webDrcDir;

	private String javaPackageDir;

	private String openfireTargetDir;

	private String openfireSrcDir;

	public void execute() throws MojoExecutionException, MojoFailureException {

		if (includeMaps != null && !includeMaps.isEmpty()) {
			for (Map map : includeMaps) {
				targetDir = getStringVal("targetDir", map);
				groupId = getStringVal("groupId", map);
				artifactId = getStringVal("artifactId", map);
				version = getStringVal("version", map);
				exec();
			}
		}
	}

	String getStringVal(String key, Map map) {
		if (map.get(key) != null) {
			return map.get(key).toString().trim();
		}
		return "";
	}

	public void exec() {
		if (!paramsCheckAndSet(groupId, artifactId, version)) {
			return;
		}

		setAndMkTargetDirPath(targetDir);

		// target/artifactId
		setAndMkBaseDir(targetDirPath + S + artifactId);

		// target/artifactId/src/main
		setAndMkSrcDir(baseDir + S + "src" + S + "main");

		// target/artifactId/src/main/webapp
		setAndMkWebDrcDir(srcDir + S + "webapp");

		// target/artifactId/src/main/java/goroupId
		setAndMkJavaPackageDir(srcDir + S + "java" + S
				+ generateDirPath(groupId));

		String of_dir = "/" + "openfire-plugin";

		// crete datebase dir
		mkAndCopydir("demo.sql", of_dir + "/" + "database", srcDir + S
				+ "database", artifactId + ".sql");
		// crete i18n dir
		mkAndCopydir("demo_i18n.properties", of_dir + "/" + "i18n", srcDir + S
				+ "i18n", artifactId + "_i18n.properties");

		// crete resources dir
		mkAndCopydir("plugin.properties", of_dir + "/" + "resources", srcDir
				+ S + "resources", artifactId + ".properties");

		// crete java dir
		generatejavaSrc();

		// crete openfire dir
		mkAndSetOpenfireTargetDir(context, srcDir);

		copyOpenfireDirFiles(of_dir + "/" + "openfire");

		// webapp
		writeFile("index.jsp", of_dir + "/" + "webapp",
				webDrcDir);
		
		writeFile("demo.gif", of_dir + "/" + "webapp" + "/" + "images",
				webDrcDir + S + "images", artifactId + ".gif");

		mkAndCopydir("demo.js", of_dir + "/" + "webapp" + "/" + "scripts",
				webDrcDir + S + "scripts", artifactId + ".js");

		mkAndCopydir("demo.css", of_dir + "/" + "webapp" + "/" + "style",
				webDrcDir + S + "style", artifactId + ".css");

		mkAndCopydir("web.xml", of_dir + "/" + "webapp" + "/" + "WEB-INF",
				webDrcDir + S + "WEB-INF", null);

		mergeFile(context, "pom.xml", of_dir, baseDir);

	}

	void generatejavaSrc() {

		String srcDir = generateDirPath("openfire.plugin.timer");
		String targetDir = this.javaPackageDir + S + srcDir;
		mkdir(targetDir);
		mergeFile(context, "TimerPlugin.java",
				"/openfire-plugin/java/openfire/plugin/timer", targetDir);

		String p2 = generateDirPath("openfire.plugin.timer.handler");
		String t2 = this.javaPackageDir + S + p2;
		mkdir(t2);
		mergeFile(context, "IQTimerHandler.java",
				"/openfire-plugin/java/openfire/plugin/timer/handler", t2);
	}

	void mkAndCopydir(String fileNmes, String sourcesSir, String targerDir,
			String targetFileName) {
		String[] nameArr = fileNmes.split("\\|");
		mkAndCopydir(nameArr, sourcesSir, targerDir, targetFileName);
	}

	void mkAndCopydir(String[] fileNmes, String sourcesSir, String targerDir,
			String targetFileName) {
		mkdir(targerDir);
		for (String name : fileNmes) {
			mergeFile(context, name, sourcesSir, targerDir, targetFileName);
		}
	}

	void initVelocity() {
		Properties p = new Properties();
		p.setProperty("file.resource.loader.cache", "false");
		p.setProperty("input.encoding", "UTF-8");
		p.setProperty("output.encoding", "UTF-8");

		Velocity.init(p);

		context = new VelocityContext();
	}

	void setAndMkJavaPackageDir(String javaPackageDir) {
		this.javaPackageDir = javaPackageDir;
		mkdir(this.javaPackageDir);
	}

	void setAndMkWebDrcDir(String webDrcDir) {
		this.webDrcDir = srcDir + S + "webapp";
		mkdir(this.webDrcDir);
	}

	void setAndMkSrcDir(String srcDir) {
		this.srcDir = baseDir + S + "src" + S + "main";
		mkdir(this.srcDir);
	}

	void setAndMkBaseDir(String baseDir) {
		this.baseDir = this.targetDirPath + S + artifactId;
		mkdir(this.baseDir);
	}

	void setAndMkTargetDirPath(String targetDir) {
		if (StringUtils.isBlank(targetDir)
				|| !new File(targetDir).isDirectory()) {
			this.targetDirPath = outputDirectory.getAbsolutePath();
		} else {
			this.targetDirPath = targetDir;
		}

		mkdir(this.targetDirPath);
	}

	boolean paramsCheckAndSet(String groupId, String artifactId, String version) {
		if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId)
				|| StringUtils.isBlank(version)) {
			getLog().error("param error!");
			return false;
		}

		initVelocity();

		context.put("groupId", groupId);
		context.put("artifactId", artifactId);
		context.put("version", version);
		context.put("date", new Date().toLocaleString());

		getLog().info("VTL init succeed!");

		return true;
	}

	void copyOpenfireDirFiles(String openfireSrcDir) {
		this.openfireSrcDir = openfireSrcDir;
		mergeFile(context, "plugin.xml", this.openfireSrcDir, openfireTargetDir);

		writeFile("changelog.html", this.openfireSrcDir, openfireTargetDir);
		writeFile("logo_large.gif", this.openfireSrcDir, openfireTargetDir);
		writeFile("logo_small.gif", this.openfireSrcDir, openfireTargetDir);
		writeFile("readme.html", this.openfireSrcDir, openfireTargetDir);
	}

	void mkAndSetOpenfireTargetDir(VelocityContext context, String webDrcDir) {

		this.openfireTargetDir = webDrcDir + S + "openfire";
		mkdir(this.openfireTargetDir);
	}

	void mkdir(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			file.delete();
		}
		file = new File(dir);
		file.mkdirs();

		getLog().info("generate dir:" + file.getAbsolutePath());

	}

	String generateDirPath(String groupId) {
		if (StringUtils.isNotBlank(groupId)) {
			String[] arr = groupId.split("\\.");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < arr.length; i++) {
				sb.append(arr[i]);
				if (i != arr.length - 1) {
					sb.append(S);
				}
			}
			return sb.toString();
		}
		return "";
	}

	void writeFile(String resource, String SrcDir, String targetDir) {
		writeFile(resource, SrcDir, targetDir, null);
	}

	void writeFile(String resource, String SrcDir, String targetDir,
			String newResource) {
		FileOutputStream fos = null;
		try {
			String from = SrcDir + "/" + resource;
			
			new File(targetDir).mkdirs();
			
			String to = targetDir + S + resource;

			if (StringUtils.isNotBlank(newResource)) {
				to = targetDir + S + newResource;
			}

			getLog().info("create file:from " + from + " to " + to);

			InputStream in = this.getClass().getResourceAsStream(from);

			BufferedInputStream bis = new BufferedInputStream(in);
			File targetFile = new File(to);

			if (targetFile.exists()) {
				targetFile.delete();
			}

			targetFile.createNewFile();

			fos = new FileOutputStream(targetFile);

			byte[] cbuf = new byte[1024];
			int len = 0;
			while ((len = bis.read(cbuf)) != -1) {
				fos.write(cbuf, 0, len);
			}

			fos.flush();
			fos.close();

		} catch (Exception e) {
			getLog().error(e);
		}
	}

	void mergeFile(VelocityContext context, String resource, String SrcDir,
			String targetDirPath) {
		mergeFile(context, resource, SrcDir, targetDirPath, null);
	}

	void mergeFile(VelocityContext context, String resource, String SrcDir,
			String targetDirPath, String newResource) {

		try {

			String from = SrcDir + "/" + resource;
			new File(targetDir).mkdirs();
			String to = targetDirPath + S + resource;

			if (StringUtils.isNotBlank(newResource)) {
				to = targetDirPath + S + newResource;
			}

			getLog().info("merger file:from " + from + " to " + to);

			InputStream in = this.getClass().getResourceAsStream(from);

			if (in == null) {
				in = Resources.getResourceAsStream(this.getClass()
						.getClassLoader(), from);
			}

			if (in == null) {
				in = new FileInputStream(FileUtils.getFile(from));
			}

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in));

			File targetFile = new File(to);

			if (targetFile.exists()) {
				targetFile.delete();
			}

			targetFile.createNewFile();

			FileWriter w = new FileWriter(targetFile);

			Velocity.evaluate(context, w, resource, bufferedReader);
			w.flush();
			w.close();

		} catch (Exception e) {
			getLog().error(e);
		}
	}

}
