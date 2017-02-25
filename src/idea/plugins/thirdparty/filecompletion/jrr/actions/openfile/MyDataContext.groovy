package idea.plugins.thirdparty.filecompletion.jrr.actions.openfile

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.vfs.VirtualFile
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NonNls

@CompileStatic
class MyDataContext implements DataContext {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    DataContext deligateDc;

    VirtualFile file;

    MyDataContext(DataContext deligateDc, VirtualFile file) {
        this.deligateDc = deligateDc
        this.file = file
    }

    @Override
    Object getData(@NonNls String dataId) {
        log.debug "dataId : ${dataId}"
        if (dataId == 'virtualFile') {
            return file
        }
        return deligateDc.getData(dataId)
    }
}
