package pers.summer502.j8zyeinkappstore.j8server.tomcat.coyote.http11connect;

import org.apache.catalina.Loader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.tomcat.ContextBind;

public class YummyContextBind implements ContextBind {
    private Loader loader = new WebappLoader();

    @Override
    public ClassLoader bind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {
        ClassLoader webApplicationClassLoader = null;
        if (loader != null) {
            webApplicationClassLoader = loader.getClassLoader();
        }

        Thread currentThread = Thread.currentThread();
        if (originalClassLoader == null) {
            originalClassLoader = currentThread.getContextClassLoader();
        }

        if (webApplicationClassLoader == null || webApplicationClassLoader == originalClassLoader) {
            return null;
        }

        currentThread.setContextClassLoader(webApplicationClassLoader);
        return originalClassLoader;
    }

    @Override
    public void unbind(boolean usePrivilegedAction, ClassLoader originalClassLoader) {
        if (originalClassLoader == null) {
            return;
        }

        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(originalClassLoader);
    }
}
