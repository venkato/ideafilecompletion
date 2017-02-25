package idea.plugins.thirdparty.filecompletion.share

import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.history.LocalHistory
import com.intellij.ide.SaveAndSyncHandler
import com.intellij.ide.actions.OpenFileAction
import com.intellij.ide.caches.CachesInvalidator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileStatusNotification
import com.intellij.openapi.compiler.CompilerBundle
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.persistent.FSRecords
import com.intellij.psi.*
import com.intellij.psi.search.ProjectAndLibrariesScope
import com.intellij.unscramble.AnalyzeStacktraceUtil
import groovy.transform.CompileStatic
import net.sf.jremoterun.JrrUtils
import net.sf.jremoterun.utilities.DefaultObjectName
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.ObjectWrapper
import net.sf.jremoterun.utilities.OsInegrationClientI
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import javax.management.MalformedObjectNameException
import javax.management.ObjectName
import javax.swing.*
import java.util.concurrent.Callable

@CompileStatic
public class OSIntegrationIdea implements DefaultObjectName, OsInegrationClientI {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static OSIntegrationIdea osIntegrationIdea

    OSIntegrationIdea() {
        if (osIntegrationIdea == null) {
            osIntegrationIdea = this
        } else {

        }
    }

    @Override
    public ObjectName getDefaultObjectName() throws MalformedObjectNameException {
        return OsInegrationClientI.objectName;
    }

    @Override
    public void buildAllProjects() throws Exception {
        Project project = OSIntegrationIdea.openedProject
        invokeAndWaitInSwingThread {
            ApplicationManager.getApplication().runWriteAction {
                log.info "refreshing all ..."
                refreshAll();
                log.info "refreshing all done"
                log.info "rebuilding"
                CompilerManager.getInstance(project).rebuild(new CompileStatusNotification() {
                    @Override
                    public void finished(boolean aborted, int errors, int warnings,
                                         final CompileContext compileContext) {
                        if (aborted || project.isDisposed()) {
                            return;
                        }

                        String text = "";
                        LocalHistory.getInstance().putSystemLabel(project, errors == 0
                                ? CompilerBundle.message("rebuild.lvcs.label.no.errors", text)
                                : CompilerBundle.message("rebuild.lvcs.label.with.errors", text));
                        log.info "compilation finished"
                    }
                });
                log.info "rebuild all done"

            }
        }
    }

    DumbService getDumbService() {
        DumbService.getInstance(getOpenedProject())
    }


    void refreshAll() {
        invokeAndWaitInSwingThread {
            FileDocumentManager.getInstance().saveAllDocuments();
            SaveAndSyncHandler.getInstance().refreshOpenFiles();
            VirtualFileManager.getInstance().syncRefresh()
        }
    }

    static void invokeAndWaitInSwingThread(Callable callable) {
        if (SwingUtilities.isEventDispatchThread()) {
            callable.call()
        } else {
            ObjectWrapper<Throwable> exc = new ObjectWrapper<>(null);
            SwingUtilities.invokeAndWait {
                try {
                    callable.call()
                } catch (Throwable e) {
                    exc.object = e;
                }
            };
            if (exc.object != null) {
                Throwable e = exc.object;
                JrrUtils.throwThrowable(e);
            }
        }
    }

    @Override
    public void clearConsole(String s) throws Exception {

    }

    public void invalidateCacheAndRestart() {
        saveAllEditors()
        FSRecords.invalidateCaches();

        for (CachesInvalidator invalidater : CachesInvalidator.EP_NAME.getExtensions()) {
            invalidater.invalidateCaches();
        }
        final ApplicationEx app = (ApplicationEx) ApplicationManager.getApplication();
        app.restart(false)
    }


    @Override
    public void closeAllEditorsWithoutSave() {

    }

    @Override
    public void saveAllEditors() throws Exception {
        invokeAndWaitInSwingThread {
            PsiDocumentManager.getInstance(openedProject).commitAllDocuments();
            openedProject.save()
        }
    }

    @Override
    public void closeProject(String s) throws Exception {

    }

    @Override
    public String[] getPluginSymbolicNames() {
        return new String[0];
    }


