package tr.com.sinya.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.common.crypto.Algorithms;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.common.util.StringUtil;
import tr.gov.tubitak.uekae.esya.api.common.util.bag.Pair;
import tr.gov.tubitak.uekae.esya.api.smartcard.apdu.APDUSmartCard;
import tr.gov.tubitak.uekae.esya.api.smartcard.pkcs11.*;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * SmartCardManager handles user smart card operations. Uses APDU access to card as default.
 * Works with Java 6
 * @author orcun.ertugrul
 *
 */
public class SmartCardManager 
{
	private static Logger LOGGER = LoggerFactory.getLogger(SmartCardManager.class);
	
	private static Object lockObject = new Object();

	private static SmartCardManager mSCManager;

	private int mSlotCount = 0;

	private String mSerialNumber;

	private ECertificate mSignatureCert;

	private ECertificate mEncryptionCert;
	
	protected BaseSmartCard bsc;
	
	protected BaseSigner mSigner;
	
	private static boolean useAPDU = true;
	
	public static void useAPDU(boolean aUseAPDU)
	{
		useAPDU = aUseAPDU;
	}

	/**
	 * Singleton is used for this class. If many card placed, it wants to user to select one of cards.
	 * If there is a influential change in the smart card environment, it  repeat the selection process.
	 * The influential change can be: 
	 * 		If there is a new smart card connected to system.
	 * 		The cached card is removed from system.
	 * These situations are checked in getInstance() function. So for your non-squential SmartCard Operation,
	 * call getInstance() function to check any change in the system.
	 *
	 * In order to reset thse selections, call reset function.
	 * 
	 * @return SmartCardManager instance
	 * @throws SmartCardException
	 */
	public static SmartCardManager getInstance() throws SmartCardException
	{
		synchronized (lockObject) 
		{
			if(mSCManager == null)
			{
				mSCManager = new SmartCardManager();
				return mSCManager;
			}
			else
			{
				//Check is there any change
				try 
				{
					//If there is a new card in the system, user will select a smartcard. 
					//Create new SmartCard.
					if(mSCManager.getSlotCount() < SmartOp.getCardTerminals().length) 
					{
						LOGGER.debug("New card pluged in to system");
						mSCManager = null;
						return getInstance();
					}

					//If used card is removed, select new card.
					String availableSerial = null;
					try
					{
						availableSerial =  StringUtil.toString(mSCManager.getBasicSmartCard().getSerial());
					}
					catch(SmartCardException ex)
					{
						LOGGER.debug("Card removed");
						mSCManager = null;
						return getInstance();
					}
					if(!mSCManager.getSelectedSerialNumber().equals(availableSerial))
					{
						LOGGER.debug("Serial number changed. New card is placed to system");
						mSCManager = null;
						return getInstance();
					}

					return mSCManager;
				} 
				catch (SmartCardException e) 
				{
					mSCManager = null;
					throw e;
				}
			}
		}
	}


	/**
	 *
	 * @throws SmartCardException
	 */
	public SmartCardManager() throws SmartCardException
	{
		try 
		{
			LOGGER.debug("New SmartCardManager will be created");
			String [] terminals = SmartOp.getCardTerminals();
			String terminal;
			
			if(terminals == null || terminals.length == 0)
				throw new SmartCardException("No terminal found");
			
			LOGGER.debug("Terminal count : " + terminals.length );
			
			int index = 0;
			if(terminals.length == 1)
				terminal = terminals[index];
			else
			{
				index = askOption(null, null, terminals,"Terminal List",new String[]{"OK"});
				terminal = terminals[index];
			}
			
			boolean apduSupport = false;
			
			try
			{
				apduSupport = APDUSmartCard.isSupported(terminal);
			}
			catch(Exception ex)
			{
				LOGGER.error("APDU Smartcard cannot be created. Probably AkisCIF.jar is missing");
				apduSupport = false;
			}
			
			if(useAPDU == true && apduSupport)
			{
				LOGGER.debug("APDU Smartcard will be created");
				APDUSmartCard asc = new APDUSmartCard();
				CardTerminal ct = TerminalFactory.getDefault().terminals().getTerminal(terminal);
				asc.openSession(ct);
				bsc = asc;
			}
			else
			{
				LOGGER.debug("PKCS11 Smartcard will be created");
				Pair<Long, CardType> slotAndCardType = SmartOp.getSlotAndCardType(terminal);
				bsc = new P11SmartCard(slotAndCardType.getObject2());
				bsc.openSession(slotAndCardType.getObject1());
			}
				
			mSerialNumber = StringUtil.toString(bsc.getSerial());
			mSlotCount = terminals.length; 
		}
		catch (SmartCardException e) 
		{
			LOGGER.error(e.getMessage());
			throw e;
		}
		catch (PKCS11Exception e) 
		{
			LOGGER.error(e.getMessage());
			throw new SmartCardException("Pkcs11 exception", e);
		} 
		catch (IOException e) 
		{
			LOGGER.error(e.getMessage());
			throw new SmartCardException("Smart Card IO exception", e);
		}
	}

	/**
	 * BaseSigner interface for the requested certificate. Do not forget to logout after your crypto 
	 * operation finished
	 * @param aCardPIN
	 * @param aCert
	 * @return
	 * @throws SmartCardException
	 * @throws LoginException 
	 */
	public synchronized BaseSigner getSigner(String aCardPIN, ECertificate aCert)throws SmartCardException, LoginException
	{
		if(mSigner == null)
		{
			bsc.login(aCardPIN);
			mSigner = bsc.getSigner(aCert.asX509Certificate(), Algorithms.SIGNATURE_RSA_SHA256);
		}
		return mSigner;
	}
	
	
	
