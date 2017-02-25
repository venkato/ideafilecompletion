package idea.plugins.thirdparty.filecompletion.jrr.jrrlib

enum ReflectionElement {

    setFieldValue, findField, findMethod, getFieldValue, invokeMethod;

    public static EnumSet<ReflectionElement> reFields = EnumSet.of(ReflectionElement.setFieldValue,
            ReflectionElement.getFieldValue, ReflectionElement.findField);

    public static EnumSet<ReflectionElement> reMethods = EnumSet.of(ReflectionElement.invokeMethod,
            ReflectionElement.findMethod);

}
