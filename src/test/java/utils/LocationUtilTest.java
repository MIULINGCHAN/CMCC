/**
 * 
 */
package utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ocs.utils.LocationUtils;

/**
 * @author MiuChan
 * @DATE 2014年12月18日
 */
/**
 * @author miumiu
 *
 */
public class LocationUtilTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link com.ocs.utils.LocationUtils#isContains(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testIsContains() {
		LocationUtils m;
		try {
			m = LocationUtils.getInstance();
			assertEquals(m.getRelativeLocation("广州", "广州"), "本地");
			assertEquals(m.getRelativeLocation("广州", "深圳"), "省内");
			assertEquals(m.getRelativeLocation("广州", "长沙"), "国内");
//			assertEquals(m.getRelativeLocation("广州", "北京"), "国内");
			assertEquals(m.getRelativeLocation("广州", "美国"), "国际");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
