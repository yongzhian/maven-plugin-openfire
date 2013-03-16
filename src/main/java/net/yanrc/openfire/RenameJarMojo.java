package net.yanrc.openfire;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.gson.Gson;

/**
 * @Mojo( name = "renameJar")
 * @goal renameJar
 */
public class RenameJarMojo extends AbstractMojo {

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

    /** 命令参数 */
    private String SPILIT = "-s";

    /** 命令参数 */
    private String FIELD = "-f";

    public void execute() throws MojoExecutionException, MojoFailureException {
        /** 源文件目录 */
        String srcDirPath = null;
        /** 源文件描述 */
        String srcDescription = null;
        /** 目标文件目录 */
        String toDirPath = null;
        /** 目标文件描述 */
        String toDescription = null;
        /** 目标文件类型 */
        String suffix = ".jar";
        /** 是否保留原文件 */
        String keepSource = "true";
        if (includeMaps != null && !includeMaps.isEmpty()) {
            for (Map map : includeMaps) {
                getLog().info(new Gson().toJson(map));
                srcDirPath = getStringVal("srcDirPath", map);
                srcDescription = getStringVal("srcDescription", map);
                toDirPath = getStringVal("toDirPath", map);
                toDescription = getStringVal("toDescription", map);
                suffix = getStringVal("suffix", map);
                keepSource = getStringVal("keepSource", map);
                exec(srcDirPath, srcDescription, toDirPath, toDescription, suffix, keepSource);
            }
        }

    }

    String getStringVal(String key, Map map) {
        if (map.get(key) != null) {
            return map.get(key).toString().trim();
        }
        return "";
    }

    /**
     * 执行命令
     */
    public void exec(String srcDirPath, String srcDescription, String toDirPath, String toDescription, String suffix,
                    String keepSource) {

        getLog().info("isKeepSource:" + keepSource);

        File srcDir = new File(srcDirPath);
        File toDir = new File(toDirPath);

        File[] srcFiles = getSrcFiles(srcDir, srcDescription);
        copy(srcDirPath, srcFiles, toDir, toDescription, suffix);

        if (StringUtils.isNotBlank(keepSource) && !Boolean.parseBoolean(keepSource)) {
            deleteFiles(srcFiles);
        }
    }

    /**
     * 获取源文件
     * 
     * @param srcDir
     *            源文件目录
     * @return 指定文件描述的文件集
     */
    private File[] getSrcFiles(File srcDir, final String srcDescription) {
        getLog().info("matched files:");
        File[] packedFiles = srcDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                Pattern pattern = Pattern.compile(new String(srcDescription));
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    getLog().info(dir.getAbsolutePath() + File.separator + name);
                    return true;
                }
                return false;
            }
        });

        if (packedFiles == null) {
            packedFiles = new File[0];
            return packedFiles;
        }
        return packedFiles;
    }

    /**
     * 拷贝文件到指定文件
     * 
     * @param srcFiles
     *            源文件集
     * @param toDir
     *            目标目录
     * @param fileNameDescription
     *            目标名称
     */
    private void copy(String srcDirPath, File[] srcFiles, File toDir, String toDescription, String suffix) {

        getLog().info("copy and rename files:");

        try {
            String subPath = null;
            String fullToPath = null;
            String srcFilegetAbsolutePath = null;
            int srcFileAbsPathlen = 0;
            for (File srcFile : srcFiles) {

                srcFilegetAbsolutePath = srcFile.getAbsolutePath();
                srcFileAbsPathlen = srcFilegetAbsolutePath.length();
                subPath = srcFilegetAbsolutePath.substring(new File(srcDirPath).getAbsolutePath().length() + 1,
                                srcFileAbsPathlen);

                String toFileName = getDescriptName(subPath, toDescription) + (StringUtils.isBlank(suffix)?"":("."+suffix));
                fullToPath = toDir.getAbsoluteFile() + File.separator + toFileName;

                File target = new File(fullToPath);
                if (target.exists()) {
                    getLog().info("target fileexistsed,delete it!");
                    target.delete();
                }

                target = new File(fullToPath);
                target.createNewFile();
                
                getLog().info("create file:" + fullToPath);

                FileOutputStream out = new FileOutputStream(target);
                InputStream in = new FileInputStream(srcFile);
                byte[] b = new byte[512];
                int len;
                while ((len = in.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                out.flush();
                out.close();
                in.close();
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    /**
     * 通过文件描述获取文件名称
     * 
     * @param subPath
     *            子路径
     * @param toDescription
     *            文件描述
     * @return 文件名
     */
    private String getDescriptName(String subPath, String toDescription) {
        // String[] dirAndFileStringArr = subPath.split(File.separator);
        // int len = dirAndFileStringArr.length;
        // subPath = dirAndFileStringArr[len - 1];
        String fileName = new String(subPath);
        String[] ps = toDescription.split("\\|");
        for (String p : ps) {
            fileName = processFileName(fileName, p);
        }
        getLog().info("fileName:" + fileName);
        return fileName;

    }

    /**
     * 获取文件名称
     * 
     * @param fileName
     *            fileName
     * @param p
     *            参数选项
     * @return 文件名
     */
    private String processFileName(String fileName, String p) {
        Map<String, String> map = new HashMap<String, String>();
        String[] arr = null;
        String s = null;
        String f = null;
        try {

            String[] args = p.split("\\;");
            for (String a : args) {
                String[] options = a.split("\\=");
                map.put(options[0].trim(), options[1].trim());
            }
            s = map.get(SPILIT);
            f = map.get(FIELD);
            if (s != null) {
                arr = fileName.split(new String("\\" + s));
                if (f != null) {
                    fileName = arr[Integer.valueOf(f)];
                }
            }
        } catch (Exception e) {
            getLog().error(e);
        }
        return fileName;
    }

    /**
     * 删除源文件
     * 
     * @param files
     */
    public void deleteFiles(File[] files) {
        for (File file : files) {
            getLog().info("delete file:" + file.getAbsolutePath());
            file.delete();
        }
    }

}
