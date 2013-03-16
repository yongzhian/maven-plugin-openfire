package net.yanrc.openfire;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @Mojo( name = "ofplugingen")
 * @goal ofplugingen
 */
public class OpenfirePluginGenerateMojo extends AbstractMojo {

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

        setAndMkBaseDir(targetDirPath + File.separator + artifactId);

        setAndMkSrcDir(baseDir + File.separator + "src" + File.separator + "main");

        setAndMkWebDrcDir(srcDir + File.separator + "webapp");

        setAndMkJavaPackageDir(srcDir + File.separator + "java" + File.separator
                        + groupId.replaceAll("\\.", File.separator));

        mkAndSetOpenfireTargetDir(context, webDrcDir);

        copyOpenfireDirFiles(File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "openfire");

        mkAndCopydir("README", File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "database", webDrcDir + File.separator + "database");

        mkAndCopydir("README",
                        File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator + "i18n",
                        webDrcDir + File.separator + "i18n");

        mkAndCopydir("README", File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "images", webDrcDir + File.separator + "images");

        mkAndCopydir("README", File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "scripts", webDrcDir + File.separator + "scripts");

        mkAndCopydir("README", File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "style", webDrcDir + File.separator + "style");

        mkAndCopydir("web.xml", File.separator + "openfire-plugin" + File.separator + "webapp" + File.separator
                        + "WEB-INF", webDrcDir + File.separator + "WEB-INF");

        mergeFile(context, "pom.xml", File.separator + "openfire-plugin", baseDir);

        String p1 = "openfire.plugin.timer".replaceAll("\\.", File.separator);
        String t1 = this.javaPackageDir + File.separator + p1;
        mkdir(t1);
        mergeFile(context, "TimerPlugin.java", File.separator + "openfire-plugin" + File.separator + p1, t1);

        String p2 = "openfire.plugin.timer.handler".replaceAll("\\.", File.separator);
        String t2 = this.javaPackageDir + File.separator + p2;
        mkdir(t2);
        mergeFile(context, "IQTimerHandler.java", File.separator + "openfire-plugin" + File.separator + p2, t2);
    }

    void mkAndCopydir(String fileNmes, String sourcesSir, String targerDir) {
        String[] nameArr = fileNmes.split("\\|");
        mkAndCopydir(nameArr, sourcesSir, targerDir);
    }

    void mkAndCopydir(String[] fileNmes, String sourcesSir, String targerDir) {
        mkdir(targerDir);
        for (String name : fileNmes) {
            mergeFile(context, name, sourcesSir, targerDir);
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
        this.webDrcDir = srcDir + File.separator + "webapp";
        mkdir(this.webDrcDir);
    }

    void setAndMkSrcDir(String srcDir) {
        this.srcDir = baseDir + File.separator + "src" + File.separator + "main";
        mkdir(this.srcDir);
    }

    void setAndMkBaseDir(String baseDir) {
        this.baseDir = this.targetDirPath + File.separator + artifactId;
        mkdir(this.baseDir);
    }

    void setAndMkTargetDirPath(String targetDir) {
        if (StringUtils.isBlank(targetDir) || !new File(targetDir).isDirectory()) {
            this.targetDirPath = outputDirectory.getAbsolutePath();
        } else {
            this.targetDirPath = targetDir;
        }

        mkdir(this.targetDirPath);
    }

    boolean paramsCheckAndSet(String groupId, String artifactId, String version) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version)) {
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

        this.openfireTargetDir = webDrcDir + File.separator + "openfire";
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

    void writeFile(String resource, String SrcDir, String targetDirPath) {
        try {
            String from = SrcDir + File.separator + resource;
            String to = targetDirPath + File.separator + resource;

            getLog().info("create file:from " + from + " to " + to);

            InputStream in = this.getClass().getResourceAsStream(from);
            BufferedInputStream bis = new BufferedInputStream(in);
            File targetFile = new File(to);
            if (targetFile.isFile() && targetFile.getParentFile().isDirectory()) {
                targetFile.getParentFile().mkdirs();
            }

            if (targetFile.exists()) {
                targetFile.delete();
            }

            targetFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(targetFile);

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

    void mergeFile(VelocityContext context, String resource, String SrcDir, String targetDirPath) {
        try {
            String from = SrcDir + File.separator + resource;
            String to = targetDirPath + File.separator + resource;

            getLog().info("create file:from " + from + " to " + to);

            InputStream in = this.getClass().getResourceAsStream(from);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            File targetFile = new File(to);

            if (targetFile.isFile() && targetFile.getParentFile().isDirectory()) {
                targetFile.getParentFile().mkdirs();
            }

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
