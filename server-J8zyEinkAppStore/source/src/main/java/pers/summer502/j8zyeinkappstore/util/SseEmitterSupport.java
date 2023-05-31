package pers.summer502.j8zyeinkappstore.util;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SseEmitterSupport {
    private static final Map<String, SSE> sseMap = new ConcurrentHashMap<>();

    private SseEmitterSupport() {
    }

    public static SseEmitter startSseEmitter(String clientId, Long timeout) {
        Assert.notNull(clientId, "startSseEmitter, clientId is null");
        SseEmitter sseEmitter;
        if (timeout == null) {
            //    sseEmitter = new SseEmitter();//使用的是web容器的默认值30s，tomcat
            // 24小时
            sseEmitter = new SseEmitter(24 * 60 * 60 * 1000L);
            // SseEmitter sseEmitter = new SseEmitter(0L);//永不不超时
        } else {
            sseEmitter = new SseEmitter(timeout);
        }

        SSE sse = new SSE();
        sse.createTime = System.currentTimeMillis();
        sse.clientId = clientId;
        sse.sseEmitter = sseEmitter;
        sseMap.put(clientId, sse);

        return sseEmitter;
    }

    public static int sendMessageToAllClient(Object object) {
        return sendMessageToAllClient(object, null);
    }

    public static int sendMessageToAllClient(Object object, MediaType mediaType) {
        return sendMessageToAllClient(SseEmitter.event().data(object, mediaType));
    }

    public static int sendMessageToAllClient(SseEmitter.SseEventBuilder builder) {
        int counter = 0;
        if (sseMap.isEmpty()) {
            return counter;
        }
        for (Map.Entry<String, SSE> entry : sseMap.entrySet()) {
            String clientId = entry.getKey();
            SSE sse = entry.getValue();
            Long timeout = sse.sseEmitter.getTimeout();
            if (timeout != null && timeout > 0L) {
                if (System.currentTimeMillis() - sse.createTime >= timeout) {
                    sseMap.remove(clientId);
                    sse.sseEmitter.complete();
                    continue;
                }
            }
            try {
                sse.sseEmitter.send(builder);
                counter++;
            } catch (Exception e) {
                // Exception
                sseMap.remove(clientId);
                sse.sseEmitter.completeWithError(e);
            }
        }

        return counter;
    }

    public static List<String> getAllClientId() {
        if (sseMap.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Map.Entry<String, SSE>> entries = sseMap.entrySet();
        ArrayList<String> objects = new ArrayList<>(entries.size());
        for (Map.Entry<String, SSE> entry : entries) {
            String clientId = entry.getKey();
            SSE sse = entry.getValue();
            Long timeout = sse.sseEmitter.getTimeout();
            if (timeout != null && timeout > 0L) {
                if (System.currentTimeMillis() - sse.createTime >= timeout) {
                    sseMap.remove(clientId);
                    sse.sseEmitter.complete();
                    continue;
                }
            }
            objects.add(clientId);
        }

        return objects;
    }

    public static List<SseEmitter> getAllSSE() {
        if (sseMap.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SSE> values = sseMap.values();
        ArrayList<SseEmitter> objects = new ArrayList<>(values.size());
        Iterator<SSE> iterator = values.iterator();
        while (iterator.hasNext()) {
            SSE sse = iterator.next();
            Long timeout = sse.sseEmitter.getTimeout();
            if (timeout != null && timeout > 0L) {
                if (System.currentTimeMillis() - sse.createTime >= timeout) {
                    iterator.remove();
                    sse.sseEmitter.complete();
                    continue;
                }
            }
            objects.add(sse.sseEmitter);
        }

        return objects;
    }

    public static SseEmitter getSSE(String clientId) {
        Assert.notNull(clientId, "getSSE, clientId is null");
        SSE sse = sseMap.get(clientId);
        if (sse == null) {
            return null;
        }
        Long timeout = sse.sseEmitter.getTimeout();
        if (timeout != null && timeout > 0L) {
            if (System.currentTimeMillis() - sse.createTime >= timeout) {
                sseMap.remove(clientId);
                sse.sseEmitter.complete();
                return null;
            }
        }

        return sse.sseEmitter;
    }

    public static void removeSSE(String clientId) {
        Assert.notNull(clientId, "removeSSE, clientId is null");
        sseMap.remove(clientId);
    }

    private static class SSE {
        public String clientId;
        public long createTime;
        public SseEmitter sseEmitter;
    }
}
