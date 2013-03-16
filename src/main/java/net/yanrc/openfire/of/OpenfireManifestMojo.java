 package net.yanrc.openfire.of;
 

 
 import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.IOUtil;
 
 /***
  * Generate a manifest for the Openfire Plugin.
  *
  * @author Mike Perham
  * @version $Id: OpenfireMojo.java 307363 2005-10-09 04:50:58Z brett $
  * @goal manifest
  * @phase process-resources
  * @requiresDependencyResolution runtime
  */
 public class OpenfireManifestMojo extends AbstractOpenfireMojo
 {
     /***
      * Executes the OpenfireMojo on the current project.
      *
      * @throws MojoExecutionException if an error occured while building the webapp
      */
     public void execute() throws MojoExecutionException
     {
         File manifestDir = new File(getWarSourceDirectory(), "META-INF");
         if (!manifestDir.exists())
         {
             manifestDir.mkdirs();
         }
         File manifestFile = new File(manifestDir, "MANIFEST.MF");
         MavenArchiver ma = new MavenArchiver();
         ma.setArchiver(jarArchiver);
         ma.setOutputFile(manifestFile);
 
         PrintWriter printWriter = null;
         try
         {
             Manifest mf = ma.getManifest(getProject(), archive.getManifest());
             printWriter = new PrintWriter(new FileWriter(manifestFile));
             mf.write(printWriter);
         }
         catch (ManifestException e)
         {
             throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
         }
         catch (DependencyResolutionRequiredException e)
         {
             throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
         }
         catch (IOException e)
         {
             throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
         }
         finally
         {
             IOUtil.close(printWriter);
         }
     }
 }
