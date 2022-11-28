package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.tokenrenewal;

import java.text.ParseException;

public interface DapsTokenRenewalChecker {

    boolean needsRenewal(String dapsToken) throws ParseException;
}
