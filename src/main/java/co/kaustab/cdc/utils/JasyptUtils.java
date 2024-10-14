package co.kaustab.cdc.utils;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("jasyptUtils")
public class JasyptUtils {

	@Value("${jasypt.password}")
	private String jasyptPassword;
	
	public String decrypt(String encryptedPayload) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(jasyptPassword);
		return textEncryptor.decrypt(encryptedPayload);
	}

}