package idea.plugins.thirdparty.filecompletion.jrr.actions

import com.intellij.debugger.impl.DebuggerUtilsEx
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.util.text.CharArrayUtil
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.jrrlauncher.JrrJmxRmiLauncher
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
class RemoteRunActionImpl extends AnAction {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    Date lastAnalized;
    int offset;


    @Override
    void actionPerformed(AnActionEvent e) {
        log.debug e
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        PsiMethod psiMethod = getPlace(e)
        if (psiMethod == null) {
            log.debug "method is null"
            return
        }
        final PsiFile containingFile = psiMethod.getContainingFile();
        if (containingFile == null) {
            log.debug "can't find file"
            return
        }

        Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
        int lineNumber = document.getLineNumber(psiMethod.textOffset)
        PsiClass clazz = psiMethod.containingClass
        File userHome = System.getProperty('user.home') as File
        if (!userHome.exists()) {
            log.error("userhome not exits : ${userHome}")
            return
        }
        final File file = new File(
                userHome, "jrr.properties");
        file.text = """
# generated at ${new Date().format("yyyy-MM-dd HH:mm")}
className=${clazz.qualifiedName}
methodName=${psiMethod.name}
lineNumer=${lineNumber + 2}
"""
        log.debug "file create"
        new OSIntegrationIdea().runLaunchConfiguration('JrrRun', null)
    }

    @Override
    void setInjectedContext(boolean worksInInjected) {
        super.setInjectedContext(worksInInjected)
    }

    @Override
    void update(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        PsiMethod place = getPlace(e)
        boolean iok = place != null
        if (iok) {
//            log.debug place
            iok = plMethod(place)
            if (iok) {
                log.debug "found rrun method : ${place.name}"
            }
        }
        e.presentation.visible = iok
        e.presentation.enabled = iok
        if (iok) {
            e.presentation.text = "rrun ${place.name}"
        }
        //super.update(e)
    }

    boolean plMethod(PsiMethod psiMethod) {
        String methodName = psiMethod.name
//        log.debug methodName
        if (!methodName.startsWith('rrun')) {
            return false
        }
        PsiClassType find = psiMethod.containingClass.extendsListTypes.find {
            return it.className != null && it.className.contains(JrrJmxRmiLauncher.simpleName)
        };
        return find != null
        //psiMethod.
    }


    @Nullable
    private static PsiMethod getPlace(AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return null;
        }

        PsiElement method = null;
        Document document = null;
        PsiMethod psiMethod

        if (ActionPlaces.PROJECT_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.STRUCTURE_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.FAVORITES_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.NAVIGATION_BAR_POPUP.equals(event.getPlace())) {
            final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement instanceof PsiMethod) {
                psiMethod = psiElement as PsiMethod
                //   log.debug "method found 1 : ${psiMethod.name}"
                final PsiFile containingFile = psiElement.getContainingFile();
                if (containingFile != null) {
                    method = psiElement;
                    document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
                }
            }
        } else {
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
                    if (StdFileTypes.JAVA == fileType || StdFileTypes.CLASS == fileType) {
                        psiMethod = findMethod(project, editor);
                        method = psiMethod
                        // log.debug "method found 2 : ${psiMethod.name}"
                    } else {
                        if (org.jetbrains.plugins.groovy.GroovyFileType.GROOVY_FILE_TYPE == fileType) {
                            psiMethod = findMethod(project, editor);
                            method = psiMethod
                            //     log.debug "method found 3 : ${psiMethod.name}"

                        }
                        //log.debug "bad file type : ${fileType}"
                    }
                } else {
                    // log.debug "file is null"
                }
            }
        }

        return psiMethod;
    }

    @Nullable
    private static PsiMethod findMethod(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        final int offset = CharArrayUtil.shiftForward(editor.getDocument().getCharsSequence(), editor.getCaretModel().getOffset(), " \t");
        return DebuggerUtilsEx.findPsiMethod(psiFile, offset);
    }
}