	/**
	 * BaseSigner interface for the requested certificate. Do not forget to logout after your crypto 
	 * operation finished
	 * @param aCardPIN
	 * @param aCert
	 * @return
	 * @throws SmartCardException
	 * @throws LoginException 
	 */
	public synchronized BaseSigner getSigner(String aCardPIN, ECertificate aCert, String aSigningAlg,AlgorithmParameterSpec aParams)throws SmartCardException, LoginException
	{
		if(mSigner == null)
		{
			bsc.login(aCardPIN);
			mSigner = bsc.getSigner(aCert.asX509Certificate(), aSigningAlg, aParams);
		}
		return mSigner;
	}
	
	
	/**
	 * Logouts from smart card. 
	 * @throws SmartCardException
	 */
	public synchronized void logout()throws SmartCardException
	{
		mSigner = null;
		bsc.logout();
	}
	

	/**
	 * Returns for the signature certificate. If there are more than one certificates in the card in requested
	 * attributes, it wants user to select the certificate. It caches the selected certificate, to reset cache,
	 * call reset function.
	 * 
	 * @param checkIsQualified Only selects the qualified certificates if it is true.
	 * @param checkBeingNonQualified Only selects the non-qualified certificates if it is true. 
	 * if the two parameters are false, it selects all certificates.
	 * if the two parameters are true, it throws ESYAException. A certificate can not be qualified and non qualified at
	 * the same time.
	 * 
	 * @return certificate
	 * @throws SmartCardException
	 * @throws ESYAException
	 */
	public synchronized ECertificate getSignatureCertificate(boolean checkIsQualified, boolean checkBeingNonQualified) throws SmartCardException, ESYAException
	{
		if(mSignatureCert == null)
		{
			List<byte []> allCerts = bsc.getSignatureCertificates();
			mSignatureCert = selectCertificate(checkIsQualified, checkBeingNonQualified, allCerts);
		}

		return mSignatureCert;
	}

	/**
	 * Returns for the encryption certificate. If there are more than one certificates in the card in requested
	 * attributes, it wants user to select the certificate. It caches the selected certificate, to reset cache,
	 * call reset function.
	 * 
	 * @param checkIsQualified
	 * @param checkBeingNonQualified
	 * @return
	 * @throws SmartCardException
	 * @throws ESYAException
	 */
	public synchronized ECertificate getEncryptionCertificate(boolean checkIsQualified, boolean checkBeingNonQualified) throws SmartCardException, ESYAException
	{
		if(mEncryptionCert == null)
		{
			List<byte []> allCerts = bsc.getEncryptionCertificates();
			mEncryptionCert = selectCertificate(checkIsQualified, checkBeingNonQualified, allCerts);
		}

		return mEncryptionCert;
	}

	private ECertificate selectCertificate(boolean checkIsQualified, boolean checkBeingNonQualified,List<byte []> aCerts) throws SmartCardException, ESYAException
	{
		if(aCerts != null  && aCerts.size() == 0 )
			throw new ESYAException("No certificate in smartcard");

		if(checkIsQualified && checkBeingNonQualified)
			throw new ESYAException("A certificate is either qualified or not, cannot be both");

		List<ECertificate> certs = new ArrayList<ECertificate>();

		for (byte[] bs : aCerts) 
		{
			ECertificate cert = new ECertificate(bs);

			if(checkIsQualified)
			{
				if( cert.isQualifiedCertificate())
					certs.add(cert);
			}
			else if(checkBeingNonQualified)
			{
				if( !cert.isQualifiedCertificate())
					certs.add(cert);
			}
			else
			{
				certs.add(cert);
			}
		}

		ECertificate selectedCert = null;

		if(certs.size() == 0)
		{
			if(checkIsQualified)
				throw new ESYAException("No qualified certificate in smartcard");
			else if(checkBeingNonQualified)
				throw new ESYAException("No non-qualified certificate in smartcard");
			
			throw new ESYAException("No certificate in smartcard");
		}
		else if(certs.size() == 1)
		{
			selectedCert = certs.get(0);
		}
		else
		{
			String [] optionList = new String[certs.size()];
			for(int i =0 ; i < certs.size(); i++)
			{
				//Sadece nitelikli ve mali mühür sertifikalarını seçmek için aşağıdaki kodu kullanabilirsiniz
				/*
				if (certs.get(i).isQualifiedCertificate()) {
					optionList[i] = certs.get(i).getSubject().getCommonNameAttribute()+ " (Nitelikli)";
				} else if (certs.get(i).isMaliMuhurCertificate()) {
					optionList[i] = certs.get(i).getSubject().getCommonNameAttribute()+ " (MaliMühür)";
				}*/
				optionList[i] = certs.get(i).getSubject().getCommonNameAttribute();
			}

			int result = askOption(null, null, optionList,"Certificate List",new String[]{"OK"});

			if(result < 0)
				selectedCert = null;
			else 
				selectedCert = certs.get(result);
		} 
		return selectedCert;
	}

	private String getSelectedSerialNumber()
	{
		return mSerialNumber;
	}

	private int getSlotCount()
	{
		return mSlotCount;
	}

	public BaseSmartCard getBasicSmartCard()
	{
		return bsc;
	}


	public static void reset() throws SmartCardException
	{
		synchronized (lockObject) 
		{
			mSCManager = null;
		}
	}


	private int askOption(Component aParent, Icon aIcon, String[] aSecenekList,String aBaslik,String[] aOptions)
	{
		JComboBox combo = new JComboBox(aSecenekList);

		int cevap = JOptionPane.showOptionDialog(aParent, combo,
				aBaslik,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null, aOptions, aOptions[0]);

		if(cevap == 1)
			return -1;
		return combo.getSelectedIndex();
	}
}
