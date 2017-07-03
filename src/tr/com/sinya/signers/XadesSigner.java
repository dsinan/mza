package tr.com.sinya.signers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import javax.swing.JOptionPane;

import tr.com.sinya.common.Utils;
import tr.com.sinya.controllers.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.LoginException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.SignatureMethod;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignatureException;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.config.Config;

public class XadesSigner implements ISigner{

	private static final String SIGNATURE_FILENAME = "Signed";
	private static final String CONFIG = "/config/xmlsignature-config.xml";
	private File file;
	private boolean IS_QUALIFIED = false;
	private String fileContext = "text/xml";

	public XadesSigner(File file) {
		this.file = file;
	}
	
	public XadesSigner(File file, String fileContext) {
		this.file = file;
		this.fileContext = fileContext;
	}

	public void sign(ECertificate eCertificate, String pin) {
		try {
            // create context with working directory
            Context context = createContext();

            // create signature according to context,
            // with default type (XADES_BES)
            XMLSignature signature = new XMLSignature(context);

            // add document as reference, but do not embed it
            // into the signature (embed=false)
            signature.addObject(Files.readAllBytes(file.toPath()), fileContext, "UTF8");

            signature.getSignedInfo().setSignatureMethod(SignatureMethod.RSA_SHA256);

            // false-true gets non-qualified certificates while true-false gets qualified ones
            ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(IS_QUALIFIED , !IS_QUALIFIED);

            // add certificate to show who signed the document
            signature.addKeyInfo(cert);

            // now sign it by using smart card
            signature.sign(SmartCardManager.getInstance().getSigner(pin, cert));

            String ext = "."+ fileContext.split("/")[1];
			signature.write(new FileOutputStream(file.getParent() + SIGNATURE_FILENAME + file.getName() + ext));
			
			JOptionPane.showMessageDialog(null, "İmzalama işlemi tamamlandı.", "Bilgi", JOptionPane.DEFAULT_OPTION);
        }
        catch (XMLSignatureException x){
            // cannot create signature
            x.printStackTrace();
        }
        catch (IOException x){
            // probably couldn't write to the file
            x.printStackTrace();
        } catch (SmartCardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ESYAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Context createContext() {

		Context context = null;
		try {
			String classPath = Utils.getClassPath();
			System.out.println("classPath........." + classPath);
			File binDir = new File(classPath);
			System.out.println("........." + binDir);
			String ROOT_DIR = Utils.nvl(binDir.getParent(),"");
			
			context = new Context(file.getParent());
			context.setConfig(new Config(ROOT_DIR + CONFIG));
		} catch (XMLSignatureException e) {
			e.printStackTrace();
		}
		return context;
	}
}
