package tr.com.sinya.signers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import javax.swing.JOptionPane;

import tr.com.sinya.common.Utils;
import tr.com.sinya.controllers.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.pades.PAdESContext;
import tr.gov.tubitak.uekae.esya.api.signature.ContainerValidationResult;
import tr.gov.tubitak.uekae.esya.api.signature.Signature;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureContainer;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureFactory;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureFormat;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureValidationResult;
import tr.gov.tubitak.uekae.esya.api.signature.config.Config;

public class PadesSigner implements ISigner {

	private File file;
	
	public PadesSigner(File file) {
		this.file = file;
	}

	public void sign(ECertificate eCertificate, String pin) {
		try {
			// Webden applete parametre olarak gönderilenverinin alınması
			// String data = getParameter("data");
			// byte[] decodedData = Base64.getDecoder().decode(data);
			FileInputStream fis = new FileInputStream(file);
			// InputStream is = new ByteArrayInputStream(fis);

			SignatureContainer pc = SignatureFactory.readContainer(SignatureFormat.PAdES, fis, createContext());

			// ECertificate eCertificate =
			// SmartCardManager.getInstance().getSignatureCertificate(IS_QUALIFIED,
			// !IS_QUALIFIED);
			BaseSigner signer = SmartCardManager.getInstance().getSigner(pin, eCertificate);

			// add signature
			Signature signature = pc.createSignature(eCertificate);
			signature.setSigningTime(Calendar.getInstance());
			signature.sign(signer);

			// TODO Cashe te tutulacak şekilde düzenlenmeli
			// Webtarafından request edildiğinde verilecek bir yapı
			// oluşturulmalı
			pc.write(new FileOutputStream(file.getParent() + "/signed-bes.pdf"));

			// read and validate
			SignatureContainer pc2 = SignatureFactory.readContainer(SignatureFormat.PAdES,
					new FileInputStream(file.getParent() + "signed-bes.pdf"), createContext());
			ContainerValidationResult svr = pc2.verifyAll();
			Map<Signature, SignatureValidationResult> signatureValidationResults = svr.getSignatureValidationResults();

			for (Signature signature1 : signatureValidationResults.keySet()) {
				System.out.println(signature1.toString());
			}

			JOptionPane.showMessageDialog(null, "İmzalama işlemi tamamlandı.", "Bilgi", JOptionPane.DEFAULT_OPTION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates context for signature creation and validation
	 *
	 * @return created context
	 */
	public PAdESContext createContext() throws Exception {
		
		String classPath = Utils.getClassPath();
		System.out.println("classPath.........:" + classPath);
		File binDir = new File(classPath);
		System.out.println("binDir.........:" + binDir);
		String ROOT_DIR = Utils.nvl(binDir.getParent(),"");
		PAdESContext c = new PAdESContext(new File("").toURI());
		c.setConfig(new Config(ROOT_DIR + "/config/esya-signature-config.xml"));
		return c;
	}

}
