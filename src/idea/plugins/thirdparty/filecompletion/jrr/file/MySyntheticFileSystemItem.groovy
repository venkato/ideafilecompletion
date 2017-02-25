package idea.plugins.thirdparty.filecompletion.jrr.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.SyntheticFileSystemItem
import com.intellij.psi.search.PsiElementProcessor
import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.share.OSIntegrationIdea
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.Nullable

@CompileStatic
public class MySyntheticFileSystemItem extends SyntheticFileSystemItem {

    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);
    VirtualFile virtualFile;
    File file;

    public MySyntheticFileSystemItem(File file) {
        this(OSIntegrationIdea.openedProject, file);

    }

    @Override
    String getName() {
        return "Open file ${file.absolutePath}"
    }

    @Override
    String getText() {
        return getName()
    }

    public MySyntheticFileSystemItem(Project project, File file) {
        super(project);
        virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        this.file = file;
    }

    @Nullable
    @Override
    public PsiFileSystemItem getParent() {
        return new MySyntheticFileSystemItem(project, file.parentFile);
        //return null;
    }

    @Override
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public boolean processChildren(PsiElementProcessor<PsiFileSystemItem> processor) {
        return false;
    }


}
