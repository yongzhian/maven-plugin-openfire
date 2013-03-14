package net.yanrc.openfire;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.yanrc.util.template2file.GeneratorRequest;
import net.yanrc.util.template2file.Template2FileUtil;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.gson.Gson;

/**
 * @Mojo( name = "autoconfig")
 * @goal autoconfig
 * @phase generate-resources
 */
public class Temp2FileMojo extends AbstractMojo {

    /**
     * 输出目录.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression = "${project.basedir}"
     * @required
     * @readonly
     */
    private File baseDir;

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        GeneratorRequest rq = null;
        for (Map map : includeMaps) {
            getLog().info(map.toString());
            
            rq = new GeneratorRequest(getStringVal("templateSrcDir", map), getStringVal("fileTargetDir", map),
                            getStringVal("fileSuffix", map), getStringVal("templateType", map),
                            Boolean.parseBoolean(getStringVal("isCache", map)), getStringVal("userName", map),
                            getStringVal("password", map), getStringVal("url", map), getStringVal("timeout", map));
            
            getLog().info(new Gson().toJson(rq));
            
            try {
                Template2FileUtil.execute(rq);
            } catch (Exception e) {
                getLog().error("Temp2FileMojo execute error!", e);
            }
        }

        // getLog().info("baseDir:" + baseDir.getAbsolutePath());
        // getLog().info("sourceDirectory:" +
        // sourceDirectory.getAbsolutePath());
        // getLog().info("resources:" + resources.size());
        // getLog().info("testSourceDirectory:" +
        // testSourceDirectory.getAbsolutePath());
        // getLog().info("testResources:" + testResources.size());
    }

    String getStringVal(String key, Map map) {
        if (map.get(key) != null) {
            return map.get(key).toString();
        }
        return "";
    }
}
