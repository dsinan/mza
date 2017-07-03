package tr.com.sinya;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import tr.com.sinya.controllers.SmartCardManager;

public class Test {

	public static void main(String[] args) {
		 NativeLibrary cryptUI = NativeLibrary.getInstance("Cryptui");
		    NativeLibrary crypt32 = NativeLibrary.getInstance("Crypt32");

		    Function functionCertOpenSystemStore = crypt32.getFunction("CertOpenSystemStoreA");
		    Object[] argsCertOpenSystemStore = new Object[] { 0, "CA"};
		    SmartCardManager h = (SmartCardManager) functionCertOpenSystemStore.invoke(SmartCardManager.class, argsCertOpenSystemStore);

		    Function functionCryptUIDlgSelectCertificateFromStore = cryptUI.getFunction("CryptUIDlgSelectCertificateFromStore");
		    System.out.println(functionCryptUIDlgSelectCertificateFromStore.getName());
		    Object[] argsCryptUIDlgSelectCertificateFromStore = new Object[] { h, 0, 0, 0, 16, 0, 0};
		    Pointer ptrCertContext = (Pointer) functionCryptUIDlgSelectCertificateFromStore.invoke(Pointer.class, argsCryptUIDlgSelectCertificateFromStore);

		    Function functionCertGetNameString = crypt32.getFunction("CertGetNameStringW");
		    char[] ptrName = new char[128];
		    Object[] argsCertGetNameString = new Object[] { ptrCertContext, 5, 0, 0, ptrName, 128};
		    functionCertGetNameString.invoke(argsCertGetNameString);
		    System.out.println("Selected certificate is " + new String(ptrName));

		    Function functionCertFreeCertificateContext = crypt32.getFunction("CertFreeCertificateContext");
		    Object[] argsCertFreeCertificateContext = new Object[] { ptrCertContext};
		    functionCertFreeCertificateContext.invoke(argsCertFreeCertificateContext);

		    Function functionCertCloseStore = crypt32.getFunction("CertCloseStore");
		    Object[] argsCertCloseStore = new Object[] { h, 0};
		    functionCertCloseStore.invoke(argsCertCloseStore);
	}
}
