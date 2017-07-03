package tr.com.sinya;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import tr.com.sinya.common.Utils;
import tr.com.sinya.controllers.Controller;
import tr.com.sinya.controllers.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.SmartCardException;

/**
 *
 * @author
 */
public class Eimza extends JFrame {

	private static final Dimension PREFERRED_SIZE = new java.awt.Dimension(31, 31);

	private Controller controller = null;

	private ECertificate cert = null;

	private Random random = new Random();

	private Logger loger = Logger.getLogger(Eimza.class.getName());

	/**
	 * Creates new form PinPanel
	 */
	public Eimza() {
		try {
			File file = new File(Utils.getClassPath()+ "/lisans/lisans.xml");
			if(file.exists())
				System.out.println("Lisans dosyası var");
			cert = SmartCardManager.getInstance().getSignatureCertificate(true, false);
			// jList1.setModel(listModel);
		} catch (SmartCardException ex) {
			Logger.getLogger(Eimza.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ESYAException ex) {
			Logger.getLogger(Eimza.class.getName()).log(Level.SEVERE, null, ex);
		}

		initComponents();
		
		controller = new Controller(this);
	}

	public static void main(String args[]) {
		
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(Eimza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(Eimza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(Eimza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(Eimza.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Eimza eimza = new Eimza();
				eimza.setSize(new Dimension(325, 320));
				eimza.setVisible(true);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		setTitle("Evrak İmzalama Uzmanı");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setLayout(null);

		getContentPane().add(getLblKartGir());

		getContentPane().add(getLblPinGir());

		getContentPane().add(getNumberBoardPanel());

		getContentPane().add(getChRandomizeButton());

		getContentPane().add(getBtnSign());

		getContentPane().add(getCbxCertList());

		getContentPane().add(getBtnRefreshCert());
		
		getContentPane().add(getPswPin());

		getContentPane().add(getChkHidePass());

		getContentPane().add(getBtnSelectFile());

		getContentPane().add(getLblSelectedFilePath());
	}

	private JPanel getNumberBoardPanel() {
		if(numberBoardPanel != null )
			return numberBoardPanel;
		
		numberBoardPanel = new javax.swing.JPanel();
		numberBoardPanel.setLayout(null);

		List<String> numbers = createNumbers();

		btn1 = getNumberButton(0, 0, getNumber(numbers));
		numberBoardPanel.add(btn1);
		btn2 = getNumberButton(40, 0, getNumber(numbers));
		numberBoardPanel.add(btn2);
		btn3 = getNumberButton(80, 0, getNumber(numbers));
		numberBoardPanel.add(btn3);
		btn4 = getNumberButton(0, 30, getNumber(numbers));
		numberBoardPanel.add(btn4);
		btn5 = getNumberButton(40, 30, getNumber(numbers));
		numberBoardPanel.add(btn5);
		btn6 = getNumberButton(80, 30, getNumber(numbers));
		numberBoardPanel.add(btn6);
		btn7 = getNumberButton(0, 60, getNumber(numbers));
		numberBoardPanel.add(btn7);
		btn8 = getNumberButton(40, 60, getNumber(numbers));
		numberBoardPanel.add(btn8);
		btn9 = getNumberButton(80, 60, getNumber(numbers));
		numberBoardPanel.add(btn9);
		btn0 = getNumberButton(0, 90, getNumber(numbers));
		numberBoardPanel.add(btn0);

		numberBoardPanel.add(getBtnDel());
		numberBoardPanel.setBounds(20, 150, 120, 130);
		return numberBoardPanel;
	}

	private JComboBox<String> getCbxCertList() {
		if(cbxCertList != null)
			return cbxCertList;
		cbxCertList = new javax.swing.JComboBox<String>();
		cbxCertList.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "<--Kartınızı Takınız-->" }));
		
		try {
			DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();
			listModel.addElement(cert.getSubject().getCommonNameAttribute());
			cbxCertList.setModel(listModel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cbxCertList.setBounds(20, 60, 220, 30);
		return cbxCertList;
	}

	private JLabel getLblPinGir() {
		if(lblPinGir != null)
			return lblPinGir;
		lblPinGir = new javax.swing.JLabel("Pin Giriniz");
		lblPinGir.setBounds(20, 100, 220, 14);
		return lblPinGir;
	}

	private JLabel getLblKartGir() {
		if(lblKartGir != null)
			return lblKartGir;
		lblKartGir = new javax.swing.JLabel("Kartınızı Seçiniz");
		lblKartGir.setBounds(20, 40, 220, 14);
		return lblKartGir;
	}

	private JPasswordField getPswPin() {
		if(pswPin != null)
			return pswPin;
		pswPin = new javax.swing.JPasswordField();
		pswPin.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				if (key.getKeyCode() == KeyEvent.VK_ENTER)
					btnSignActionPerformed(null);
			}
		});
		pswPin.setBounds(20, 120, 220, 26);
		return pswPin;
	}

	private JButton getBtnSelectFile() {
		if(btnSelectFile != null)
			return btnSelectFile;
		btnSelectFile = new javax.swing.JButton("Dosya Seç");
		btnSelectFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSelectFilebtnSelectFileActionPerformed(evt);
			}
		});
		btnSelectFile.setBounds(20, 10, 91, 23);
		return btnSelectFile;
	}

	private JLabel getLblSelectedFilePath() {
		if(lblSelectedFilePath != null)
			return lblSelectedFilePath;
		lblSelectedFilePath = new javax.swing.JLabel("Seçilmiş dosya yok");
		lblSelectedFilePath.setLabelFor(btnSelectFile);
		lblSelectedFilePath.setBounds(116, 12, 164, 18);
		return lblSelectedFilePath;
	}

	private JButton getBtnSign() {
		if(btnSign != null)
			return btnSign;
		btnSign = new javax.swing.JButton("İmzala");
		btnSign.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSignActionPerformed(evt);
			}
		});
		btnSign.setBounds(140, 150, 100, 120);
		
		return btnSign;
	}

	private JButton getBtnDel() {
		if(btnDel != null)
			return btnDel;
		btnDel = new JButton("Sil");
		btnDel.setFont(new java.awt.Font("Tahoma", 0, 10));
		btnDel.setPreferredSize(PREFERRED_SIZE);
		btnDel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnDelActionPerformed(evt);
			}
		});
		btnDel.setBounds(40, 90, 79, 31);
		
		return btnDel;
	}

	private JCheckBox getChkHidePass() {
		if(chkHidePass != null )
			return chkHidePass;
		chkHidePass = new JCheckBox("Gizle", true);
		chkHidePass.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chkHidePassActionPerformed(evt);
			}
		});
		chkHidePass.setBounds(240, 120, 59, 23);
		
		return chkHidePass;
	}

	private JButton getBtnRefreshCert() {
		if(btnRefreshCert != null)
			return btnRefreshCert;

		ImageIcon icon = new ImageIcon("img/refresh.png");
		Image scaledInstance = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaledInstance);
		
		btnRefreshCert = new JButton(icon);
		btnRefreshCert.setToolTipText("Sertifikayı tekrar yükle");
		
		btnRefreshCert.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRefreshCertActionPerformed(evt);
			}
		});
		btnRefreshCert.setBounds(250, 60, 30, 30);
		return btnRefreshCert;
	}
	
	private JCheckBox getChRandomizeButton() {
		if (chRandomizeButton == null) {
			chRandomizeButton = new JCheckBox("Karıştır", true);
			chRandomizeButton.setBounds(240, 150, 63, 23);
		}
		return chRandomizeButton;
	}

	private List<String> createNumbers() {
		List<String> numbers = new ArrayList<String>();
		for (int i = 0; i < 10; i++)
			numbers.add(String.valueOf(i));
		return numbers;
	}

	private String getNumber(List<String> numbers) {
		int index = random.nextInt(numbers.size());
		String text = numbers.get(index);
		numbers.remove(index);
		return text;
	}

	private JButton getNumberButton(int x, int y, String text) {
		JButton btnNumber = new javax.swing.JButton(text);
		btnNumber.setFont(new java.awt.Font("Tahoma", 0, 10));
		btnNumber.setPreferredSize(PREFERRED_SIZE);
		btnNumber.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				numberButtonActionPerformed(evt);
			}
		});
		btnNumber.setBounds(x, y, 39, 31);
		return btnNumber;
	}

	private void numberButtonActionPerformed(java.awt.event.ActionEvent evt) {
		pswPin.setText(pswPin.getText().trim() + ((JButton) evt.getSource()).getText());
		if(chRandomizeButton.isSelected())
			setRandomButtonText();
	}

	private void setRandomButtonText() {
		List<String> numbers = createNumbers();
		btn0.setText(getNumber(numbers));
		btn1.setText(getNumber(numbers));
		btn2.setText(getNumber(numbers));
		btn3.setText(getNumber(numbers));
		btn4.setText(getNumber(numbers));
		btn5.setText(getNumber(numbers));
		btn6.setText(getNumber(numbers));
		btn7.setText(getNumber(numbers));
		btn8.setText(getNumber(numbers));
		btn9.setText(getNumber(numbers));
	}

	private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {
		String t = pswPin.getText().trim();
		if (t.equals("")) {
			return;
		}
		pswPin.setText(t.substring(0, t.length() - 1));
	}

	private void btnSignActionPerformed(java.awt.event.ActionEvent evt) {
		if (controller.isFileNull()) {
			JOptionPane.showMessageDialog(this, "Dosya seçmelisiniz!", "Uyarı", JOptionPane.DEFAULT_OPTION);
			btnSelectFile.requestFocus();
			return;
		}
		if (pswPin.getPassword() == null || pswPin.getPassword().length < 1) {
			JOptionPane.showMessageDialog(this, "Pin giriniz!", "Uyarı", JOptionPane.DEFAULT_OPTION);
			pswPin.requestFocus();
			return;
		}
		controller.sign(cert, String.valueOf(pswPin.getPassword()));
	}

	private void chkHidePassActionPerformed(java.awt.event.ActionEvent evt) {
		if (chkHidePass.isSelected()) {
			pswPin.setEchoChar('*');
		} else {
			pswPin.setEchoChar((char) 0);
		}
	}

	private void btnRefreshCertActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			SmartCardManager.reset();
			cert = SmartCardManager.getInstance().getSignatureCertificate(true, false);
			DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<String>();
			listModel.addElement(cert.getSubject().getCommonNameAttribute());
			cbxCertList.setModel(listModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void btnSelectFilebtnSelectFileActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser fc = new JFileChooser("D:\\");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			controller.setFile(file);
			getLblSelectedFilePath().setText(file.getPath());
			loger .log(Level.SEVERE, "Opening: " + file.getName());
		} else {
			loger.log(Level.SEVERE, "Open command cancelled by user.");
		}
		
	}

	private javax.swing.JButton btnDel;
	private javax.swing.JButton btnRefreshCert;
	private javax.swing.JButton btnSelectFile;
	private javax.swing.JButton btnSign;
	private javax.swing.JButton btn1;
	private javax.swing.JButton btn2;
	private javax.swing.JButton btn3;
	private javax.swing.JButton btn4;
	private javax.swing.JButton btn5;
	private javax.swing.JButton btn6;
	private javax.swing.JButton btn7;
	private javax.swing.JButton btn8;
	private javax.swing.JButton btn9;
	private javax.swing.JButton btn0;
	private javax.swing.JComboBox<String> cbxCertList;
	private javax.swing.JCheckBox chkHidePass;
	private javax.swing.JPanel numberBoardPanel;
	private javax.swing.JLabel lblKartGir;
	private javax.swing.JLabel lblPinGir;
	private javax.swing.JLabel lblSelectedFilePath;
	private javax.swing.JPasswordField pswPin;
	private JCheckBox chRandomizeButton;
	
}
