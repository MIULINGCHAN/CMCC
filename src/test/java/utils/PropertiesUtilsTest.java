/**
 * 
 */
package utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ocs.utils.PropertiesUtils;

/**
 * @author MiuChan
 * @DATE 2014Äê12ÔÂ18ÈÕ
 */
/**
 * @author miumiu
 *
 */
public class PropertiesUtilsTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getABMServerIP()}.
	 */
	@Test
	public void testGetABMServer() {
		System.out.println("ABM Server IP" + " [ " + PropertiesUtils.getABMServerIP() +":"+ PropertiesUtils.getABMServerPort() + " ]");
		assertNotEquals(null, PropertiesUtils.getABMServerIP());
		assertNotEquals(null, PropertiesUtils.getABMServerPort());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getDBIP()}.
	 */
	@Test
	public void testGetDBIP() {
		System.out.println("DataBase IP" + " £º " + PropertiesUtils.getDBIP());
		assertNotEquals(null, PropertiesUtils.getDBIP());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getDBUsername()}.
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getDBPassword()}.
	 */
	@Test
	public void testGetDB() {
		System.out.println("Database[ username : " + PropertiesUtils.getDBUsername() + ", " 
				+ "password : " + PropertiesUtils.getDBPassword() + " ]");
		assertNotEquals(null, PropertiesUtils.getDBUsername());
		assertNotEquals(null, PropertiesUtils.getDBPassword());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getOCSServerIP()}.
	 */
	@Test
	public void testGetOCSServerIP() {
		System.out.println("OCS Server IP" + " : " + PropertiesUtils.getOCSServerIP());
		assertNotEquals(null, PropertiesUtils.getOCSServerIP());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getOCSServerPort()}.
	 */
	@Test
	public void testGetOCSServerPort() {
		System.out.println("OCS Server Port" + " : " + PropertiesUtils.getOCSServerPort());
		assertNotEquals(null, PropertiesUtils.getOCSServerPort());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getLocationFilePath()}.
	 */
	@Test
	public void testGetLocationFilePath() {
		System.out.println("Location FilePath" + " : " + PropertiesUtils.getLocationFilePath());
		assertNotEquals(null, PropertiesUtils.getLocationFilePath());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getGGSNServerIP()}.
	 */
	@Test
	public void testGetGGSNServerIP() {
		System.out.println("GGSN Server IP" + " : " + PropertiesUtils.getGGSNServerIP());
		assertNotEquals(null, PropertiesUtils.getGGSNServerIP());
	}

	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getTairConfigServerIP}.
	 */
	@Test
	public void testGetTairConfigServerIP() {
		System.out.println("Tair Config Server IP" + " : " + PropertiesUtils.getTairConfigServerIP());
		assertNotEquals(null, PropertiesUtils.getTairConfigServerIP());
	}
	
	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getTairConfigServerPort}.
	 */
	@Test
	public void testGetTairConfigServerPort() {
		System.out.println("Tair Config Server port" + " : " + PropertiesUtils.getTairConfigServerPort());
		assertNotEquals(null, PropertiesUtils.getTairConfigServerPort());
	}
	
	/**
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getCFThreadPoolMaxNum}.
	 * Test method for {@link com.ocs.utils.PropertiesUtils#getRFThreadPoolMaxNum}.
	 */
	@Test
	public void testGetThreadPoolMaxNum() {
		System.out.println("CF ThreadPool MAX" + " : " + PropertiesUtils.getCFThreadPoolMaxNum());
		System.out.println("RF ThreadPool MAX" + " : " + PropertiesUtils.getRFThreadPoolMaxNum());
		assertNotEquals(null, PropertiesUtils.getCFThreadPoolMaxNum());
		assertNotEquals(null, PropertiesUtils.getRFThreadPoolMaxNum());
	}
	
	public void testGetDRLFile(){
		System.out.println("DRL file : " + PropertiesUtils.getDrlFilePath() + "/" + PropertiesUtils.getDrlFileName());
		assertNotEquals(null, PropertiesUtils.getDrlFilePath());
		assertNotEquals(null, PropertiesUtils.getDrlFileName());
	}
	
}
