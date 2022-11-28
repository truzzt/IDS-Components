package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.RejectionReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RejectMessageException extends Exception {

    private RejectionReason rejectionReason;
    private String rejectionPayload;

    Logger logger = LoggerFactory.getLogger(RejectMessageException.class);

    //Default constructor - internal only. Used by ContractRejectMessageException
    protected RejectMessageException(){}

    //Internal only. Used by ContractRejectMessageException
    protected RejectMessageException(Throwable throwable) {super(throwable);}

    public RejectMessageException(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public RejectMessageException(RejectionReason rejectionReason, Throwable cause) {
        super(cause);
        this.rejectionReason = rejectionReason;
        this.rejectionPayload = cause.getMessage();
        if(rejectionPayload == null || rejectionPayload.equals("Exception did not have a message"))
        {
            //For debugging - exceptions should have a message
            logger.warn("RejectMessageException without message created. Printing for debug purposes, as this results in a poor error message.", cause);
            //cause.printStackTrace();
        }
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public String getRejectionPayload() {
        //Fixes NullPointerException
        if(rejectionPayload == null)
        {
            logger.warn("No message in exception", this);
            return "Exception did not have a message";
        }
    	return rejectionPayload;
    }
}
