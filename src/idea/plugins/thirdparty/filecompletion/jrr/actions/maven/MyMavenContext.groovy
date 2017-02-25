package idea.plugins.thirdparty.filecompletion.jrr.actions.maven

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.vfs.VirtualFile
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import net.sf.jremoterun.utilities.classpath.MavenId
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.jetbrains.annotations.NonNls


@CompileStatic
public class MyMavenContext  implements DataContext {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    DataContext deligateDc;

    MavenId file;

    MyMavenContext(DataContext deligateDc, MavenId file) {
        this.deligateDc = deligateDc
        this.file = file
    }

    @Override
    Object getData(@NonNls String dataId) {
        log.debug "dataId : ${dataId}"
        switch (dataId){
            case 'groupId':
                return file.groupId
            case 'artifactId':
                return file.artifactId
            case 'version':
                return file.version
            case 'mavenId':
                return file.toString()
        }
        return deligateDc.getData(dataId)
    }


}
