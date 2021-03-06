package edu.students.kse.fixoe;

import akka.actor.ActorRef;
import edu.students.kse.me.MEIdGenerator;
import edu.students.kse.me.enums.OrderSide;
import edu.students.kse.me.enums.OrderTimeQualifier;
import edu.students.kse.me.enums.OrderType;
import edu.students.kse.me.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;
import quickfix.fix50sp2.OrderCancelRequest;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import util.FixFieldMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static quickfix.Acceptor.*;

public class FixApplication extends quickfix.fix50sp2.MessageCracker implements Application {

    private final Logger logger = LoggerFactory.getLogger(FixApplication.class);

    private final ActorRef fsaRef;

    private SessionSettings settings;
    private MessageStoreFactory storeFactory;
    private MessageFactory messageFactory;
    private LogFactory logFactory;
    private SocketAcceptor acceptor;
    private final MEIdGenerator generator;

    private final ConcurrentHashMap<SessionID, Session> createdSessions = new ConcurrentHashMap<>();
    private final Map<InetSocketAddress, List<DynamicAcceptorSessionProvider.TemplateMapping>> dynamicSessionMappings;


    public FixApplication(ActorRef fsaRef, InputStream sessionConfig) {
        this.generator = new MEIdGenerator();
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
    public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound {

        MENewOrderMessage meOrderMessage = getConvertedNewOrderMessage(message);
        fsaRef.tell(meOrderMessage, ActorRef.noSender());
        logger.info("FIX message sent to ME");
    }

    @Override
    public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        MECancelMessage cancelMessage = getConvertedCancelOrderMessage(message);
        fsaRef.tell(cancelMessage, ActorRef.noSender());
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
            return null;
        } else if (msg instanceof MEExecutionReport) {
            MEExecutionReport meExecutionReport = (MEExecutionReport) msg;
            logger.info(msg.toString());
            ExecutionReport executionReport = new ExecutionReport();
            executionReport.set(new ExecID(meExecutionReport.getExecId()));
            executionReport.getHeader().setField(new SenderCompID(meExecutionReport.getClientId()));
            executionReport.set(new ClOrdID(meExecutionReport.getClientOrderId()));
            executionReport.set(new OrderID(meExecutionReport.getOrderId()));
            executionReport.set(new ExecType(meExecutionReport.getExecType().getCode()));
            executionReport.set(new OrdStatus(meExecutionReport.getOrderStatus().getCode()));
            executionReport.set(new Side(meExecutionReport.getSide().getCode()));
            executionReport.set(new LeavesQty(meExecutionReport.getQty().doubleValue()));
            executionReport.set(new CumQty(meExecutionReport.getExecutedQty().doubleValue()));
            executionReport.set(new Symbol(meExecutionReport.getSymbol()));
            if (meExecutionReport.getTradeMatchId() != null) {
                executionReport.set(new TrdMatchID(meExecutionReport.getTradeMatchId()));
            }
            return executionReport;
        }
        else if (msg instanceof METradeMessage){
            logger.info(msg.toString());
            return null;
        }
        else {
            throw new UnsupportedMessageType();
        }
    }

    private MENewOrderMessage getConvertedNewOrderMessage(NewOrderSingle message) throws FieldNotFound {
        String clOrdId = message.getString(ClOrdID.FIELD);
        String clId =  message.getHeader().getString(SenderCompID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        long instrId = symbol.hashCode();
        OrderType ordType = FixFieldMapper.getOrderTypeByValue(message.getString(OrdType.FIELD));
        OrderTimeQualifier tif;
        if (message.isSetField(TimeInForce.FIELD)) {
            tif = FixFieldMapper.getOrderTimeQualifierByValue(message.getString(TimeInForce.FIELD));
        }
        else {
            tif = OrderTimeQualifier.GOOD_TILL_CANCEL;
        }
        OrderSide side = FixFieldMapper.getOrderSideByValue(message.getString(Side.FIELD));
        BigDecimal orderQty = new BigDecimal(message.getString(OrderQty.FIELD));
        BigDecimal limitPrice = null;
        if (ordType == OrderType.LIMIT || ordType == OrderType.STOP_LIMIT) {
            limitPrice = new BigDecimal(message.getString(Price.FIELD));
        }
        BigDecimal stopPrice = null;
        if (ordType == OrderType.STOP || ordType == OrderType.STOP_LIMIT) {
            stopPrice = new BigDecimal(message.getString(StopPx.FIELD));
        }
        String orderId = generator.getNextOrderId();
        return new MENewOrderMessage(clOrdId, orderId, clId, instrId, ordType, tif, side, orderQty, orderQty, limitPrice, stopPrice, symbol);
    }

    private MECancelMessage getConvertedCancelOrderMessage(OrderCancelRequest message) throws FieldNotFound {
        String clOrdId = message.getString(ClOrdID.FIELD);
        String clId = message.getHeader().getString(SenderCompID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        long instrId = symbol.hashCode();
        String originalClientOrderId = message.getString(OrigClOrdID.FIELD);
        String orderId = generator.getNextOrderId();
        OrderSide side = FixFieldMapper.getOrderSideByValue(message.getString(Side.FIELD));
        return new MECancelMessage(clOrdId, clId, originalClientOrderId, orderId, instrId, side, symbol);
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