    @Override
    public void openClass(String clazz) throws Exception {
        invokeAndWaitInSwingThread {
            Project project = getOpenedProject();
            ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clazz, scope);
            if (psiClass == null) {
                throw new ClassNotFoundException(clazz);
            }
            VirtualFile virtualFile = psiClass.getContainingFile().getViewProvider().getVirtualFile();
            SwingUtilities.invokeLater {
                FileEditor[] fileEditors = FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }
        }
    }


    @Override
    public void openMethod(String clazz, String methodName, int paramsCount) throws Exception {
        invokeAndWaitInSwingThread {

            Project project = getOpenedProject();
            ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clazz, scope);
            if (psiClass == null) {
                throw new ClassNotFoundException(clazz);
            }
            PsiMethod[] allMethods = psiClass.getAllMethods();
            for (PsiMethod psiMethod :
                    allMethods) {
                if (methodName.equals(psiMethod.getName()) && psiMethod.getParameterList().getParametersCount() == paramsCount) {

                    psiMethod.navigate(true);
                    return;
                }
            }
            throw new NoSuchMethodException(clazz + " "
                    + methodName + "(" + paramsCount + ")");
        }

    }

    @Override
    public void showStackTrace(String s) throws Exception {
        Project project = getOpenedProject();
        SwingUtilities.invokeLater {
            AnalyzeStacktraceUtil.addConsole(project,
                    null, "<Stacktrace p>", s)
        }

    }

    static boolean isAtLeastOneProjectOpened() {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects()
        return openProjects != null && openProjects.length > 0
    }

    static Project getOpenedProject() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects == null || openProjects.length == 0) {
            throw new IllegalStateException("Can't find open project");
        }
        return openProjects[0];

    }


    @Override
    public void openField(String clazz, String fieldName) throws Exception {
        Project project = getOpenedProject();
        ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);
        invokeAndWaitInSwingThread {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clazz, scope);
            if (psiClass == null) {
                throw new ClassNotFoundException(clazz);
            }
            PsiField allMethods = psiClass.findFieldByName(fieldName, true);
            if (allMethods == null) {
                throw new NoSuchFieldException(clazz + " "
                        + fieldName);
            }

            allMethods.navigate(true);
        }


    }

    @Override
    public void openConstructor(String clazz, int paramsCount) throws Exception {
        invokeAndWaitInSwingThread {
            Project project = getOpenedProject();
            ProjectAndLibrariesScope scope = new ProjectAndLibrariesScope(project);
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(clazz, scope);
            if (psiClass == null) {
                throw new ClassNotFoundException(clazz);
            }
            PsiMethod[] allMethods = psiClass.getAllMethods();
            for (PsiMethod psiMethod :
                    allMethods) {
                if (psiMethod.getParameterList().getParametersCount() == paramsCount) {

                    psiMethod.navigate(true);
                    return;
                }
            }
            throw new NoSuchMethodException(clazz + " "
                    + "(" + paramsCount + ")");
        }
    }

    @Override
    public void importProject(File file) throws Exception {

    }

    @Override
    public void saveWorkspace() throws Exception {

    }

    @Override
    public void openProject(String s) throws Exception {

    }

    @Override
    public void setMonitorOutFile(File file) throws FileNotFoundException {

    }

    @Override
    public void saveAllAndClose() throws Exception {
        PsiDocumentManager.getInstance(openedProject).commitAllDocuments();
        ApplicationManager.application.exit()
    }

    @Override
    public void refreshAndCleanWorkspace() throws Exception {

    }

    @Override
    public void disablePlugin(String s) throws Exception {

    }

    @Override
    public void runLaunchConfiguration(String launchName, String mode) throws Exception {
        if (mode == null) {
            mode = "Run";
        }
        Executor executor = ExecutorRegistry.getInstance().getExecutorById(mode);
        if (executor == null) {
            throw new IllegalArgumentException("Executor not found : " + mode);
        }
        RunManagerEx runManager = RunManagerEx.getInstanceEx(OSIntegrationIdea.getOpenedProject());
        RunnerAndConfigurationSettings configurationByName = runManager.findConfigurationByName(launchName);

        if (configurationByName == null) {
            throw new IllegalArgumentException("Configuration not found : " + launchName);
        }
        ExecutionUtil.runConfiguration(configurationByName, executor);
    }

    @Override
    public void openFile(File file, String s) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("Not a file : " + file.getAbsolutePath());
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        if (virtualFile == null) {
            throw new Exception("virtual file is null");
        }
        if (!virtualFile.isValid()) {
            throw new Exception("virtual file not valid");
        }

        Project project = OSIntegrationIdea.getOpenedProject();
        ObjectWrapper<Throwable> exc = new ObjectWrapper<>(null);
        SwingUtilities.invokeAndWait {
            try {
                OpenFileAction.openFile(virtualFile, project);
            } catch (Throwable e) {
                exc.object = e;
            }
        };
        if (exc.object != null) {
            Throwable e = exc.object;
            JrrUtils.throwThrowable(e);
        }
    }

    @Override
    public void openFileAndSelectInRse(File file, String s) throws Exception {
        openFile(file, s);
    }
}
