package cc.coodex.concrete.jaxrs;

import cc.coodex.concrete.common.ErrorMessageFacade;

/**
 * Created by davidoff shen on 2016-12-01.
 */
public class ErrorDefinition implements Comparable<ErrorDefinition> {
    private int errorCode;
    private String errorMessage;

    public ErrorDefinition(int errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = ErrorMessageFacade.getMessageTemplate(errorCode);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int compareTo(ErrorDefinition o) {
        return errorCode < o.errorCode ? -1 : 1;
    }
}
