package idea.plugins.thirdparty.filecompletion.share

import groovy.transform.CompileStatic
import idea.plugins.thirdparty.filecompletion.jrr.actions.RemoteRunActionImpl
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
class RemoteRunAction extends DeligateAction {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    public static DeligateAction thisObject

    public RemoteRunAction() {
        super(new RemoteRunActionImpl())
        if (thisObject == null) {
            thisObject = this;
        } else {
            log.error("object already created")
        }
    }

}
