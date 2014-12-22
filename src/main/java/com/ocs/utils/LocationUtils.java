package com.ocs.utils;

import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



//下面主要是org.xml.sax包的类
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocationUtils {
	private static LocationUtils instance = null;
	private Map<String, String> citiesMap;

	private LocationUtils() throws Exception {
		// 加载城市数据
		load_data();
	}

	/**
	 * 获取CityManager的实例
	 * 
	 * @return
	 * @throws Exception
	 */
	public static synchronized LocationUtils getInstance() throws Exception {
		if (instance == null) {
			instance = new LocationUtils();
		}
		return instance;
	}

	/**
	 * 加载城市数据
	 * 
	 * @throws Exception
	 */
	private void load_data() throws Exception {
		// 城市数据文件路径
//		final String DATA_FILE_PATH = "C://China.xml";
		final String DATA_FILE_PATH = System.getProperty("user.dir")+PropertiesUtils.getLocationFilePath();
		
		citiesMap = new HashMap<String, String>();
	
		// （1）得到DOM解析器的工厂实例
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		// 得到javax.xml.parsers.DocumentBuilderFactory;类的实例就是我们要的解析器工厂

		// （2）从DOM工厂获得DOM解析器
		DocumentBuilder dombuilder = domfac.newDocumentBuilder();
		// 通过javax.xml.parsers.DocumentBuilderFactory实例的静态方法newDocumentBuilder()得到DOM解析器
		// （3）把要解析的XML文档转化为输入流，以便DOM解析器解析它
		InputStream is = new FileInputStream(DATA_FILE_PATH);
		// （4）解析XML文档的输入流，得到一个Document
		Document doc = dombuilder.parse(is);
		// 由XML文档的输入流得到一个org.w3c.dom.Document对象，以后的处理都是对Document对象进行的
		// （5）得到XML文档的根节点
		Element root = doc.getDocumentElement();
		// 在DOM中只有根节点是一个org.w3c.dom.Element对象。
		// （6）得到节点的第一个子节点（中国城市数据）
		NodeList contries = root.getChildNodes();
		Node china = contries.item(1);

		// 读取数据
		if (china != null) {
			NodeList provinces = china.getChildNodes();
			for(int i=0;i<provinces.getLength();i++){
                Node prov=provinces.item(i);
                if(prov.getNodeType()==Node.ELEMENT_NODE){
                	String pName = prov.getNodeName().trim();
                	NodeList cities = prov.getChildNodes();
                	for(int j = 0; j < cities.getLength(); j++){
                		Node city = cities.item(j);
                		if (city.getNodeType() == Node.ELEMENT_NODE){
                			this.citiesMap.put(city.getTextContent().trim(), pName);
                		}
                	}
                }
			}
		}
	}
	
	/**
	 * 判断AB两个城市的关系
	 * 
	 * @param cityA: 城市A
	 * @param cityB： 城市B
	 * @return "本地", "省内", "国内", "国际"
	 */
	public String getRelativeLocation(String cityA, String cityB){
		final String[] result = {"本地", "省内", "国内", "国际"};
		int index = 0;
		
		if (!cityA.equals(cityB)){
			// A B不是同一个城市
			String provA = this.citiesMap.get(cityA);
			String provB = this.citiesMap.get(cityB);
			
			if (provA != null && provB != null){
				if (provA.equals(provB)){
					// 省份相同
					index = 1;
				}else{
					// 省份不同
					index = 2;
				}
			}else{
				index = 3;
			}
			
		}
		return result[index];
		
	}
	
	/**
	 * 判断两个相对位置是否包含
	 * 例如：area1-国内
	 *     area2-本地
	 *     返回true
	 * @param area1
	 * @param area2
	 * @return true,false
	 */
	public static boolean isContains(String area1,String area2){
		String areas[] = {"本地", "省内", "国内", "国际"};
		int areaCode[] = {1001,2001,3001,4001};
		
		int code1=0,code2=0;
		for(int i = 0;i<4;i++){
			if(areas[i].equals(area1))
				code1 = areaCode[i];
		}
		for(int i = 0;i<4;i++){
			if(areas[i].equals(area2))
				code2 = areaCode[i];
		}
		if(code1*code2>0){
			if(code1>=code2)
				return true;
		}
		
		return false;
	}
}
