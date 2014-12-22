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
 * @DATE 2014��12��18��
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
			assertEquals(m.getRelativeLocation("����", "����"), "����");
			assertEquals(m.getRelativeLocation("����", "����"), "ʡ��");
			assertEquals(m.getRelativeLocation("����", "��ɳ"), "����");
//			assertEquals(m.getRelativeLocation("����", "����"), "����");
			assertEquals(m.getRelativeLocation("����", "����"), "����");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
