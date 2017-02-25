package idea.plugins.thirdparty.filecompletion.jrr.file

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.StaticallyImportable
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.codeInsight.lookup.LookupElementPresentation
import groovy.transform.CompileStatic
import net.sf.jremoterun.utilities.JrrClassUtils
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import javax.swing.Icon

@CompileStatic
class FileLookupElement extends LookupElementDecorator implements StaticallyImportable {
    private static final Logger log = LogManager.getLogger(JrrClassUtils.currentClass);

    File file;
    Icon icon

    protected FileLookupElement(LookupElement delegate, File file, Icon icon) {
        super(delegate)
        this.file = file;
        this.icon = icon
    }



    @Override
    void setShouldBeImported(boolean shouldImportStatic) {
        log.debug "shouldImportStatic ${shouldImportStatic}"
    }

    @Override
    boolean canBeImported() {
        // log.debug "canBeImported"
        return true
    }

    @Override
    boolean willBeImported() {
        // log.debug "willBeImported"
        return true
    }



    @Override
    void renderElement(LookupElementPresentation presentation) {
        String text = file.name;
        if(text==null||text.length() ==0){
            text = file.absolutePath.tr('\\','/')
        }else {
            if (file.isDirectory()) {
                text += '/'
            }
            //text += ' '+(file.isFile()?'f':'d')
        }
        presentation.icon = icon
        presentation.setItemText(text);
    }

    @Override
    void handleInsert(InsertionContext context) {
        log.debug "handle insert ${context}"
//        super.handleInsert(context)
    }
}
