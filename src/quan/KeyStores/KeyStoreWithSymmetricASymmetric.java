package quan.KeyStores;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

//http://tutorials.jenkov.com/java-cryptography/keystore.html
//https://www.baeldung.com/java-keystore
//

public class KeyStoreWithSymmetricASymmetric {
// refer: https://www.baeldung.com/java-keystore
	public static void main(String[] args) {
			saveKeyStores();
			
			KeyStore keyStore = getKeyStores();
			
			// get symmetricKey from keystore
			Key key = getSymmetricKey(keyStore);
			System.out.println("Secret of Key: " + new String(key.getEncoded()));
			
			//get ASymmetricKey form keystore
		    Key privateKey = getAsymmetricKey(keyStore);
	}
	
	@SuppressWarnings("resource")
	public static void saveKeyStores() {
		try {
			
			Scanner scanner = new Scanner(System.in);
			System.out.println(" Enter the password for keystore: ");
			char[] password = scanner.nextLine().trim().toCharArray();
			//store keystore
			KeyStore keyStore = KeyStore.getInstance("jceks");
			//create new KeyStore
			keyStore.load(null, password);
			
			FileOutputStream fileOutputStream = new FileOutputStream("keyStores.jceks");
			keyStore.store(fileOutputStream, password);
			
			System.out.println("create and save ok");
			
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static KeyStore getKeyStores() {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println(" Enter the password for getting KeyStore: ");
			char[] password = scanner.nextLine().trim().toCharArray();
			
			KeyStore keyStore = KeyStore.getInstance("jceks");
			
			FileInputStream fileInputStream = new FileInputStream("keyStores.jceks");
			keyStore.load(fileInputStream, password);
			System.out.println("Getting KeyStore Ok");
			return keyStore;
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PrivateKey getAsymmetricKey(KeyStore keyStore) {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println(" Enter the password to setKeyEntry:");
			char[] password = scanner.nextLine().trim().toCharArray();
			
			CertAndKeyGen certAndKeyGen = new CertAndKeyGen("RSA", "SHA1WithRSA");
			certAndKeyGen.generate(2048);
			X509Certificate[] x509Certificate =  new X509Certificate[1];
			x509Certificate[0] = certAndKeyGen.getSelfCertificate(new X500Name("CN=ROOT"), 365*24*3600);
			keyStore.setKeyEntry("ASymmetricAlias", certAndKeyGen.getPrivateKey(), password, x509Certificate);
			System.out.println("setKeyEntry ok");
			//
			
			System.out.println(" Enter the password to getting private key:");
			char[] passwordOfPrivateKey = scanner.nextLine().trim().toCharArray();
			Key privateKey = keyStore.getKey("ASymmetricAlias", passwordOfPrivateKey);
			System.out.println();
			
			if (privateKey instanceof PrivateKey) {
				System.out.println("Get private key : " + Base64.getEncoder().encodeToString(privateKey.toString().getBytes()));
				Certificate[] certs = keyStore.getCertificateChain("ASymmetricAlias");
				System.out.println("Certificate chain length : " + certs.length);
				for (Certificate cert : certs) {
					//System.out.println("cert: " + cert.toString());
				}
			} else {
				System.out.println("Key is not private key");
			}
			
			return (PrivateKey) privateKey;
			
		} catch (NoSuchAlgorithmException | InvalidKeyException | CertificateException | SignatureException | NoSuchProviderException | IOException | KeyStoreException | UnrecoverableKeyException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Key getSymmetricKey(KeyStore keyStore) {
		// we will store symmetric key using our keystore

		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println(" Enter the password to setEntry");
			char[] password = scanner.nextLine().trim().toCharArray();
			
			SecretKeySpec secretKeySpec = new SecretKeySpec("AAAAAA".getBytes(), "AES");
			SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKeySpec);
			KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password);

			keyStore.setEntry("symmetricAlias", secretKeyEntry, protectionParameter);
			System.out.println("Set entry ok !");
			
			// getKey
			System.out.println(" Enter the password to get secret key: ");
			char[] passwordOfSecretKey = scanner.nextLine().trim().toCharArray();
			Key key = keyStore.getKey("symmetricAlias", passwordOfSecretKey);
			System.out.println("Getting secret key ok");
			System.out.println("getCertificate: " + keyStore.getCertificate("symmetricAlias"));

			System.out.println("contains symmetricAlias ?:" + keyStore.containsAlias("symmetricAlias"));
			System.out.println("contains symmetricAlias2 ?:" + keyStore.containsAlias("symmetricAlias2"));

			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				System.out.println(aliases.nextElement());

			}
			return key;

		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

}
