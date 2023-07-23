package icu.chiou;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
public class ServiceConfig<T> {
    private Class<T> interfaceRef;
    private Object ref;

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public Object getRef() {
        return ref;
    }
}
