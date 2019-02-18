package com.sync.data;

import org.junit.Test;

import com.sync.data.Account;
import com.sync.data.AnalysisCode;
import com.sync.data.Audited;
import nl.jqno.equalsverifier.EqualsVerifier;

public class DataTests {

    @Test
    public void dataTests() {

        EqualsVerifier.forClass(Audited.class).verify();
        EqualsVerifier.forClass(Account.class).verify();
        EqualsVerifier.forClass(AnalysisCode.class).verify();

    }
}
