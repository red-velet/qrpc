package icu.chiou;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
public class ServiceConfig<T> {
    private Class<?> interfaceRef;
    private Object ref;

    public void setInterface(Class<?> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public Class<?> getInterface() {
        return interfaceRef;
    }

    public Object getRef() {
        return ref;
    }
}
