package tr.com.sinya.common;

import java.net.URL;

public class Utils {
	
	public static String getClassPath() {
		URL root;
		String classPath = "";
		try {
			root = Utils.class.getClassLoader().getResource("");
			System.out.println("getClassLoader().getResource(\"./\") -->"+ root);
			if(root == null) {
				root = Utils.class.getResource("/");
				System.out.println("getResource(\"./\") --> "+ root);
			}
			
			if(root == null) {
			}else{
				classPath = root.getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			
		}finally {
			return classPath;
		}
	}

	public static String nvl(String value, String ifNullRetVal) {
		if(value == null)
			return ifNullRetVal;
		return value;
	}

}
