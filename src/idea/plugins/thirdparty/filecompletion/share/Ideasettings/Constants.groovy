package idea.plugins.thirdparty.filecompletion.share.Ideasettings

import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

@CompileStatic
class Constants {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    static final String pluginName = 'OpenFileFromSourceSettings'
}
