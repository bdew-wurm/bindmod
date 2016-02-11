package net.bdew.wurm.bind;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BindMod implements WurmMod, Initable, PreInitable {
    private static final Logger logger = Logger.getLogger("BindMod");

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    @Override
    public void init() {
        logger.fine("Initializing");
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();

            CtClass ctSocketServer = classPool.getCtClass("com.wurmonline.communication.SocketServer");
            ctSocketServer.getConstructor("([BIILcom/wurmonline/communication/ServerListener;)V").instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getByAddress")) {
                        m.replace("$_ = java.net.InetAddress.getByAddress(new byte[]{0, 0, 0, 0});");
                        logger.info("patched SocketServer at line " + m.getLineNumber());
                    }
                }
            });

            CtClass ctServerLauncher = classPool.getCtClass("com.wurmonline.server.ServerLauncher");
            ctServerLauncher.getMethod("runServer", "(Z)V").instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getByAddress")) {
                        m.replace("$_ = java.net.InetAddress.getByAddress(new byte[]{0, 0, 0, 0});");
                        logger.info("patched ServerLauncher at line " + m.getLineNumber());
                    }
                }
            });
        } catch (Throwable e) {
            logException("Error loading mod", e);
        }
    }

    @Override
    public void preInit() {

    }
}
