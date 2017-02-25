package idea.plugins.thirdparty.filecompletion.jrr.classpathhook

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineState
import com.intellij.execution.configurations.ParametersList
import groovy.transform.CompileStatic
import javassist.CtClass
import javassist.CtMethod
import myidea.jrr.cl.classhook.IdeaJavaRunnerSettings
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.JrrUtilities
import net.sf.jremoterun.utilities.UrlCLassLoaderUtils
import net.sf.jremoterun.utilities.classpath.AddFilesToClassLoaderGroovy
import net.sf.jremoterun.utilities.javassist.JrrJavassistUtils
import net.sf.jremoterun.utilities.javassist.codeinjector.CodeInjector
import net.sf.jremoterun.utilities.javassist.codeinjector.InjectedCode
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@CompileStatic
class JavaClassPathHook extends InjectedCode {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static void installBothHooks() {
        JavaClassPathHook.installHook()
        JavaStartConfigHook.installHook()
    }

    @Override
    protected Object handleException(Object key, Throwable throwable) {
        super.handleException(key, throwable)
        JrrUtilities.showException("Can't create java start hook", throwable)
        return null
    }

    static void installHook() {
        Class clazz = JavaCommandLineState
        CtClass ctClass = JrrJavassistUtils.getClassFromDefaultPool(clazz)
        CtMethod method = JrrJavassistUtils.findMethod(clazz, ctClass, 'createCommandLine', 0)
        method.insertAfter """
            ${CodeInjector.createSharedObjectsHookVar2(clazz)}
            ${CodeInjector.myHookVar}.get(new Object[]{this,\$_});
        """
        JrrJavassistUtils.redefineClass(ctClass, clazz);
        CodeInjector.putInector2(clazz, new JavaClassPathHook())
    }

    @Override
    Object getImpl(Object key) {
        java.lang.Object[] obj = (Object[]) key;
        JavaCommandLineState commandLineState = obj[0] as JavaCommandLineState;
        GeneralCommandLine generalCommandLine = obj[1] as GeneralCommandLine
        String runnerName = commandLineState.environment.toString()
        File runnerFile = new File(IdeaJavaRunnerSettings.runners, runnerName)
        if (!runnerFile.exists()) {
            log.info "no runner file ${runnerFile}"
            return null
        }
        File file1 = new File(IdeaJavaRunnerSettings.libs, runnerFile.text + '.groovy')
        if (!file1.exists()) {
            log.info "file not exists ${file1}"
            return null
        }


        ParametersList parametersList = generalCommandLine.parametersList
        log.info "parametersString : ${parametersList.parametersString}"
        List<String> list = parametersList.list
        int index = list.indexOf('-classpath')
        if (index < 0) {
            log.error("bad index : ${list}")
        } else {
            String classPathJar = list.get(index + 1)
            File file = classPathJar as File
            assert file.exists()
            List<File> classpath = [];
            File myLibsPrefix = createZip7(file1)
            classpath.add myLibsPrefix
            classpath.add file
            classpath.each { assert it.exists() }
            String newClassPath = classpath.collect { it.absolutePath }.join(';')
            parametersList.set(index + 1, newClassPath)
        }
        return null
    }

    public static File createZip7(File libNameAsGroovy) throws Exception {
        List<File> classpath = [];
        AddFilesToClassLoaderGroovy addCl = new AddFilesToClassLoaderGroovy() {
            @Override
            void addFileImpl(File file33) throws Exception {
                classpath.add(file33)
            }
        }
        addCl.addFromFile5(libNameAsGroovy)
        File suffix = createZip2(classpath, FilenameUtils.getBaseName(libNameAsGroovy.name))
        return suffix
    }

    public static File createZip2(List<File> files, String libName) throws Exception {
        byte[] bs = UrlCLassLoaderUtils.createJarForLoadingClasses(files)
        assert IdeaJavaRunnerSettings.jars.exists()
        File libFile = new File(IdeaJavaRunnerSettings.jars, libName + '.jar')
        libFile.bytes = bs
        return libFile
    }



}
