package edu.students.kse.fixoe;

import akka.actor.ActorRef;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;
import edu.students.kse.me.messages.MEOutputMessage;
import edu.students.kse.me.messages.TransactionComplete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Symbol;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

public class FixApplication extends quickfix.fix50sp2.MessageCracker implements Application {

    private final Logger logger = LoggerFactory.getLogger(FixApplication.class);

    private final ActorRef fsaRef;

    private SessionSettings settings;
    private MessageStoreFactory storeFactory;
    private MessageFactory messageFactory;
    private LogFactory logFactory;
    private SocketAcceptor acceptor;

    private final Map<SessionID, Session> createdSessions = new HashMap<>();
    private final Map<InetSocketAddress, List<DynamicAcceptorSessionProvider.TemplateMapping>> dynamicSessionMappings;


    public FixApplication(ActorRef fsaRef, InputStream sessionConfig) {
        this.fsaRef = fsaRef;
        try {
            settings = new SessionSettings(sessionConfig);
        } catch (ConfigError configError) {
            logger.error("Cannot initialize FIX session settings", configError);
        }

        storeFactory = settings.getDefaultProperties().containsKey("FileStorePath")
                ? new FileStoreFactory(settings)
                : new MemoryStoreFactory();

        logFactory = settings.getDefaultProperties().containsKey("FileLogPath")
                ? new FileLogFactory(settings)
                : new SLF4JLogFactory(settings);

        messageFactory = new DefaultMessageFactory();

        try {
            acceptor = new SocketAcceptor(this, storeFactory, settings, logFactory, messageFactory);
        } catch (ConfigError configError) {
            logger.error("Cannot initialize FIX acceptor", configError);
        }

        dynamicSessionMappings = new HashMap<>();
        try {
            configureDynamicSessions(settings, this, storeFactory, logFactory, messageFactory);
        } catch (ConfigError|FieldConvertError error) {
            logger.error("Could not initialize FIX dynamic sessions", error);
        }

        logger.info("FIX Server initialized");
    }


    @Override
    public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MENewOrderMessage meOrderMessage = getConvertedMessage(message, sessionID);
        fsaRef.tell(meOrderMessage, ActorRef.noSender());
        logger.info("FIX message sent to ME");
    }

    public void start() {
        try {

            acceptor.start();
            logger.info("FIX acceptor started");
        } catch (ConfigError configError) {
            logger.error("Failed to start FIX acceptor", configError);
        }
    }

    public void stop() {
        acceptor.stop();
        logger.info("FIX acceptor stopped");
    }

    @Override
    public void onCreate(SessionID sessionId) {
        Session session = Session.lookupSession(sessionId);

        if (session == null)
            return;

        createdSessions.put(sessionId, session);
    }

    @Override
    public void onLogon(SessionID sessionId) {

    }

    @Override
    public void onLogout(SessionID sessionId) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
    }

    public void sendMessage(MEOutputMessage msg) {
        // TODO: getting sessionId
        try {
            quickfix.fix50sp2.Message outputMessage = getOutputMessage(msg);
            if (outputMessage != null) {
                createdSessions.values().forEach((session) -> {
                    session.send(outputMessage);
                });
            }
        } catch (UnsupportedMessageType unsupportedMessageType) {
            logger.error("Message cannot be converted", unsupportedMessageType);
        }
    }


    private quickfix.fix50sp2.Message getOutputMessage(MEOutputMessage msg) throws UnsupportedMessageType {
        if (msg instanceof TransactionComplete) {
            // TODO: add support transaction complete messages
            return null;
        } else if (msg instanceof MEExecutionReport) {
            return new ExecutionReport();
        } else {
            throw new UnsupportedMessageType();
        }
    }

    private MENewOrderMessage getConvertedMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound {
        long instrId = message.getString(Symbol.FIELD).hashCode(); // FIXME: add mapping Symbol
        BigDecimal price = new BigDecimal(message.getString(Price.FIELD));
        BigDecimal size = new BigDecimal(message.getString(OrderQty.FIELD));
        return new MENewOrderMessage(sessionID.toString(), instrId, price, size);
    }

    // Dynamic sessions:

    private void configureDynamicSessions(SessionSettings settings, Application application,
                                          MessageStoreFactory messageStoreFactory, LogFactory logFactory,
                                          MessageFactory messageFactory) throws ConfigError, FieldConvertError {

        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();

            if (isSessionTemplate(settings, sessionID)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(new DynamicAcceptorSessionProvider.TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress, List<DynamicAcceptorSessionProvider.TemplateMapping>> entry : dynamicSessionMappings.entrySet()) {
            acceptor.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<DynamicAcceptorSessionProvider.TemplateMapping> getMappings(InetSocketAddress address) {
        return dynamicSessionMappings.computeIfAbsent(address, k -> new ArrayList<>());
    }

    private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings, SessionID sessionID) throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";

        if (settings.isSetting(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS);
        }

        int acceptorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_ACCEPT_PORT);

        return new InetSocketAddress(acceptorHost, acceptorPort);
    }

    private boolean isSessionTemplate(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {

        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }
}