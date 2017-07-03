package tr.com.sinya.signers;

import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;

public interface ISigner {

	public void sign(ECertificate eCertificate, String pin);
}
