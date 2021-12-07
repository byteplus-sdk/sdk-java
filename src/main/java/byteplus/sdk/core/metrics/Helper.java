package byteplus.sdk.core.metrics;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Helper {

    public static String processTags(TreeMap<String, String> treeMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    public static Map<String, String> recoverTags(String tagString) {
        Map<String, String> tags = new HashMap<>();
        for (String entry : tagString.split("\\|")) {
            String[] keyValue = entry.split("=");
            if (keyValue.length != 2) {
                continue;
            }

            tags.put(keyValue[0], keyValue[1]);
        }
        return tags;
    }

    public static TreeMap<String, String> appendTags(Map<String, String> baseTags, String[] tagKvs) {
        TreeMap<String, String> tags = new TreeMap<>(baseTags);
        for (String kv : tagKvs) {
            String[] tagFields = kv.split(":", 2);
            tags.put(tagFields[0], tagFields[1]);
        }
        return tags;
    }

    @Slf4j
    public static class LocalHostUtil {
        private static String ip;

        public static String getHostAddr() {
            if (ip != null) return ip;
            try {
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                    NetworkInterface item = e.nextElement();
                    for (InterfaceAddress address : item.getInterfaceAddresses()) {
                        if (item.isLoopback() || !item.isUp()) {
                            continue;
                        }
                        if (address.getAddress() instanceof Inet4Address) {
                            Inet4Address inet4Address = (Inet4Address) address.getAddress();
                            ip = inet4Address.getHostAddress();
                            return ip;
                        }
                    }
                }
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                log.error("get local ip failed: {} \n {}", e.getMessage(), ExceptionUtil.getTrace(e));
            }
            return "localhost";
        }
    }

    public static class ExceptionUtil {
        public static String getTrace(Throwable throwable) {
            if (throwable == null) {
                return "";
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.getBuffer().toString();
        }
    }

    public static class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup group;

        private final AtomicInteger threadNum = new AtomicInteger(1);

        private final String namePrefix;

        private final boolean daemon;

        public NamedThreadFactory(String name) {
            this(name, false);
        }

        public NamedThreadFactory(String name, boolean daemon) {
            final SecurityManager sm = System.getSecurityManager();
            this.group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "byteplus-" + name + "-thread-";
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNum.getAndIncrement(), 0);
            t.setDaemon(daemon);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
