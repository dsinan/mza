/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.com.sinya.controllers;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import tr.com.sinya.common.Utils;
import tr.com.sinya.signers.PadesSigner;
import tr.com.sinya.signers.XadesSigner;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.util.LicenseUtil;

/**
 *
 * @author
 */
public class Controller {

	Logger loger = Logger.getLogger(Controller.class.getName());
	private final Component parent;
	private File file;
	private String ROOT_DIR;
	// TODO İptal edilecek
	private boolean IS_QUALIFIED = true;
	private String resources = "";
	// private String resources = "/src/main/resources";

	public Controller(Component parent) {
		this.parent = parent;
		String classPath = Utils.getClassPath();
		
		System.out.println("classPath........." + classPath);
		
		File binDir = new File(classPath);
		
		System.out.println("........." + binDir);
		
		ROOT_DIR = Utils.nvl(binDir.getParent(),"");
		
		try {
			LicenseUtil.setLicenseXml(new FileInputStream(ROOT_DIR + resources + "/lisans/lisans.xml"));
		} catch (Exception ex) {
			loger.log(Level.SEVERE, null, ex);
		}
	}

	private String getParameter(String string) {

		return "";
	}


	public boolean isFileNull() {
		return file == null;
	}

	public void sign(ECertificate cert, String pin) {
		String ext = "";
		try {
			ext = Files.probeContentType(file.toPath());
			loger.severe("--->"+ext);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if("application/pdf".equals(ext.toLowerCase())){
			PadesSigner p = new PadesSigner(file);
			p.sign(cert, pin);
		}else if("text/xml".equals(ext.toLowerCase())){
			XadesSigner x = new XadesSigner(file);
			x.sign(cert, pin);
		}else {
			XadesSigner x = new XadesSigner(file);
			x.sign(cert, pin);
		}
	}

	public void setFile(File selectedFile) {
		this.file = selectedFile;
	}
}
