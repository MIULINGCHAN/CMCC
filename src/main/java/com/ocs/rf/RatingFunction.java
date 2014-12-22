package com.ocs.rf;

import com.ocs.bean.account.Account;
import com.ocs.bean.event.DataTrafficEvent;
import com.ocs.bean.event.RatingResult;

public interface RatingFunction {
	public RatingResult dataTrafficRating(Account account, DataTrafficEvent event);
}
