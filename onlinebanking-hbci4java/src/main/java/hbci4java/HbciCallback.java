package hbci4java;

import exception.InvalidPinException;
import org.kapott.hbci.GV.GVTAN2Step;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by alexg on 08.02.17.
 */
public class HbciCallback implements HBCICallback {

    private static final Logger LOG = LoggerFactory.getLogger(HbciCallback.class);

    @Override
    public void log(String msg, int level, Date date, StackTraceElement trace) {
        String msg2 = preprocessLogMsg(msg);
        if (shouldPrintTrace(trace)) {
            switch (level) {
                case HBCIUtils.LOG_ERR:
                    LOG.error("Log: {} (at {})", msg2, trace);
                    break;
                case HBCIUtils.LOG_WARN:
                    LOG.warn("Log: {} (at {})", msg2, trace);
                    break;
                case HBCIUtils.LOG_INFO:
                    LOG.info("Log: {} (at {})", msg2, trace);
                    break;
                case HBCIUtils.LOG_DEBUG:
                    LOG.debug("Log: {} (at {})", msg2, trace);
                    break;
                case HBCIUtils.LOG_DEBUG2:
                    LOG.trace("Log: {} (at {})", msg2, trace);
                    break;
                default:
                    break;
            }
        } else {
            switch (level) {
                case HBCIUtils.LOG_ERR:
                    LOG.error("Log: {}", msg2);
                    break;
                case HBCIUtils.LOG_WARN:
                    LOG.warn("Log: {}", msg2);
                    break;
                case HBCIUtils.LOG_INFO:
                    LOG.info("Log: {}", msg2);
                    break;
                case HBCIUtils.LOG_DEBUG:
                    LOG.debug("Log: {}", msg2);
                    break;
                case HBCIUtils.LOG_DEBUG2:
                    LOG.trace("Log: {}", msg2);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * We want to suppress the (at ...) info for calls from hbci4java.
     * This is the same thing done here: {@link org.kapott.hbci.callback.AbstractHBCICallback#createDefaultLogLine}.
     */
    boolean shouldPrintTrace(StackTraceElement trace) {
        String className = trace.getClassName();
        return !className.startsWith("org.kapott.hbci.");
    }

    /**
     * Removes all but the first line of the complete stacktraces that hbci4java generates when an exception has occured.
     */
    private String preprocessLogMsg(String msg) {
        /** the (?s) turns on {@link Pattern#DOTALL} mode. */
        return msg.replaceFirst("(?s)\\n\\s*at org\\.kapott\\.hbci.*", "");
    }

    @Override
    public void callback(int reason, String msg, int datatype, StringBuffer retData) {
        switch (reason) {
            case HBCICallback.NEED_PT_PHOTOTAN: {
                String hhduc = retData.toString();
                break;
            }
            case HBCICallback.NEED_PT_TAN: {
                String flicker = retData.toString();
                if (flicker != null && flicker.length() > 0) {
                    // Wir haben einen Flicker-Code. Also zeigen wir den Flicker-Dialog statt
                    // dem normalen TAN-Dialog an

                } else {

                }
                break;
            }
            // BUGZILLA 200
            case HBCICallback.NEED_PT_SECMECH: {
                String firstTanMethod = retData.toString().substring(0, retData.toString().indexOf(':'));
                retData.setLength(0);
                retData.insert(0, firstTanMethod);
                break;
            }

            case HBCICallback.WRONG_PIN: {
                throw new InvalidPinException();
            }

            // No need to tell when we may open or close our internet connection
            case HBCICallback.NEED_CONNECTION:
            case HBCICallback.CLOSE_CONNECTION:
                LOG.debug("Callback: reason: {}, message: {}", reason, msg);
                break;
            default:
                LOG.warn("Callback: reason: {}, message: {}", reason, msg);
        }
    }

    @Override
    public String tanMediaCallback(String medialist) {
        return null;
    }

    @Override
    public void tanCallback(HBCIPassport passport, GVTAN2Step hktan) {
    }

    @Override
    public void status(int statusTag, Object[] o) {
        LOG.debug("Status: {} {}, objects: {}", statusTag, statusToString(statusTag), o);
    }

    @Override
    public void status(int statusTag, Object o) {
        status(statusTag, new Object[]{o});
    }

    String statusToString(int status) {
        switch (status) {
            case STATUS_SEND_TASK:
                return "STATUS_SEND_TASK";
            case STATUS_SEND_TASK_DONE:
                return "STATUS_SEND_TASK_DONE";
            case STATUS_INST_BPD_INIT:
                return "STATUS_INST_BPD_INIT";
            case STATUS_INST_BPD_INIT_DONE:
                return "STATUS_INST_BPD_INIT_DONE";
            case STATUS_INST_GET_KEYS:
                return "STATUS_INST_GET_KEYS";
            case STATUS_INST_GET_KEYS_DONE:
                return "STATUS_INST_GET_KEYS_DONE";
            case STATUS_SEND_KEYS:
                return "STATUS_SEND_KEYS";
            case STATUS_SEND_KEYS_DONE:
                return "STATUS_SEND_KEYS_DONE";
            case STATUS_INIT_SYSID:
                return "STATUS_INIT_SYSID";
            case STATUS_INIT_SYSID_DONE:
                return "STATUS_INIT_SYSID_DONE";
            case STATUS_INIT_UPD:
                return "STATUS_INIT_UPD";
            case STATUS_INIT_UPD_DONE:
                return "STATUS_INIT_UPD_DONE";
            case STATUS_LOCK_KEYS:
                return "STATUS_LOCK_KEYS";
            case STATUS_LOCK_KEYS_DONE:
                return "STATUS_LOCK_KEYS_DONE";
            case STATUS_INIT_SIGID:
                return "STATUS_INIT_SIGID";
            case STATUS_INIT_SIGID_DONE:
                return "STATUS_INIT_SIGID_DONE";
            case STATUS_DIALOG_INIT:
                return "STATUS_DIALOG_INIT";
            case STATUS_DIALOG_INIT_DONE:
                return "STATUS_DIALOG_INIT_DONE";
            case STATUS_DIALOG_END:
                return "STATUS_DIALOG_END";
            case STATUS_DIALOG_END_DONE:
                return "STATUS_DIALOG_END_DONE";
            case STATUS_MSG_CREATE:
                return "STATUS_MSG_CREATE";
            case STATUS_MSG_SIGN:
                return "STATUS_MSG_SIGN";
            case STATUS_MSG_CRYPT:
                return "STATUS_MSG_CRYPT";
            case STATUS_MSG_SEND:
                return "STATUS_MSG_SEND";
            case STATUS_MSG_DECRYPT:
                return "STATUS_MSG_DECRYPT";
            case STATUS_MSG_VERIFY:
                return "STATUS_MSG_VERIFY";
            case STATUS_MSG_RECV:
                return "STATUS_MSG_RECV";
            case STATUS_MSG_PARSE:
                return "STATUS_MSG_PARSE";
            case STATUS_SEND_INFOPOINT_DATA:
                return "STATUS_SEND_INFOPOINT_DATA";
            case STATUS_MSG_RAW_SEND:
                return "STATUS_MSG_RAW_SEND";
            case STATUS_MSG_RAW_RECV:
                return "STATUS_MSG_RAW_RECV";
            default:
                return "?";
        }
    }
}
