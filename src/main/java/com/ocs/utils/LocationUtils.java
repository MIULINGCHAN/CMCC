package com.ocs.utils;

import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



//������Ҫ��org.xml.sax������
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocationUtils {
	private static LocationUtils instance = null;
	private Map<String, String> citiesMap;

	private LocationUtils() throws Exception {
		// ���س�������
		load_data();
	}

	/**
	 * ��ȡCityManager��ʵ��
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
	 * ���س�������
	 * 
	 * @throws Exception
	 */
	private void load_data() throws Exception {
		// ���������ļ�·��
//		final String DATA_FILE_PATH = "C://China.xml";
		final String DATA_FILE_PATH = System.getProperty("user.dir")+PropertiesUtils.getLocationFilePath();
		
		citiesMap = new HashMap<String, String>();
	
		// ��1���õ�DOM�������Ĺ���ʵ��
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		// �õ�javax.xml.parsers.DocumentBuilderFactory;���ʵ����������Ҫ�Ľ���������

		// ��2����DOM�������DOM������
		DocumentBuilder dombuilder = domfac.newDocumentBuilder();
		// ͨ��javax.xml.parsers.DocumentBuilderFactoryʵ���ľ�̬����newDocumentBuilder()�õ�DOM������
		// ��3����Ҫ������XML�ĵ�ת��Ϊ���������Ա�DOM������������
		InputStream is = new FileInputStream(DATA_FILE_PATH);
		// ��4������XML�ĵ������������õ�һ��Document
		Document doc = dombuilder.parse(is);
		// ��XML�ĵ����������õ�һ��org.w3c.dom.Document�����Ժ�Ĵ����Ƕ�Document������е�
		// ��5���õ�XML�ĵ��ĸ��ڵ�
		Element root = doc.getDocumentElement();
		// ��DOM��ֻ�и��ڵ���һ��org.w3c.dom.Element����
		// ��6���õ��ڵ�ĵ�һ���ӽڵ㣨�й��������ݣ�
		NodeList contries = root.getChildNodes();
		Node china = contries.item(1);

		// ��ȡ����
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
	 * �ж�AB�������еĹ�ϵ
	 * 
	 * @param cityA: ����A
	 * @param cityB�� ����B
	 * @return "����", "ʡ��", "����", "����"
	 */
	public String getRelativeLocation(String cityA, String cityB){
		final String[] result = {"����", "ʡ��", "����", "����"};
		int index = 0;
		
		if (!cityA.equals(cityB)){
			// A B����ͬһ������
			String provA = this.citiesMap.get(cityA);
			String provB = this.citiesMap.get(cityB);
			
			if (provA != null && provB != null){
				if (provA.equals(provB)){
					// ʡ����ͬ
					index = 1;
				}else{
					// ʡ�ݲ�ͬ
					index = 2;
				}
			}else{
				index = 3;
			}
			
		}
		return result[index];
		
	}
	
	/**
	 * �ж��������λ���Ƿ����
	 * ���磺area1-����
	 *     area2-����
	 *     ����true
	 * @param area1
	 * @param area2
	 * @return true,false
	 */
	public static boolean isContains(String area1,String area2){
		String areas[] = {"����", "ʡ��", "����", "����"};
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
