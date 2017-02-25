package idea.plugins.thirdparty.filecompletion.jrr.actions.maven

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.tools.Tool
import com.intellij.util.text.CharArrayUtil
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.maven.MyAcceptMavenProviderImpl
import idea.plugins.thirdparty.filecompletion.share.Ideasettings.OpenFileActionSettings
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.JrrUtilities
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
public class OpenMavenIdImpl extends AnAction {


    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static File browserPath = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe" as File

    @Override
    void actionPerformed(AnActionEvent e) {
        try {
            actionPerformedImpl(e);
        }catch (Throwable e2){
            JrrUtilities.showException('Failed open maven id',e2)
        }
    }

    void actionPerformedImpl(AnActionEvent e) {
        log.debug "running ${e}"
        MavenId place = getPlace(e)
        if (place == null) {
            log.debug "can't find file name"
            return
        }
        openFile3(place, e);
        //runTool(myActionId, e.getDataContext(), e, 0L, null);
    }

    private void openFile3(MavenId file, AnActionEvent e) {
        String url = """http://search.maven.org/#search|gav|1|g:${file.groupId} AND a:${file.artifactId}"""
        String[] ppp = [this.browserPath, url]
        Process browserOpened = Runtime.runtime.exec(ppp);


    }



    private void openFile2(MavenId file, AnActionEvent e) {
        String url = 'http://search.maven.org/#artifactdetails|'+ file.toString().replace(':','|')+'|jar'
        String[] ppp = [this.browserPath, url]
        Process browserOpened = Runtime.runtime.exec(ppp);


    }

    private void openFile(MavenId file, AnActionEvent e) {

//        Tool tool = OpenFileActionSettings.instance.findSelectedTool();
        Tool tool = OpenFileActionSettings.findAllEnabledTools().find { it.name == 'OpenMavenId' }
        assert tool != null
//        if (tool == null) {
//            if (OpenFileActionSettings.instance.findAllEnabledTools().size() == 0) {
//                ShowSettingsUtil.getInstance().showSettingsDialog(OSIntegrationIdea.openedProject, "External Tools")
//            } else {
//                ShowSettingsUtil.getInstance().showSettingsDialog(OSIntegrationIdea.openedProject, Constants.pluginName)
//            }
//
//        } else {
        MyMavenContext myDataContext = new MyMavenContext(e.dataContext, file)
        tool.execute(e, myDataContext, 0L, null);
        log.debug "run action done"

    }

    @Override
    void setInjectedContext(boolean worksInInjected) {
        super.setInjectedContext(worksInInjected)
    }

    @Override
    void update(AnActionEvent e) {
        // log.debug e
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        MavenId place = getPlace(e)
        boolean iok = place != null
        if (iok) {
//            log.debug place
            if (iok) {
                log.debug "found file method with path : ${place}"
            }
        }
        e.presentation.visible = iok
        e.presentation.enabled = iok
        //super.update(e)
    }


    @Nullable
    private static MavenId getPlace(AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return null;
        }

        PsiElement psiElement1 = null;
        Document document = null;
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        if (editor != null) {
            document = editor.getDocument();
//                log.debug "editor found "
            PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if (file != null) {
                final VirtualFile virtualFile = file.getVirtualFile();
                FileType fileType = virtualFile != null ? virtualFile.getFileType() : null;
                psiElement1 = findMethod(project, editor);
                log.debug "psiElement1 ${psiElement1?.class.name} ${psiElement1}"
            } else {
                // log.debug "file is null"
            }
        }
//        }
        //log.debug "psiElement1 ${}"
        if (psiElement1 instanceof LeafPsiElement) {
            if (MyAcceptMavenProviderImpl.isOkPsiElement(psiElement1 as LeafPsiElement)) {
                String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(psiElement1.parent);
                List<String> ids = value4.tokenize(':')
                if (value4.count(':') != 2 || ids.size() != 3) {
                    log.debug "Not a maven token ${value4}"
                    return null
                }
                MavenId mavenId = new MavenId(value4)
//                File file = addFilesToClassLoader.findMavenOrGradle(mavenId)
//                if(file ==null){
//                    log.info "Maven token not found : ${value4}"
//                    return null
//                }
                return mavenId;
            }

        }
        return null;
    }

    @Nullable
    private static PsiElement findMethod(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        final int offset = CharArrayUtil.shiftForward(editor.getDocument().getCharsSequence(), editor.getCaretModel().getOffset(), " \t");
        return psiFile.findElementAt(offset)
    }


}
