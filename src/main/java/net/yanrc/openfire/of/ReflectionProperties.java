package net.yanrc.openfire.of;

import java.util.AbstractMap;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;

/***
 * @version $Id: ReflectionProperties.java 480784 2006-11-30 00:07:45Z jvanzyl $
 * @todo merge with resources/assembly plugin
 */
public class ReflectionProperties extends AbstractMap {
    private MavenProject project;

    public ReflectionProperties(MavenProject project) {
        this.project = project;
    }

    public synchronized Object get(Object key) {
        Object value = null;
        try {
            value = ReflectionValueExtractor.evaluate(String.valueOf(key), project);
        } catch (Exception e) {
            // TODO: remove the try-catch block when
            // ReflectionValueExtractor.evaluate() throws no more exceptions
        }
        return value;
    }

    public Set entrySet() {
        throw new UnsupportedOperationException("Cannot enumerate properties in a project");
    }
}
