package de.fraunhofer.iais.eis.ids.component.core.logging;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class provides logging functionality for inbound messages
 */
public class MessageLogger {


    /**
     * The severity with which messages should be logged. Use NONE to not log messages at all
     */
    public enum Severity {NONE, DEBUG, INFO, WARN}

    /**
     * The severity with which messages should be logged. Use NONE to not log messages at all
     */
    public static Severity severity = Severity.INFO;

    /**
     * If set to true, all log entries produced by this class will contain the name of the class invoking the logging
     */
    public static boolean includeCallingClassName = true;

    /**
     * If set to true, the entire message will be logged as is
     */
    public static boolean logEntireMessage = false;


    private static final Logger logger = LoggerFactory.getLogger(MessageLogger.class);

    /**
     * This function provides consistent logging functionality for given messages. Configuration of the logged messages is available via the static variables of this class
     * @param m Message to be logged
     * @param fieldNamesToLog If the message contains fields which should be logged explicitly, provide them here
     */
    public static void logMessage(Message m, String... fieldNamesToLog)
    {
        //Is logging disabled? If so, return immediately
        if(severity.equals(Severity.NONE)) return;

        //Name of incoming message class (minus trailing "Impl")
        String className = m.getClass().getSimpleName();
        if(className.endsWith("Impl"))
        {
            className = className.substring(0, className.length() - 4);
        }
        //Basic information, containing class name and issuer
        StringBuilder messageToLog = new StringBuilder(className + " received from " + m.getIssuerConnector());

        //Should we include the name of the class which invoked this function call?
        if(includeCallingClassName)
        {
            //0 = Thread, 1 = This class, 2 = Direct caller, see https://stackoverflow.com/questions/11306811/how-to-get-the-caller-class-in-java
            //Note: This is the fully qualified class name, not the simple name
            messageToLog.append(" (called by ").append(Thread.currentThread().getStackTrace()[2].getClassName()).append(")");
        }



        //Should we log the message "as is"?
        if(logEntireMessage)
        {
            messageToLog.append("\n").append(m.toRdf());
        }
        else
        {
            //Only additionally log certain fields, if we are not logging entire message anyways
            if(fieldNamesToLog != null && fieldNamesToLog.length != 0)
            {
                //For each field name
                for(String fieldString : fieldNamesToLog) {
                    try {
                        //Getter method starts with "get" and then the field name, with first character capitalized
                        String methodName = "get" + String.valueOf(fieldString.charAt(0)).toUpperCase() + fieldString.substring(1);
                        //Attempt to retrieve the respective field via public getter method
                        Method getter = m.getClass().getMethod(methodName);
                        Object valueToLog = getter.invoke(m);
                        String stringValueToLog = valueToLog == null? "null" : valueToLog.toString();
                        messageToLog.append("\n").append(fieldString).append(": ").append(stringValueToLog);
                    }
                    catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
                    {
                        logger.error("Could not log field " + fieldString, e);
                    }
                }
            }
        }

        switch (severity)
        {
            case DEBUG: logger.debug(messageToLog.toString()); break;
            case INFO: logger.info(messageToLog.toString()); break;
            case WARN: logger.warn(messageToLog.toString()); break;
            //Not supporting error, as it makes no sense to log incoming messages as error. Arguably, this might also hold true for WARN.
        }
    }

    /**
     * This function offers functionality for logging a MessageAndPayload object
     * @param map MessageAndPayload to be logged
     * @param logOnlyPayloadHash If set to true, only a hash value will be logged for the payload. If set to false, the function will attempt to log the entire payload. This can be problematic on binary input
     * @param fieldNamesToLog If the message contains fields which should be logged explicitly, provide them here
     */
    public static void logMessage(MessageAndPayload<?, ?> map, boolean logOnlyPayloadHash, String... fieldNamesToLog)
    {
        //If logging is turned off, return immediately
        if(severity.equals(Severity.NONE)) return;

        //Invoke the method for logging the message header
        logMessage(map.getMessage(), fieldNamesToLog);

        //Capture what we need to log to a string. Once we got the entire message, decide upon logging severity
        String messageToLog;

        //Is there a payload?
        if(map.getPayload().isPresent()) {
            //Should we log only a hash value?
            if (logOnlyPayloadHash) {
                //Using given hashCode function, probably provided by Java
                messageToLog = "Payload hash: " + map.getPayload().get().hashCode();
            }
            else
            {
                //Attempting to log entire payload
                messageToLog = "Message payload:\n";
                //Is the payload already a string?
                if(String.class.isAssignableFrom(map.getPayload().get().getClass()))
                {
                    //It is a string. Just append it
                    messageToLog += map.getPayload().get();
                }
                else
                {
                    try {
                        //It is not a string. Check if this is one of "our" IDS information model classes, which provide a "toRdf" method
                        //Try to get this method and invoke it. If it goes wrong (see catch clause), it is a different type of class
                        messageToLog += map.getPayload().get().getClass().getMethod("toRdf").invoke(map.getPayload().get());
                    }
                    catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
                    {
                        //Not a string and not an IDS information model class. Falling back to "toString"
                        messageToLog += map.getPayload().get().toString();
                    }
                }
            }
        }
        else
        {
            //The MessageAndPayload did not have a payload
            messageToLog = "Message did not have a payload.";
        }

        //Do the actual logging with the predefined severity
        switch (severity)
        {
            case DEBUG: logger.debug(messageToLog); break;
            case INFO: logger.info(messageToLog); break;
            case WARN: logger.warn(messageToLog); break;
        }
    }
}
