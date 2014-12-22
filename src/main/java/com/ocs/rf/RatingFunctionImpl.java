package com.ocs.rf;

import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import com.ocs.bean.account.Account;
import com.ocs.bean.account.PackageInfo;
import com.ocs.bean.account.RuleUsage;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;
import com.ocs.utils.PropertiesUtils;

public class RatingFunctionImpl implements RatingFunction {

	@Override
	public RatingResult dataTrafficRating(Account account,
			DataTrafficEvent event) {
		// TODO Auto-generated method stub
        try {
            // load up the knowledge base
//	        KieServices ks = KieServices.Factory.get();
//    	    KieContainer kContainer = ks.getKieClasspathContainer();
//        	KieSession kSession = kContainer.newKieSession("ksession-rules");
        	

        	KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(); 
//        	kbuilder.add(ResourceFactory.newClassPathResource("DG3G19.drl", DroolsTest.class),ResourceType.DRL);
        	String drlResourcePath = PropertiesUtils.getDrlFilePath()+ "/" + PropertiesUtils.getDrlFileName();  
        	kbuilder.add(ResourceFactory.newFileResource( System.getProperty("user.dir")+drlResourcePath),ResourceType.DRL);
        	KnowledgeBase kbase = kbuilder.newKnowledgeBase(); 
        	kbase.addKnowledgePackages(kbuilder.getKnowledgePackages()); 
        	StatefulKnowledgeSession kSession = kbase.newStatefulKnowledgeSession();
        	
        	kSession.insert(event);
        	PackageInfo mainSer = account.getMainService();
        	for(RuleUsage ru : mainSer.usages){
        		kSession.insert(ru);
        	}
        	
        	RatingResult rr = new RatingResult();
        	kSession.setGlobal("result", rr);
      
        	System.out.println("Start to match rules >>>>>>> ");
        	
        	kSession.fireAllRules();
        	
        	rr = (RatingResult) kSession.getGlobal("result");
        	kSession.dispose();
        	
        	rr.quantity = event.getProduceQuantity();
        	
        	if(rr.ruleID==null||rr.ruleID.equals("")){
        		//查找不到匹配的规则
        		System.out.println("Cannot find the matching rule  >>>>>>>");
        		return null;
        	}
        	else{
        		System.out.println("finish matching rules >>>>>>> ");
        		return rr;
        	}
        } catch (Throwable t) {
 
            t.printStackTrace();
        }
		return null;
	}
}
