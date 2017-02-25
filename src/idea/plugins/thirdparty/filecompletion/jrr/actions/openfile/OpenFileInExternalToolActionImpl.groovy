package idea.plugins.thirdparty.filecompletion.jrr.actions.openfile

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.tools.Tool
import com.intellij.util.text.CharArrayUtil
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.file.FileCompletionBean
import idea.plugins.thirdparty.filecompletion.jrr.file.MyAcceptFileProviderImpl
import idea.plugins.thirdparty.filecompletion.jrr.maven.MyAcceptMavenProviderImpl
import idea.plugins.thirdparty.filecompletion.share.Ideasettings.Constants
import idea.plugins.thirdparty.filecompletion.share.Ideasettings.OpenFileActionSettings
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.JrrUtilities
import net.sf.jremoterun.utilities.classpath.AddFilesToClassLoaderGroovy
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression

@CompileStatic
class OpenFileInExternalToolActionImpl extends AnAction {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);



    static AddFilesToClassLoaderGroovy addFilesToClassLoader = new AddFilesToClassLoaderGroovy() {
        @Override
        void addFileImpl(File file) throws Exception {

        }
    }


    @Override
    void actionPerformed(AnActionEvent e) {
        log.debug "running ${e}"
        FileCompletionBean place = getPlace(e)
        if(place.value==null){
            log.debug "can't find file name"
            return
        }
        File file;
        if (place.parentFilePath == null) {
            file = new File(place.value)
        } else {
            file = new File(place.parentFilePath, place.value);
        }
        log.debug "file : ${file}"
        if (file.exists()) {
            openFile(file, e);
        } else {
            JrrUtilities.showException("${file.name} file not found", new FileNotFoundException(file.absolutePath))
        }
        //runTool(myActionId, e.getDataContext(), e, 0L, null);
    }

    private void openFile(File file, AnActionEvent e) {
        Tool tool = OpenFileActionSettings.instance.findSelectedTool();
        if (tool == null) {
            if (OpenFileActionSettings.instance.findAllEnabledTools().size() == 0) {
                ShowSettingsUtil.getInstance().showSettingsDialog(OSIntegrationIdea.openedProject, "External Tools")
            } else {
                ShowSettingsUtil.getInstance().showSettingsDialog(OSIntegrationIdea.openedProject, Constants.pluginName)
            }

        } else {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
            log.debug "virtualFile : ${virtualFile}"
            if (virtualFile == null) {
                VirtualFile parent = LocalFileSystem.getInstance().findFileByIoFile(file.parentFile)
                if (parent == null) {
                    JrrUtilities.showException("can't find virtual file", new FileNotFoundException(file.absolutePath))
                    return
                }
                virtualFile = parent.children.find { it.name == file.name }
                log.debug "found child : ${virtualFile}"
                if (virtualFile == null) {
                    JrrUtilities.showException("can't find virtual file 2", new FileNotFoundException(file.absolutePath))
                    return
                }
            }
            if (virtualFile != null) {
                MyDataContext myDataContext = new MyDataContext(e.dataContext, virtualFile)
                tool.execute(e, myDataContext, 0L, null);
                log.debug "run action done"
            }
        }
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
        FileCompletionBean place = getPlace(e)
        boolean iok = place != null
        if (iok) {
//            log.debug place
            if (iok) {
                log.debug "found file method with path : ${place.value}"
            }
        }
        e.presentation.visible = iok
        e.presentation.enabled = iok
        //super.update(e)
    }


    @Nullable
    private static FileCompletionBean getPlace(AnActionEvent event) {
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
        if (psiElement1 instanceof PsiJavaToken) {
            log.debug "cp3"
            return MyAcceptFileProviderImpl.isOkJavaPsiElement((PsiJavaToken) psiElement1)
        }
        if (psiElement1 instanceof LeafPsiElement) {
            if(MyAcceptMavenProviderImpl.isOkPsiElement(psiElement1 as LeafPsiElement)){
                String value4 = MyAcceptFileProviderImpl.getStringFromPsiLiteral(psiElement1.parent);
                List<String> ids = value4.tokenize(':')
                if(value4.count(':')!=2 || ids.size() != 3){
                    log.debug "Not a maven token ${value4}"
                    return null
                }
                MavenId mavenId = new MavenId(value4)
                File file = addFilesToClassLoader.mavenCommonUtils.findMavenOrGradle(mavenId)
                if(file ==null){
                    log.info "Maven token not found : ${value4}"
                    return null
                }
                FileCompletionBean element = new FileCompletionBean ()
                element.value = file.absolutePath
                return element
            }

            log.debug "cp4"
            FileCompletionBean element = MyAcceptFileProviderImpl.isOkJavaAndGroovyPsiElement((LeafPsiElement) psiElement1, true)
            if (element == null) {
                log.debug "cp2 ${psiElement1}"
                File element99 = isVar(psiElement1);
                if (element99 == null) {
                    log.debug "cp3 not found ${psiElement1}"
                    return null
                }
                FileCompletionBean completionBean = new FileCompletionBean()
                completionBean.value = element99.name
                completionBean.parentFilePath = element99.parentFile
                return completionBean;
            } else {
                return element
            }
        }
        return null;
    }


    private static File isVar(PsiElement psiElement) {
        if (psiElement instanceof LeafPsiElement) {
            log.debug "cp 1"
            if (psiElement.parent instanceof GrReferenceExpression) {
                log.debug "cp 2"
                GrReferenceExpression e = (GrReferenceExpression) psiElement.parent;
                if (e.type instanceof PsiClassType) {
                    log.debug "cp 3"
                    PsiClassType psitype = (PsiClassType) e.type;
                    PsiClass resolve = psitype.resolve()

                    if (resolve == null || !(resolve.name.contains('File'))) {
                        log.debug "no a file"
                        return null
                    }
                    log.debug("accpted")
                    if (!(e.sameNameVariants?.length == 1)) {
                        log.debug "args not 1"
                        return null;
                    }
                    PsiElement varRef = e.sameNameVariants[0].element;
                    File var3 = MyAcceptFileProviderImpl.findFileFromVarGeneric(varRef)
                    log.debug "accteped : ${var3}"
                    return var3

                }
            }else  {
                File file1 =  MyAcceptFileProviderImpl.findFileFromVarGeneric(psiElement.parent)
                log.debug "found : ${file1} ${psiElement.parent}"
                return file1

            }

        }
        return null
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
