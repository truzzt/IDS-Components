package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class has identical behaviour to its superclass. However, if we reject a message due to a contract, we need to send a different RejectionMessage type
 * Using this class for throwing an exception indicates that the failure was caused due to a contract
 */
public class ContractRejectMessageException extends RejectMessageException {
    private final TypedLiteral contractRejectionReason;
    private String rejectionPayload;

    Logger logger = LoggerFactory.getLogger(ContractRejectMessageException.class);


    public ContractRejectMessageException(TypedLiteral contractRejectionReason) {
        super();
        this.contractRejectionReason = contractRejectionReason;
    }

    public ContractRejectMessageException(TypedLiteral contractRejectionReason, Throwable cause) {
        super(cause);
        this.contractRejectionReason = contractRejectionReason;
        this.rejectionPayload = cause.getMessage();
        if(rejectionPayload == null || rejectionPayload.equals("Exception did not have a message"))
        {
            //For debugging - exceptions should have a message
            logger.warn("RejectMessageException without message created. Printing for debug purposes, as this results in a poor error message.", cause);
            //cause.printStackTrace();
        }
    }

    public TypedLiteral getContractRejectionReason() {
        return contractRejectionReason;
    }

    public String getRejectionPayload() {
        //Fixes NullPointerException
        if(rejectionPayload == null)
        {
            return "Exception did not have a message";
        }
        return rejectionPayload;
    }
}
