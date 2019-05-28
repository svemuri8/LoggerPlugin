package LoggerPlugin;

import com.android.build.api.transform.*;
import com.android.ddmlib.Log;
import javassist.*;
import org.gradle.api.file.Directory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LoggerTransform extends Transform {

    @Override
    public String getName() {
        return "Logger";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        Set<QualifiedContent.ContentType> set = new HashSet<>();
        set.add(QualifiedContent.DefaultContentType.CLASSES);
        return set;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        Set<? super QualifiedContent.Scope> set = new HashSet<>();
        set.add(QualifiedContent.Scope.PROJECT);
        return set;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        Set<? super QualifiedContent.Scope> set = new HashSet<>();
        set.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES);
        set.add(QualifiedContent.Scope.SUB_PROJECTS);
        return set;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) {
        System.out.println("transform method called succesfully");
        ArrayList<DirectoryInput> directoryInputs = new ArrayList<>();
        ArrayList<JarInput> jarInputs = new ArrayList<>();
        for (TransformInput transformInput: transformInvocation.getInputs()) {
            directoryInputs.addAll(transformInput.getDirectoryInputs());
            jarInputs.addAll(transformInput.getJarInputs());
        }
        ArrayList<File> outputs = new ArrayList();
        for (DirectoryInput directoryInput: directoryInputs) {
            File output = transformInvocation.getOutputProvider().getContentLocation(
                    directoryInput.getName(),
                    getOutputTypes(),
                    getScopes(),
                    Format.DIRECTORY
            );
            outputs.add(output);
            recurseOnFile(directoryInput.getFile(), output);
        }
        for (JarInput jarInput: jarInputs) {
            outputs.add(transformInvocation.getOutputProvider().getContentLocation(
                    jarInput.getName(),
                    getOutputTypes(),
                    getScopes(),
                    Format.JAR
            ));
        }
    }

    private void recurseOnFile(File passedFile, File output) {
        if (passedFile.isFile()) {
            process(passedFile, output);
        } else if (passedFile.isDirectory()) {
            File[] listOfFiles = passedFile.listFiles();
            for (File file : listOfFiles) {
                recurseOnFile(file, output);
            }
        }
    }

    private void process(File passedFile, File output) {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(passedFile.getAbsolutePath());
            CtClass ctInput = pool.get(passedFile.getName());
            CtClass ctOutput = pool.get(output.getName());
            CtMethod[] ctMethods = ctInput.getDeclaredMethods("onListItemClick");
            for (CtMethod ctMethod : ctMethods) {
                ctMethod.insertBefore("{android.util.Log.i(\"TESTING\"" +
                        ",\"JAVASSIST ADDED THIS LOG. PROCESS METHOD WORKED\");}");
            }
            ctOutput.writeFile(passedFile.getAbsolutePath());
            System.out.println("PROCESS METHOD WORKED." + passedFile.getAbsolutePath());
        } catch (NotFoundException e) {
            return;
        } catch (CannotCompileException e) {
            throw new RuntimeException("CtMethod CHANGE FAILED.");
        } catch (IOException e) {
            throw new RuntimeException("ctClass CAN'T WRITE FILE.");
        }
    }


}
