package idea.plugins.thirdparty.filecompletion.share

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
abstract class DeligateAction extends AnAction {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);


    public AnAction deligate;

    public DeligateAction(AnAction deligate) {
        this.deligate = deligate;
    }


    @Override
    void actionPerformed(AnActionEvent e) {
        deligate.actionPerformed(e)
    }

    @Override
    void update(AnActionEvent e) {
        deligate.update(e);
    }

    @Override
    boolean displayTextInToolbar() {
        return deligate.displayTextInToolbar()
    }

    @Override
    void setInjectedContext(boolean worksInInjected) {
        super.setInjectedContext(worksInInjected)
        deligate.setInjectedContext(worksInInjected)

    }

    @Override
    boolean isInInjectedContext() {
        return deligate.isInInjectedContext()
    }

    @Override
    boolean isTransparentUpdate() {
        return deligate.isTransparentUpdate()
    }

    @Override
    boolean isDumbAware() {
        return deligate.isDumbAware()
    }

    @Override
    boolean startInTransaction() {
        return deligate.startInTransaction()
    }
}
