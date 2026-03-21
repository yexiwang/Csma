package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{empId}/{role}/{diningPointId}")
@Slf4j
public class WebSocketServer {

    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String TOKEN_PARAM = "token";
    private static JwtProperties jwtProperties;

    // 以 sessionId 作为 key，允许同一操作员开多个页面连接
    private static final Map<String, ClientConnection> CONNECTIONS = new ConcurrentHashMap<>();

    @Autowired
    public void setJwtProperties(JwtProperties jwtProperties) {
        WebSocketServer.jwtProperties = jwtProperties;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("empId") Long empId,
                       @PathParam("role") String role,
                       @PathParam("diningPointId") Long diningPointId) {
        ClientConnection connection = validateConnection(session, empId, role, diningPointId);
        if (connection == null) {
            return;
        }

        CONNECTIONS.put(session.getId(), connection);
        log.info("WebSocket连接建立: sessionId={}, empId={}, role={}, diningPointId={}",
                session.getId(), connection.getEmpId(), connection.getRole(), connection.getDiningPointId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息: sessionId={}, message={}", session.getId(), message);
    }

    @OnClose
    public void onClose(Session session) {
        CONNECTIONS.remove(session.getId());
        log.info("WebSocket连接关闭: sessionId={}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (session != null) {
            CONNECTIONS.remove(session.getId());
            log.warn("WebSocket连接异常: sessionId={}", session.getId(), throwable);
            return;
        }
        log.warn("WebSocket连接异常", throwable);
    }

    public void sendToAllClient(String message) {
        Collection<ClientConnection> connections = CONNECTIONS.values();
        for (ClientConnection connection : connections) {
            sendMessage(connection.getSession(), message);
        }
    }

    public void sendToOperatorsByDiningPoint(Long diningPointId, String message) {
        if (diningPointId == null) {
            log.warn("忽略WebSocket定向推送，因为助餐点为空");
            return;
        }

        for (ClientConnection connection : CONNECTIONS.values()) {
            if (!ROLE_OPERATOR.equalsIgnoreCase(normalizeRole(connection.getRole()))) {
                continue;
            }
            if (!diningPointId.equals(connection.getDiningPointId())) {
                continue;
            }
            sendMessage(connection.getSession(), message);
        }
    }

    private void sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            CONNECTIONS.remove(session.getId());
            try {
                session.close();
            } catch (Exception closeException) {
                log.warn("关闭异常WebSocket连接失败: sessionId={}", session.getId(), closeException);
            }
            log.warn("WebSocket消息发送失败: sessionId={}", session.getId(), e);
        }
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim();
    }

    private ClientConnection validateConnection(Session session, Long pathEmpId, String pathRole, Long pathDiningPointId) {
        String normalizedPathRole = normalizeRole(pathRole);
        if (!ROLE_OPERATOR.equalsIgnoreCase(normalizedPathRole)) {
            log.warn("拒绝WebSocket连接，非法角色: sessionId={}, role={}", session.getId(), pathRole);
            closeUnauthorized(session, "仅操作员允许建立订单提醒连接");
            return null;
        }

        if (jwtProperties == null) {
            log.error("拒绝WebSocket连接，JwtProperties未初始化: sessionId={}", session.getId());
            closeUnauthorized(session, "服务端认证配置未就绪");
            return null;
        }

        String token = extractToken(session);
        if (token == null || token.trim().isEmpty()) {
            log.warn("拒绝WebSocket连接，缺少token: sessionId={}", session.getId());
            closeUnauthorized(session, "缺少认证令牌");
            return null;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long tokenEmpId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            String tokenRole = claims.get(JwtClaimsConstant.ROLE) == null
                    ? ""
                    : normalizeRole(claims.get(JwtClaimsConstant.ROLE).toString());
            Long tokenDiningPointId = claims.get(JwtClaimsConstant.DINING_POINT_ID) == null
                    ? null
                    : Long.valueOf(claims.get(JwtClaimsConstant.DINING_POINT_ID).toString());

            if (!ROLE_OPERATOR.equalsIgnoreCase(tokenRole)) {
                log.warn("拒绝WebSocket连接，token角色不合法: sessionId={}, role={}", session.getId(), tokenRole);
                closeUnauthorized(session, "仅操作员允许建立订单提醒连接");
                return null;
            }

            if (tokenDiningPointId == null || tokenEmpId == null) {
                log.warn("拒绝WebSocket连接，token缺少必要身份字段: sessionId={}", session.getId());
                closeUnauthorized(session, "认证信息不完整");
                return null;
            }

            if (!tokenEmpId.equals(pathEmpId)
                    || !tokenDiningPointId.equals(pathDiningPointId)
                    || !tokenRole.equalsIgnoreCase(normalizedPathRole)) {
                log.warn("拒绝WebSocket连接，URL身份与token不匹配: sessionId={}, pathEmpId={}, tokenEmpId={}, pathRole={}, tokenRole={}, pathDiningPointId={}, tokenDiningPointId={}",
                        session.getId(), pathEmpId, tokenEmpId, pathRole, tokenRole, pathDiningPointId, tokenDiningPointId);
                closeUnauthorized(session, "连接身份校验失败");
                return null;
            }

            return new ClientConnection(session, tokenEmpId, tokenRole, tokenDiningPointId);
        } catch (Exception ex) {
            log.warn("拒绝WebSocket连接，token校验失败: sessionId={}", session.getId(), ex);
            closeUnauthorized(session, "认证令牌无效");
            return null;
        }
    }

    private String extractToken(Session session) {
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        if (requestParameterMap == null) {
            return null;
        }

        List<String> tokens = requestParameterMap.get(TOKEN_PARAM);
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        return tokens.get(0);
    }

    private void closeUnauthorized(Session session, String reason) {
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (Exception ex) {
            log.warn("关闭未授权WebSocket连接失败: sessionId={}", session.getId(), ex);
        }
    }

    @AllArgsConstructor
    private static class ClientConnection {
        private final Session session;
        private final Long empId;
        private final String role;
        private final Long diningPointId;

        public Session getSession() {
            return session;
        }

        public Long getEmpId() {
            return empId;
        }

        public String getRole() {
            return role;
        }

        public Long getDiningPointId() {
            return diningPointId;
        }
    }
}
