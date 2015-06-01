package com.redhat.summit2015.ceplab;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.drools.core.time.SessionPseudoClock;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;

import com.redhat.summit2015.ceplab.model.Account;
import com.redhat.summit2015.ceplab.model.AccountStatus;
import com.redhat.summit2015.ceplab.model.Transaction;

/**
 * Unit test for simple App.
 * @see org.kie.api.KieServices, org.kie.api.runtime.rule.EntryPoint
 */
public class Lab3Test extends TestCase {

	KieSession kSession;
	
	/**
	 * An EntryPoint is
	 * We will only use one EntryPoint in this example.
	 */
	EntryPoint entryPoint;
	
    SessionPseudoClock clock;


	@Before
	public void setUp() {
		// load up the knowledge base
		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		kSession = kContainer.newKieSession("ksession1");
		assertNotNull(
				"kSession should be instantiated and set to member variable",
				kSession);
        entryPoint = kSession.getEntryPoint("Withdrawls");
        clock = kSession.getSessionClock();
	}

	@Test
	public void testSomething() throws InstantiationException, IllegalAccessException {
		assertNotNull("kSession should be instantiated", kSession);
        assertNotNull("EntryPoint should not be null", entryPoint);
		
		Account account = new Account(AccountStatus.ACTIVE, BigDecimal.valueOf(10000));
		Transaction withdrawl1 = new Transaction(account, BigDecimal.valueOf(400));
		Transaction withdrawl2 = new Transaction(account, BigDecimal.valueOf(400));
		Transaction withdrawl3 = new Transaction(account, BigDecimal.valueOf(150));

		FactType accountInfoFactType = kSession.getKieBase().getFactType("com.redhat.summit2015.ceplab", "AccountInfo");
		Object accountInfo = accountInfoFactType.newInstance();
		accountInfoFactType.set(accountInfo, "averageBalance",
				BigDecimal.valueOf(1000));
		accountInfoFactType.set(accountInfo, "id", account.getId());
		FactHandle accountInfoHandle = kSession.insert(accountInfoFactType);
		kSession.update(accountInfoHandle, accountInfo);		

		kSession.insert(account);
		entryPoint.insert(withdrawl1);
		entryPoint.insert(withdrawl2);
		entryPoint.insert(withdrawl3);
		

		kSession.fireAllRules();

		assertSame("Account should not be blocked", account.getStatus(), AccountStatus.BLOCKED);
		
	}
	
	@Test
	public void testPassingWithdrawls() throws InstantiationException, IllegalAccessException{
		assertNotNull("kSession should be instantiated", kSession);
        assertNotNull("EntryPoint should not be null", entryPoint);
		
		Account account = new Account(AccountStatus.ACTIVE, BigDecimal.valueOf(10000));
		Transaction withdrawl1 = new Transaction(account, BigDecimal.valueOf(100));
		Transaction withdrawl2 = new Transaction(account, BigDecimal.valueOf(100));
		Transaction withdrawl3 = new Transaction(account, BigDecimal.valueOf(100));

		kSession.insert(account);
		entryPoint.insert(withdrawl1);
		
		System.out.println(clock.getCurrentTime());
		clock.advanceTime(20, TimeUnit.MINUTES);
		System.out.println(clock.getCurrentTime());
		
		entryPoint.insert(withdrawl2);
		entryPoint.insert(withdrawl3);
		
		kSession.fireAllRules();
		
		assertSame("Account should still be Active", account.getStatus(), AccountStatus.ACTIVE);
	}

}